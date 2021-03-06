package de.btobastian.javacord.events.server;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;

/**
 * A server change owner event.
 */
public class ServerChangeOwnerEvent extends ServerEvent {

    /**
     * The new owner of the server.
     */
    private final User newOwner;

    /**
     * The old owner of the server.
     */
    private final User oldOwner;

    /**
     * Creates a new server change owner event.
     *
     * @param api The api instance of the event.
     * @param server The server of the event.
     * @param newOwner The new owner of the server.
     * @param oldOwner The old owner of the server.
     */
    public ServerChangeOwnerEvent(DiscordApi api, Server server, User newOwner, User oldOwner) {
        super(api, server);
        this.newOwner = newOwner;
        this.oldOwner = oldOwner;
    }

    /**
     * Gets the old owner of the server.
     *
     * @return The old owner of the server.
     */
    public User getOldOwner() {
        return oldOwner;
    }

    /**
     * Gets the new owner of the server.
     *
     * @return The new owner of the server.
     */
    public User getNewOwner() {
        return newOwner;
    }

}
