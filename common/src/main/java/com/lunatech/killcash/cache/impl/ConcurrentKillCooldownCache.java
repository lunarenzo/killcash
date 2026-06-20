package com.lunatech.killcash.cache.impl;

import com.lunatech.killcash.cache.KillCooldownCache;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe implementation of {@link KillCooldownCache} using ConcurrentHashMaps.
 */
public class ConcurrentKillCooldownCache implements KillCooldownCache {
    private final Map<UUID, Map<UUID, Long>> cooldownMap = new ConcurrentHashMap<>();

    @Override
    public boolean isOnCooldown(UUID killer, UUID victim) {
        Map<UUID, Long> victimMap = cooldownMap.get(killer);
        if (victimMap == null) return false;

        Long expiry = victimMap.get(victim);
        if (expiry == null) return false;

        if (System.currentTimeMillis() > expiry) {
            victimMap.remove(victim);
            if (victimMap.isEmpty()) {
                cooldownMap.remove(killer);
            }
            return false;
        }
        return true;
    }

    @Override
    public void recordKill(UUID killer, UUID victim, long cooldownSeconds) {
        if (cooldownSeconds <= 0) return;
        long expiry = System.currentTimeMillis() + (cooldownSeconds * 1000);
        cooldownMap.computeIfAbsent(killer, k -> new ConcurrentHashMap<>()).put(victim, expiry);
    }
}
