package com.lunatech.killcash.service.impl;

import com.lunatech.killcash.cache.KillCooldownCache;
import com.lunatech.killcash.config.ConfigHandler;
import com.lunatech.killcash.config.PluginConfig;
import com.lunatech.killcash.constant.PDCKeys;
import com.lunatech.killcash.hook.EconomyProvider;
import com.lunatech.killcash.pdc.PDCUtil;
import com.lunatech.killcash.service.KillRewardService;
import com.lunatech.killcash.service.MessageService;
import io.github.milkdrinkers.threadutil.Scheduler;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Default implementation of {@link KillRewardService}.
 * Enforces anti-abuse checks and asynchronously deposits economy rewards.
 */
public class DefaultKillRewardService implements KillRewardService {
    private final ConfigHandler configHandler;
    private final Supplier<EconomyProvider> economyProviderSupplier;
    private final KillCooldownCache cooldownCache;
    private final MessageService messageService;

    public DefaultKillRewardService(
            ConfigHandler configHandler,
            Supplier<EconomyProvider> economyProviderSupplier,
            KillCooldownCache cooldownCache,
            MessageService messageService
    ) {
        this.configHandler = configHandler;
        this.economyProviderSupplier = economyProviderSupplier;
        this.cooldownCache = cooldownCache;
        this.messageService = messageService;
    }

    @Override
    public void processKill(Player killer, Player victim) {
        PluginConfig.PvpReward settings = configHandler.getConfig().pvpReward;
        if (!settings.enabled) {
            return;
        }

        // 1. IP Check (Same network check)
        if (settings.antiAbuse.ipCheck) {
            InetSocketAddress killerAddr = killer.getAddress();
            InetSocketAddress victimAddr = victim.getAddress();
            if (killerAddr != null && victimAddr != null) {
                String killerIp = killerAddr.getAddress().getHostAddress();
                String victimIp = victimAddr.getAddress().getHostAddress();
                if (killerIp.equals(victimIp)) {
                    messageService.sendMessage(killer, "pvp.anti-abuse-same-ip");
                    return;
                }
            }
        }

        // 2. Playtime Check (Victim eligibility check)
        long minPlaytimeSec = settings.antiAbuse.minPlaytime;
        if (minPlaytimeSec > 0) {
            long victimPlaytimeSec = victim.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20; // 20 ticks = 1 second
            if (victimPlaytimeSec < minPlaytimeSec) {
                messageService.sendMessage(killer, "pvp.anti-abuse-playtime", Map.of("victim", victim.getName()));
                return;
            }
        }

        // 3. Cooldown Check
        if (cooldownCache.isOnCooldown(killer.getUniqueId(), victim.getUniqueId())) {
            messageService.sendMessage(killer, "pvp.anti-abuse-cooldown", Map.of("victim", victim.getName()));
            return;
        }

        // Update player statistics in PDC (must be done on the regional tick thread)
        PDCUtil.incrementInt(killer, PDCKeys.KILLS, 1);
        PDCUtil.incrementInt(killer, PDCKeys.STREAK, 1);
        PDCUtil.incrementInt(victim, PDCKeys.DEATHS, 1);
        PDCUtil.setInt(victim, PDCKeys.STREAK, 0);

        int newStreak = PDCUtil.getInt(killer, PDCKeys.STREAK, 1);

        // Calculate base reward amount
        double min = settings.minReward;
        double max = settings.maxReward;
        double baseReward = min;
        if (max > min) {
            baseReward = min + (ThreadLocalRandom.current().nextDouble() * (max - min));
        }

        // Apply permission multipliers (find the highest applicable multiplier)
        double permissionMultiplier = 1.0;
        if (settings.permissionMultipliers != null) {
            for (java.util.Map.Entry<String, Double> entry : settings.permissionMultipliers.entrySet()) {
                if (killer.hasPermission(entry.getKey())) {
                    permissionMultiplier = Math.max(permissionMultiplier, entry.getValue());
                }
            }
        }

        // Apply streak multipliers
        double streakMultiplier = 1.0;
        if (settings.streakMultipliers != null) {
            Double mult = settings.streakMultipliers.get(String.valueOf(newStreak));
            if (mult != null) {
                streakMultiplier = mult;
            }
        }

        final double finalReward = baseReward * permissionMultiplier * streakMultiplier;

        // Put killer on cooldown for this victim immediately to prevent race conditions during async operations
        if (settings.antiAbuse.killCooldown > 0) {
            cooldownCache.recordKill(killer.getUniqueId(), victim.getUniqueId(), settings.antiAbuse.killCooldown);
        }

        // Perform economy operations asynchronously off-thread to avoid blocking tick speed
        Scheduler.async(() -> {
            boolean success = economyProviderSupplier.get().deposit(killer, finalReward);
            if (success) {
                // Dispatch message back to player's thread context
                Scheduler.sync(() -> {
                    if (killer.isOnline()) {
                        messageService.sendMessage(killer, "pvp.reward-received", Map.of(
                                "amount", String.format("%.2f", finalReward),
                                "victim", victim.getName()
                        ));

                        // Notify killer of their current streak
                        messageService.sendMessage(killer, "pvp.streak-active", Map.of("streak", String.valueOf(newStreak)));
                    }
                }).execute();
            }
        }).execute();
    }
}
