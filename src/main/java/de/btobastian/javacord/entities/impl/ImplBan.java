package de.btobastian.javacord.entities.impl;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.entities.Ban;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;

import java.util.Optional;

/**
 * The implementation of {@link Ban}.
 */
public class ImplBan implements Ban {

    /**
     * The server of the ban.
     */
    private final Server server;

    /**
     * The banned user.
     */
    private final User user;

    /**
     * The reason for the ban.
     */
    private final String reason;

    /**
     * Creates a new ban.
     *
     * @param server The server of the ban.
     * @param data The json data of the ban.
     */
    public ImplBan(Server server, JsonNode data) {
        this.server = server;
        this.user = ((ImplDiscordApi) server.getApi()).getOrCreateUser(data.get("user"));
        this.reason = data.has("reason") ? data.get("reason").asText() : null;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }
}
