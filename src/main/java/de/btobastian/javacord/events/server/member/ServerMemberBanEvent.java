package de.btobastian.javacord.events.server.member;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;

/**
 * A server member ban event.
 */
public class ServerMemberBanEvent extends ServerMemberEvent {

    /**
     * Creates a new server member ban event.
     *
     * @param api The api instance of the event.
     * @param server The server of the event.
     * @param user The user of the event.
     */
    public ServerMemberBanEvent(DiscordApi api, Server server, User user) {
        super(api, server, user);
    }

}
