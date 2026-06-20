package com.lunatech.killcash.listener.player;

import com.lunatech.killcash.service.KillRewardService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener tracking player death events and delegating rewards handling to {@link KillRewardService}.
 */
public class PlayerDeathListener implements Listener {
    private final KillRewardService killRewardService;

    public PlayerDeathListener(KillRewardService killRewardService) {
        this.killRewardService = killRewardService;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Ensure there is a killer and it's not a self-kill (suicide)
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            killRewardService.processKill(killer, victim);
        }
    }
}
