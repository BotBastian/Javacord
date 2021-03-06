package de.btobastian.javacord;

import de.btobastian.javacord.utils.logging.LoggerUtil;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * This class contains some static information about Javacord.
 */
public class Javacord {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(Javacord.class);

    /**
     * The current javacord version.
     */
    public static final String VERSION = "3.0.0";

    /**
     * The github url of javacord.
     */
    public static final String GITHUB_URL = "https://github.com/BtoBastian/Javacord";

    /**
     * The user agent used for requests.
     */
    public static final String USER_AGENT = "DiscordBot (" + GITHUB_URL + ", v" + VERSION + ")";

    /**
     * The gateway protocol version from Discord which we are using.
     * A list with all protocol versions can be found
     * <a href="https://discordapp.com/developers/docs/topics/gateway#gateway-protocol-versions">here</a>.
     */
    public static final String DISCORD_GATEWAY_PROTOCOL_VERSION = "6";

    private Javacord() { }

    /**
     * This method can be used as method reference in the
     * {@link CompletableFuture#exceptionally(Function)} method. It unwraps
     * {@link CompletionException CompletionExceptions} first and logs it afterwards.
     *
     * @param throwable The exception to print.
     * @param <T> The return type of the function.
     * @return <code>null</code>
     */
    public static <T> T exceptionLogger(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while ((throwable instanceof CompletionException) && (cause != null)) {
            throwable = cause;
            cause = throwable.getCause();
        }
        logger.error("Caught unhandled exception!", throwable);
        return null;
    }

}
