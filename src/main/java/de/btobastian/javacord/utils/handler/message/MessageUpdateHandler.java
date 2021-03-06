package de.btobastian.javacord.utils.handler.message;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.channels.ServerTextChannel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.Embed;
import de.btobastian.javacord.entities.message.embed.impl.ImplEmbed;
import de.btobastian.javacord.entities.message.impl.ImplMessage;
import de.btobastian.javacord.events.message.MessageEditEvent;
import de.btobastian.javacord.listeners.message.MessageEditListener;
import de.btobastian.javacord.utils.PacketHandler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Handles the message update packet.
 */
public class MessageUpdateHandler extends PacketHandler {

    /**
     * A map with the last known edit timestamps.
     */
    private final ConcurrentHashMap<Long, Long> lastKnownEditTimestamps = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public MessageUpdateHandler(DiscordApi api) {
        super(api, true, "MESSAGE_UPDATE");
        long offset = this.api.getTimeOffset() == null ? 0 : this.api.getTimeOffset();
        api.getThreadPool().getScheduler().scheduleAtFixedRate(
                () -> lastKnownEditTimestamps.entrySet().removeIf(
                        entry -> System.currentTimeMillis() + offset - entry.getValue() > 5000)
                , 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void handle(JsonNode packet) {
        long messageId = packet.get("id").asLong();
        long channelId = packet.get("channel_id").asLong();

        api.getTextChannelById(channelId).ifPresent(channel -> {
            Optional<ImplMessage> message = api.getCachedMessageById(messageId).map(msg -> (ImplMessage) msg);

            MessageEditEvent editEvent = null;
            if (packet.has("edited_timestamp") && !packet.get("edited_timestamp").isNull()) {
                message.ifPresent(msg ->
                        msg.setLastEditTime(OffsetDateTime.parse(packet.get("edited_timestamp").asText()).toInstant()));

                long editTimestamp =
                        OffsetDateTime.parse(packet.get("edited_timestamp").asText()).toInstant().toEpochMilli();
                long lastKnownEditTimestamp = lastKnownEditTimestamps.getOrDefault(messageId, 0L);
                lastKnownEditTimestamps.put(messageId, editTimestamp);

                boolean isMostLikelyAnEdit = true;
                long offset = api.getTimeOffset() == null ? 0 : api.getTimeOffset();
                if (editTimestamp == lastKnownEditTimestamp) {
                    isMostLikelyAnEdit = false;
                } else if (System.currentTimeMillis() + offset - editTimestamp > 5000) {
                    isMostLikelyAnEdit = false;
                }

                String oldContent = message.map(Message::getContent).orElse(null);
                List<Embed> oldEmbeds = message.map(Message::getEmbeds).orElse(null);

                String newContent = null;
                if (packet.has("content")) {
                    newContent = packet.get("content").asText();
                    String finalNewContent = newContent;
                    message.ifPresent(msg -> msg.setContent(finalNewContent));
                }
                List<Embed> newEmbeds = null;
                if (packet.has("embeds")) {
                    newEmbeds = new ArrayList<>();
                    for (JsonNode embedJson : packet.get("embeds")) {
                        Embed embed = new ImplEmbed(embedJson);
                        newEmbeds.add(embed);
                    }
                    List<Embed> finalNewEmbeds = newEmbeds;
                    message.ifPresent(msg -> msg.setEmbeds(finalNewEmbeds));
                }

                if (oldContent != null && newContent != null && !oldContent.equals(newContent)) {
                    // If the old content doesn't match the new content it's for sure an edit
                    isMostLikelyAnEdit = true;
                }

                if (oldEmbeds != null && newEmbeds != null) {
                    if (newEmbeds.size() != oldEmbeds.size()) {
                        isMostLikelyAnEdit = true;
                    } else {
                        for (int i = 0; i < newEmbeds.size(); i++) {
                            if (!newEmbeds.get(i).toBuilder().toJsonNode().toString()
                                    .equals(oldEmbeds.get(i).toBuilder().toJsonNode().toString())) {
                                isMostLikelyAnEdit = true;
                            }
                        }
                    }
                }

                if (isMostLikelyAnEdit) {
                    editEvent =
                            new MessageEditEvent(api, messageId, channel, newContent, newEmbeds, oldContent, oldEmbeds);
                }
            }

            if (editEvent != null) {
                dispatchEditEvent(editEvent);
            }
        });
    }

    /**
     * Dispatches an edit event.
     *
     * @param event The event to dispatch.
     */
    private void dispatchEditEvent(MessageEditEvent event) {
        List<MessageEditListener> listeners = new ArrayList<>();
        listeners.addAll(api.getMessageEditListeners(event.getMessageId()));
        listeners.addAll(event.getChannel().getMessageEditListeners());
        if (event.getChannel() instanceof ServerTextChannel) {
            listeners.addAll(((ServerTextChannel) event.getChannel()).getServer().getMessageEditListeners());
        }
        listeners.addAll(api.getMessageEditListeners());

        dispatchEvent(listeners, listener -> listener.onMessageEdit(event));
    }

}