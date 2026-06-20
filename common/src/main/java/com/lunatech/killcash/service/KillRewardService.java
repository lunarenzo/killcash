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
}
