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
}
