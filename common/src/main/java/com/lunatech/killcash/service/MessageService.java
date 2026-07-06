package com.lunatech.killcash.service;

import org.bukkit.entity.Player;
import java.util.Map;

/**
 * Service to delegate player messaging and translation to the platform-specific implementation.
 * Decouples the common PvP logic from platform-specific color parsing/chat formatting libraries.
 */
public interface MessageService {
    /**
     * Sends a parsed translation message to the player.
     *
     * @param player the recipient player
     * @param translationKey the key for the translation template
     */
    void sendMessage(Player player, String translationKey);

    /**
     * Sends a parsed translation message to the player with placeholders replaced.
     *
     * @param player the recipient player
     * @param translationKey the key for the translation template
     * @param placeholders mapping of placeholder keys to replacement values
     */
    void sendMessage(Player player, String translationKey, Map<String, String> placeholders);

    /**
     * Sends an action bar message to the player with placeholders replaced.
     *
     * @param player the recipient player
     * @param translationKey the key for the translation template
     * @param placeholders mapping of placeholder keys to replacement values
     */
    void sendActionBar(Player player, String translationKey, Map<String, String> placeholders);

    /**
     * Broadcasts a translation message to the server with placeholders replaced.
     *
     * @param translationKey the key for the translation template
     * @param placeholders mapping of placeholder keys to replacement values
     */
    void broadcast(String translationKey, Map<String, String> placeholders);

    /**
     * Broadcasts a raw message (already formatted or read from configuration) to the server with placeholders replaced.
     *
     * @param message the raw message
     * @param placeholders mapping of placeholder keys to replacement values
     */
    void broadcastRaw(String message, Map<String, String> placeholders);

    /**
     * Plays a sound for a player.
     *
     * @param player the player
     * @param sound the sound to play
     * @param volume the volume of the sound
     * @param pitch the pitch of the sound
     */
    void playSound(Player player, org.bukkit.Sound sound, float volume, float pitch);

    /**
     * Plays a sound for all online players.
     *
     * @param sound the sound to play
     * @param volume the volume of the sound
     * @param pitch the pitch of the sound
     */
    void broadcastSound(org.bukkit.Sound sound, float volume, float pitch);

    /**
     * Plays a sound for all online players by its sound enum name or namespace key.
     *
     * @param soundName the sound enum name or string namespace key
     * @param volume the volume of the sound
     * @param pitch the pitch of the sound
     */
    void broadcastSound(String soundName, float volume, float pitch);

    /**
     * Plays a client-side lightning strike effect visible only to the specified player.
     *
     * @param player the player who should see the lightning strike
     * @param location the location where the lightning strike should appear
     */
    void playLightningEffect(Player player, org.bukkit.Location location);

    /**
     * Plays a client-side sound effect visible only to the specified player.
     *
     * @param player the player who should see the sound
     * @param location the location where the sound should appear
     * @param soundName the sound enum name or string namespace key
     * @param volume the volume of the sound
     * @param pitch the pitch of the sound
     */
    void playSoundEffect(Player player, org.bukkit.Location location, String soundName, float volume, float pitch);
}
