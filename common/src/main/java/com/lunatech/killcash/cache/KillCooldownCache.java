package com.lunatech.killcash.cache;

import java.util.UUID;

/**
 * Interface representing the cache for tracking kill cooldowns between players.
 * Used to enforce anti-abuse farm prevention.
 */
public interface KillCooldownCache {
    /**
     * Checks if a killer is on cooldown for killing a victim.
     *
     * @param killer the killer's UUID
     * @param victim the victim's UUID
     * @return true if on cooldown, false otherwise
     */
    boolean isOnCooldown(UUID killer, UUID victim);

    /**
     * Records a kill, putting the killer on cooldown for the victim.
     *
     * @param killer the killer's UUID
     * @param victim the victim's UUID
     * @param cooldownSeconds the duration of the cooldown in seconds
     */
    void recordKill(UUID killer, UUID victim, long cooldownSeconds);
}
