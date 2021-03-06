package de.btobastian.javacord.entities;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.btobastian.javacord.entities.channels.ServerChannel;
import de.btobastian.javacord.entities.channels.ServerTextChannel;
import de.btobastian.javacord.entities.channels.ServerVoiceChannel;
import de.btobastian.javacord.entities.impl.ImplServer;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.utils.rest.RestEndpoint;
import de.btobastian.javacord.utils.rest.RestMethod;
import de.btobastian.javacord.utils.rest.RestRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class can be used to update the settings of a server.
 */
public class ServerUpdater {

    /**
     * The server to update.
     */
    private final ImplServer server;

    /**
     * A map with all user roles to update.
     */
    private final Map<User, Collection<Role>> userRoles = new HashMap<>();

    /**
     * A map with all user nicknames to update.
     */
    private final Map<User, String> userNicknames = new HashMap<>();

    /**
     * The name to update.
     */
    private String name = null;

    /**
     * The region to update.
     */
    private Region region = null;

    /**
     * The verification level to update.
     */
    private VerificationLevel verificationLevel = null;

    /**
     * The default message notification level to update.
     */
    private DefaultMessageNotificationLevel defaultMessageNotificationLevel = null;

    /**
     * The afk channel to update.
     */
    private ServerChannel afkChannel = null;

    /**
     * The afk timeout to update.
     */
    private Integer afkTimeout = null;

    /**
     * The icon to update.
     */
    private BufferedImage icon = null;

    /**
     * The owner to update.
     */
    private User owner = null;

    /**
     * The splash to update.
     */
    private BufferedImage splash = null;

    /**
     * The system channel to update.
     */
    private ServerChannel systemChannel = null;

    /**
     * Creates a new server updater.
     *
     * @param server The server to update.
     */
    public ServerUpdater(Server server) {
        this.server = (ImplServer) server;
    }

    /**
     * Queues the name to be updated.
     *
     * @param name The new name of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Queues the region to be updated.
     *
     * @param region The new region of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setRegion(Region region) {
        this.region = region;
        return this;
    }

    /**
     * Queues the verification level to be updated.
     *
     * @param verificationLevel The new verification level of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setVerificationLevel(VerificationLevel verificationLevel) {
        this.verificationLevel = verificationLevel;
        return this;
    }

    /**
     * Queues the default message notification level to be updated.
     *
     * @param defaultMessageNotificationLevel The new default message notification level of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setDefaultMessageNotificationLevel(
            DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        this.defaultMessageNotificationLevel = defaultMessageNotificationLevel;
        return this;
    }

    /**
     * Queues the afk channel to be updated.
     *
     * @param afkChannel The new afk channel of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setAfkChannel(ServerVoiceChannel afkChannel) {
        this.afkChannel = afkChannel;
        return this;
    }

    /**
     * Queues the afk timeout in seconds to be updated.
     *
     * @param afkTimeout The new afk timeout in seconds of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setAfkTimeoutInSeconds(int afkTimeout) {
        this.afkTimeout = afkTimeout;
        return this;
    }

    /**
     * Queues the icon to be updated.
     *
     * @param icon The new icon of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setIcon(BufferedImage icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Queues the owner to be updated.
     * You must be the owner of this server in order to transfer it!
     *
     * @param owner The new owner of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Queues the splash to be updated.
     *
     * @param splash The new splash of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setSplash(BufferedImage splash) {
        this.splash = splash;
        return this;
    }

    /**
     * Queues the system channel to be updated.
     *
     * @param systemChannel The new system channel of the server.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setSystemChannel(ServerTextChannel systemChannel) {
        this.systemChannel = systemChannel;
        return this;
    }

    /**
     * Queues a user's nickname to be updated.
     *
     * @param user The user whose nickname should be updated.
     * @param nickname The new nickname of the user.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater setNickname(User user, String nickname) {
        userNicknames.put(user, nickname);
        return this;
    }

    /**
     * Queues a role to be assigned to the user.
     *
     * @param user The user to whom the role should be assigned.
     * @param role The role to be assigned.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater addRoleToUser(User user, Role role) {
        Collection<Role> userRoles = this.userRoles.putIfAbsent(user, new ArrayList<>(server.getRolesOf(user)));
        userRoles.add(role);
        return this;
    }

    /**
     * Queues a collection of roles to be assigned to the user.
     *
     * @param user The user to whom the roles should be assigned.
     * @param roles The collection of roles to be assigned.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater addAllRolesToUser(User user, Collection<Role> roles) {
        Collection<Role> userRoles = this.userRoles.putIfAbsent(user, new ArrayList<>(server.getRolesOf(user)));
        userRoles.addAll(roles);
        return this;
    }

    /**
     * Queues a role to be removed from the user.
     *
     * @param user The user who should lose the role.
     * @param role The role to be removed.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater removeRoleFromUser(User user, Role role) {
        Collection<Role> userRoles = this.userRoles.putIfAbsent(user, new ArrayList<>(server.getRolesOf(user)));
        userRoles.remove(role);
        return this;
    }

    /**
     * Queues a collection of roles to be removed from the user.
     *
     * @param user The user who should lose the roles.
     * @param roles The collection of roles to be removed.
     * @return The current instance in order to chain call methods.
     */
    public ServerUpdater removeAllRolesFromUser(User user, Collection<Role> roles) {
        Collection<Role> userRoles = this.userRoles.putIfAbsent(user, new ArrayList<>(server.getRolesOf(user)));
        userRoles.removeAll(roles);
        return this;
    }

