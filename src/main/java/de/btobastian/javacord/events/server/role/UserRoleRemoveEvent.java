package de.btobastian.javacord.events.server.role;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.permissions.Role;

/**
 * A user role remove event.
 */
public class UserRoleRemoveEvent extends UserRoleEvent {

    /**
     * Creates a new user role remove event.
     *
     * @param api The api instance of the event.
     * @param role The role of the event.
     * @param user The user of the event.
     */
    public UserRoleRemoveEvent(DiscordApi api, Role role, User user) {
        super(api, role, user);
    }

}
