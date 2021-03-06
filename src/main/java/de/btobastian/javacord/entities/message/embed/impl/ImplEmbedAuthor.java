package de.btobastian.javacord.entities.message.embed.impl;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.javacord.entities.message.embed.EmbedAuthor;
import de.btobastian.javacord.utils.logging.LoggerUtil;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * The implementation of {@link EmbedAuthor}.
 */
public class ImplEmbedAuthor implements EmbedAuthor {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(ImplEmbedAuthor.class);

    private String name;
    private String url;
    private String iconUrl;
    private String proxyIconUrl;

    /**
     * Creates a new embed author.
     *
     * @param data The json data of the author.
     */
    public ImplEmbedAuthor(JsonNode data) {
        name = data.has("name") ? data.get("name").asText() : null;
        url = data.has("url") && !data.get("url").isNull() ? data.get("url").asText() : null;
        iconUrl = data.has("icon_url") && !data.get("icon_url").isNull() ? data.get("icon_url").asText() : null;
        proxyIconUrl = data.has("proxy_icon_url") && !data.get("proxy_icon_url").isNull()
                ? data.get("proxy_icon_url").asText() : null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<URL> getUrl() {
        if (url == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(url));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the embed author is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<URL> getIconUrl() {
        if (iconUrl == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(iconUrl));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the icon url of the embed author is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<URL> getProxyIconUrl() {
        if (proxyIconUrl == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(proxyIconUrl));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the proxy icon url of the embed author is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

}