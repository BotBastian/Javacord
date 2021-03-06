package de.btobastian.javacord.entities.channels;

import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.permissions.PermissionType;

import java.util.Optional;

/**
 * This class represents a voice channel.
 */
public interface VoiceChannel extends Channel {

    /**
     * Checks if the given user can connect to the voice channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     *
     * @param user The user to check.
     * @return Whether the given user can connect or not.
     */
    default boolean canConnect(User user) {
        if (!canSee(user)) {
            return false;
        }
        Optional<ServerTextChannel> severTextChannel = asServerTextChannel();
        return !severTextChannel.isPresent()
                || severTextChannel.get().hasPermissions(user, PermissionType.ADMINISTRATOR)
                || severTextChannel.get().hasPermissions(user, PermissionType.VOICE_CONNECT);
    }

    /**
     * Checks if the given user can mute other users in this voice channel.
     * In private chats (private channel or group channel) this always returns <code>false</code>.
     *
     * @param user The user to check.
     * @return Whether the given user can mute other users or not.
     */
    default boolean canMuteUsers(User user) {
        if (!canConnect(user) || getType() == ChannelType.PRIVATE_CHANNEL || getType() == ChannelType.GROUP_CHANNEL) {
            return false;
        }
        Optional<ServerVoiceChannel> serverVoiceChannel = asServerVoiceChannel();
        return !serverVoiceChannel.isPresent()
                || serverVoiceChannel.get().hasPermissions(user, PermissionType.ADMINISTRATOR)
                || serverVoiceChannel.get().hasPermissions(user, PermissionType.VOICE_MUTE_MEMBERS);
    }

}
