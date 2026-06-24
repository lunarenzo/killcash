package com.lunatech.killcash.listener.player;

import com.lunatech.killcash.service.KillRewardService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener tracking player join events.
 */
public class PlayerJoinListener implements Listener {
    private final KillRewardService killRewardService;

    public PlayerJoinListener(KillRewardService killRewardService) {
        this.killRewardService = killRewardService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        killRewardService.handleJoin(event.getPlayer());
    }
}
