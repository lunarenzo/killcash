package com.lunatech.killcash.service;

import org.bukkit.entity.Player;

/**
 * Service handling calculations, anti-abuse checks, and deposits of cash rewards for player kills.
 */
public interface KillRewardService {
    /**
     * Processes a player killing another player, executing all anti-abuse rules and issuing the economy reward.
     *
     * @param killer the player who performed the kill
     * @param victim the player who was killed
     */
    void processKill(Player killer, Player victim);

    /**
     * Handles player joining the server, potentially restoring their killstreak.
     *
     * @param player the player who joined
     */
    void handleJoin(Player player);

    /**
     * Handles player quitting the server, tracking logout time for killstreak expiration.
     *
     * @param player the player who quit
     */
    void handleQuit(Player player);
}
