package de.btobastian.javacord.utils.ratelimits;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.exceptions.RatelimitException;
import de.btobastian.javacord.utils.logging.LoggerUtil;
import de.btobastian.javacord.utils.rest.RestRequest;
import de.btobastian.javacord.utils.rest.RestRequestResult;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * This class manages ratelimits and keeps track of them.
 */
public class RatelimitManager {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(RatelimitManager.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Set<RatelimitBucket> buckets = ConcurrentHashMap.newKeySet();
    private final HashMap<RatelimitBucket, ConcurrentLinkedQueue<RestRequest<?>>> queues = new HashMap<>();

    private final ImplDiscordApi api;

    /**
     * Creates a new ratelimit manager for the given api.
     *
     * @param api The api instance of the bot.
     */
    public RatelimitManager(DiscordApi api) {
        this.api = (ImplDiscordApi) api;
    }

    /**
     * Adds a request to the queue based on the ratelimit bucket.
     * This method is automatically called when using {@link RestRequest#execute(Function)}!
     *
     * @param request The request to queue.
     */
    public void queueRequest(RestRequest<?> request) {
        // Get the bucket for the current request type.
        RatelimitBucket bucket = buckets
                .parallelStream()
                .filter(b -> b.equals(request.getEndpoint(), request.getMajorUrlParameter().orElse(null)))
                .findAny()
                .orElse(new RatelimitBucket(api, request.getEndpoint(), request.getMajorUrlParameter().orElse(null)));

        // Add bucket to list with buckets
        buckets.add(bucket);

        // Get the queue for the current bucket or create a new one if there's no one already
        ConcurrentLinkedQueue<RestRequest<?>> queue =
                queues.computeIfAbsent(bucket, k -> new ConcurrentLinkedQueue<>());

        // Add the request to the queue and check if there's already a scheduler working on the queue
        boolean startScheduler = false;
        synchronized (bucket) {
            synchronized (queue) {
                if (bucket.hasActiveScheduler()) {
                    queue.add(request);
                } else {
                    bucket.setHasActiveScheduler(true);
                    queue.add(request);
                    startScheduler = true;
                }
            }
        }

        if (!startScheduler) {
            return;
        }
        int delay = bucket.getTimeTillSpaceGetsAvailable();
        if (delay > 0) {
            synchronized (bucket) {
                synchronized (queue) {
                    if (request.incrementRetryCounter()) {
                        request.getResult().completeExceptionally(
                                new RatelimitException(request.getOrigin(),
                                        "You have been ratelimited and ran out of retires!", request)
                        );
                        queue.remove(request);
                        bucket.setHasActiveScheduler(false);
                        return;
                    }
                }
            }
            logger.debug("Delaying requests to {} for {}ms to prevent hitting ratelimits", bucket, delay);
        }
        // Start a scheduler to work off the queue
        scheduler.schedule(() -> api.getThreadPool().getExecutorService().submit(() -> {
            try {
                while (!queue.isEmpty()) {
                    if (!bucket.hasSpace()) {
                        synchronized (queue) {
                            // Remove if we retried to often
                            queue.removeIf(req -> {
                                if (req.incrementRetryCounter()) {
                                    req.getResult().completeExceptionally(
                                            new RatelimitException(req.getOrigin(),
                                                    "You have been ratelimited and ran out of retires!", req)
                                    );
                                    return true;
                                }
                                return false;
                            });
                            if (queue.isEmpty()) {
                                break;
                            }
                        }
                        try {
                            int sleepTime = bucket.getTimeTillSpaceGetsAvailable();
                            if (sleepTime > 0) {
                                logger.debug("Delaying requests to {} for {}ms to prevent hitting ratelimits", bucket, sleepTime);
                                Thread.sleep(sleepTime);
                            }
                        } catch (InterruptedException e) {
                            logger.warn("We got interrupted while waiting for a rate limit!", e);
                        }
                    }
                    RestRequest<?> restRequest = queue.peek();
                    boolean remove = true;
                    try {
                        RestRequestResult result = restRequest.executeBlocking();

                        long currentTime = System.currentTimeMillis();

                        if (api.getTimeOffset() == null) {
                            calculateOffset(currentTime, result);
                        }

                        if (result.getResponse().code() == 429) {
                            remove = false;
                            logger.debug("Received a 429 response from Discord! Recalculating time offset...");
                            api.setTimeOffset(null);

                            int retryAfter =
                                    result.getJsonBody().isNull() ? 0 : result.getJsonBody().get("retry_after").asInt();
                            bucket.setRateLimitRemaining(0);
                            bucket.setRateLimitResetTimestamp(currentTime + retryAfter);
                        } else {
                            restRequest.getResult().complete(result);

                            String remaining = result.getResponse().header("X-RateLimit-Remaining", "1");
                            long reset = restRequest
                                    .getEndpoint()
                                    .getHardcodedRatelimit()
                                    .map(ratelimit -> currentTime + api.getTimeOffset() + ratelimit)
                                    .orElseGet(() -> Long.parseLong(result.getResponse().header("X-RateLimit-Reset")) * 1000);
                            String global = result.getResponse().header("X-RateLimit-Global");

                            if (global != null && global.equals("true")) {
                                // Mark the endpoint as global
                                bucket.getEndpoint().ifPresent(endpoint -> endpoint.setGlobal(true));
                            }

                            bucket.setRateLimitRemaining(Integer.parseInt(remaining));
                            bucket.setRateLimitResetTimestamp(reset);
                        }
                    } catch (Exception e) {
                        restRequest.getResult().completeExceptionally(e);
                    }
                    if (remove) {
                        queue.remove(restRequest);
                    }
                }
            } catch (Throwable t) {
                logger.error("Exception in RatelimitManager! Please contact the developer!", t);
            } finally {
                synchronized (bucket) {
                    bucket.setHasActiveScheduler(false);
                }
            }
        }), delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Calculates the offset of the local time and discord's time.
     *
     * @param currentTime The current time.
     * @param result The result of the rest request.
     */
    private void calculateOffset(long currentTime, RestRequestResult result) {
        // Discord sends the date in their header in the format RFC_1123_DATE_TIME
        // We use this header to calculate a possible offset between our local time and the discord time
        String date = result.getResponse().header("Date");
        if (date != null) {
            long discordTimestamp = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli();
            api.setTimeOffset((discordTimestamp - currentTime));
            logger.debug("Calculated an offset of " + api.getTimeOffset() + " to the Discord time.");
        }
    }

}