    /**
     * Performs the queued updates.
     *
     * @return A future to check if the update was successful.
     */
    public CompletableFuture<Void> update() {
        // A list with all tasks, initialized with all role updates
        ArrayList<CompletableFuture<?>> tasks = userRoles.entrySet().stream()
                .map(entry -> server.updateRoles(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
        // User nicknames
        tasks.addAll(userNicknames.entrySet().stream()
                .map(entry -> server.updateNickname(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));

        // TODO nickname update and role update use the same endpoint -> There's potential for saving some REST calls

        // Server settings
        boolean patchServer = false;
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (name != null) {
            body.put("name", name);
            patchServer = true;
        }
        if (region != null) {
            body.put("region", region.getKey());
            patchServer = true;
        }
        if (verificationLevel != null) {
            body.put("verification_level", verificationLevel.getId());
            patchServer = true;
        }
        if (defaultMessageNotificationLevel != null) {
            body.put("default_message_notifications", defaultMessageNotificationLevel.getId());
            patchServer = true;
        }
        if (afkChannel != null) {
            body.put("afk_channel_id", afkChannel.getIdAsString());
            patchServer = true;
        }
        if (afkTimeout != null) {
            body.put("afk_timeout", afkTimeout.intValue());
            patchServer = true;
        }
        if (icon != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(icon, "jpg", os);
                String base64Icon = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
                body.put("icon", base64Icon);
            } catch (IOException e) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
            patchServer = true;
        }
        if (splash != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(splash, "jpg", os);
                String base64Icon = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
                body.put("splash", base64Icon);
            } catch (IOException e) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
            patchServer = true;
        }
        if (owner != null) {
            body.put("owner_id", owner.getIdAsString());
            patchServer = true;
        }
        if (systemChannel != null) {
            body.put("system_channel_id", systemChannel.getIdAsString());
            patchServer = true;
        }
        // Only make a REST call, if we really want to update something
        if (patchServer) {
            tasks.add(new RestRequest<Void>(server.getApi(), RestMethod.PATCH, RestEndpoint.SERVER)
                    .setUrlParameters(server.getIdAsString())
                    .setBody(body)
                    .execute(result -> null));
        }

        CompletableFuture<?>[] tasksArray = tasks.toArray(new CompletableFuture<?>[tasks.size()]);
        return CompletableFuture.allOf(tasksArray);
    }

}
