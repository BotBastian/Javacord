package de.btobastian.javacord.entities.permissions.impl;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.impl.ImplServer;
import de.btobastian.javacord.entities.permissions.Permissions;
import de.btobastian.javacord.entities.permissions.Role;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

/**
 * The implementation of {@link Role}.
 */
public class ImplRole implements Role {

    /**
     * The discord api instance.
     */
    private final ImplDiscordApi api;

    /**
     * The server of the role.
     */
    private final ImplServer server;

    /**
     * The id of the role.
     */
    private final long id;

    /**
     * The name of the role.
     */
    private String name;

    /**
     * The position of the role.
     */
    private int position;

    /**
     * The color of the role.
     */
    private int color;

    /**
     * Whether this role is pinned in the user listing or not.
     */
    private boolean hoist;

    /**
     * Whether this role can be mentioned or not.
     */
    private boolean mentionable;

    /**
     * The permissions of the role.
     */
    private ImplPermissions permissions;

    /**
     * Whether this role is managed by an integration or not.
     */
    private boolean managed;

    /**
     * A collection with all users with this role.
     */
    private final Collection<User> users = new HashSet<>();

    /**
     * Creates a new role object.
     *
     * @param api The discord api instance.
     * @param server The server of the role.
     * @param data The json data of the role.
     */
    public ImplRole(ImplDiscordApi api, ImplServer server, JsonNode data) {
        this.api = api;
        this.server = server;
        this.id = data.get("id").asLong();
        this.name = data.get("name").asText();
        this.position = data.get("position").asInt();
        this.color = data.get("color").asInt(0);
        this.hoist = data.get("hoist").asBoolean(false);
        this.mentionable = data.get("mentionable").asBoolean(false);
        this.permissions = new ImplPermissions(data.get("permissions").asInt(), 0);
        this.managed = data.get("managed").asBoolean(false);
    }

    /**
     * Adds a user to the role.
     *
     * @param user The user to add.
     */
    public void addUserToCache(User user) {
        users.add(user);
    }

    /**
     * Removes a user from the role.
     *
     * @param user The user to remove.
     */
    public void removeUserFromCache(User user) {
        users.remove(user);
    }

    /**
     * Sets the permissions of the role.
     *
     * @param permissions The permissions to set.
     */
    public void setPermissions(ImplPermissions permissions) {
        this.permissions = permissions;
    }

    /**
     * Sets the position of the role.
     *
     * @param position The position to set.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public Optional<Color> getColor() {
        return Optional.ofNullable(color == 0 ? null : new Color(color));
    }

    @Override
    public boolean isMentionable() {
        return mentionable;
    }

    @Override
    public boolean isDisplayedSeparately() {
        return hoist;
    }

    @Override
    public Collection<User> getUsers() {
        if (isEveryoneRole()) {
            return getServer().getMembers();
        }
        return Collections.unmodifiableCollection(users);
    }

    @Override
    public Permissions getPermissions() {
        return permissions;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    @Override
    public String toString() {
        return String.format("Role (id: %s, name: %s, server: %s)", getId(), getName(), getServer());
    }

}
