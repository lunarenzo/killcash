package com.lunatech.killcash.listener.player;

import com.lunatech.killcash.service.KillRewardService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener tracking player quit events.
 */
public class PlayerQuitListener implements Listener {
    private final KillRewardService killRewardService;

    public PlayerQuitListener(KillRewardService killRewardService) {
        this.killRewardService = killRewardService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        killRewardService.handleQuit(event.getPlayer());
    }
}
