package com.lunatech.killcash.service.impl;

import com.lunatech.killcash.Reloadable;
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
public class DefaultKillRewardService implements KillRewardService, Reloadable {
    private final ConfigHandler configHandler;
    private final Supplier<EconomyProvider> economyProviderSupplier;
    private final KillCooldownCache cooldownCache;
    private final MessageService messageService;
    private com.lunatech.killcash.AbstractKillCash plugin;
    private io.papermc.paper.threadedregions.scheduler.ScheduledTask decayTask;
    private final Map<java.util.UUID, Long> lastKillTimes = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<java.util.UUID, Long> logoutTimes = new java.util.concurrent.ConcurrentHashMap<>();

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
    public void onEnable(com.lunatech.killcash.AbstractKillCash plugin) {
        this.plugin = plugin;
        decayTask = plugin.getServer().getAsyncScheduler().runAtFixedRate(
            plugin,
            task -> runDecayCheck(plugin),
            1L, 1L, java.util.concurrent.TimeUnit.SECONDS
        );
    }

    @Override
    public void onDisable(com.lunatech.killcash.AbstractKillCash plugin) {
        if (decayTask != null) {
            decayTask.cancel();
        }
        lastKillTimes.clear();
        logoutTimes.clear();
    }

    @Override
    public void handleJoin(Player player) {
        PluginConfig.PvpReward settings = configHandler.getConfig().pvpReward;
        if (settings == null || settings.killstreakSettings == null || !settings.killstreakSettings.enabled) {
            return;
        }

        java.util.UUID uuid = player.getUniqueId();
        Long logoutTime = logoutTimes.remove(uuid);
        if (logoutTime != null) {
            long offlineDurationMs = System.currentTimeMillis() - logoutTime;
            if (offlineDurationMs > 5 * 60 * 1000) {
                PDCUtil.setInt(player, PDCKeys.STREAK, 0);
                lastKillTimes.remove(uuid);
            } else {
                Long lastKill = lastKillTimes.get(uuid);
                if (lastKill != null) {
                    lastKillTimes.put(uuid, lastKill + offlineDurationMs);
                } else {
                    lastKillTimes.put(uuid, System.currentTimeMillis());
                }
            }
        } else {
            PDCUtil.setInt(player, PDCKeys.STREAK, 0);
            lastKillTimes.remove(uuid);
        }
    }

    @Override
    public void handleQuit(Player player) {
        PluginConfig.PvpReward settings = configHandler.getConfig().pvpReward;
        if (settings == null || settings.killstreakSettings == null || !settings.killstreakSettings.enabled) {
            return;
        }

        java.util.UUID uuid = player.getUniqueId();
        logoutTimes.put(uuid, System.currentTimeMillis());
    }

    private void runDecayCheck(com.lunatech.killcash.AbstractKillCash plugin) {
        long now = System.currentTimeMillis();
        logoutTimes.entrySet().removeIf(entry -> (now - entry.getValue()) > 5 * 60 * 1000);

        PluginConfig.PvpReward settings = configHandler.getConfig().pvpReward;
        if (settings == null || settings.killstreakSettings == null || !settings.killstreakSettings.enabled) return;
        long decayTime = settings.killstreakSettings.decayTime;
        if (decayTime <= 0) return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            java.util.UUID uuid = player.getUniqueId();
            Long lastKill = lastKillTimes.get(uuid);
            if (lastKill == null) continue;

            long elapsed = (now - lastKill) / 1000;
            if (elapsed >= decayTime) {
                Scheduler.sync(() -> {
                    if (!player.isOnline()) return;
                    int currentStreak = PDCUtil.getInt(player, PDCKeys.STREAK, 0);
                    if (currentStreak <= 0) {
                        lastKillTimes.remove(uuid);
                        return;
                    }

                    Long syncLastKill = lastKillTimes.get(uuid);
                    if (syncLastKill == null) return;
                    long syncElapsed = (System.currentTimeMillis() - syncLastKill) / 1000;
                    if (syncElapsed < decayTime) return;

                    long excess = syncElapsed - decayTime;
                    int decay = 1 + (int) (excess / 10);
                    int newStreak = Math.max(0, currentStreak - decay);

                    if (newStreak != currentStreak) {
                        PDCUtil.setInt(player, PDCKeys.STREAK, newStreak);
                        if (newStreak == 0) {
                            lastKillTimes.remove(uuid);
                        } else {
                            lastKillTimes.put(uuid, System.currentTimeMillis() - (decayTime - 10 + (excess % 10)) * 1000);
                        }
                        messageService.sendMessage(player, "pvp.streak-decayed", Map.of("streak", String.valueOf(newStreak)));
                    }
                }).execute();
            }
        }
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

        int victimStreak = 0;
        int newStreak = 1;
        boolean killstreakEnabled = settings.killstreakSettings != null && settings.killstreakSettings.enabled;

        if (killstreakEnabled) {
            victimStreak = PDCUtil.getInt(victim, PDCKeys.STREAK, 0);
            PDCUtil.incrementInt(killer, PDCKeys.STREAK, 1);
            PDCUtil.setInt(victim, PDCKeys.STREAK, 0);
            newStreak = PDCUtil.getInt(killer, PDCKeys.STREAK, 1);

            long currentTime = System.currentTimeMillis();
            lastKillTimes.put(killer.getUniqueId(), currentTime);
            lastKillTimes.remove(victim.getUniqueId());
        }

        // Update player statistics in PDC (must be done on the regional tick thread)
        PDCUtil.incrementInt(killer, PDCKeys.KILLS, 1);
        PDCUtil.incrementInt(victim, PDCKeys.DEATHS, 1);

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
        if (killstreakEnabled && settings.streakMultipliers != null) {
            if (settings.killstreakSettings.useRangeSystem) {
                int highestConfiguredStreak = 0;
                for (java.util.Map.Entry<String, Double> entry : settings.streakMultipliers.entrySet()) {
                    try {
                        int configStreak = Integer.parseInt(entry.getKey());
                        if (newStreak >= configStreak && configStreak > highestConfiguredStreak) {
                            highestConfiguredStreak = configStreak;
                            streakMultiplier = entry.getValue();
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                Double mult = settings.streakMultipliers.get(String.valueOf(newStreak));
                if (mult != null) {
                    streakMultiplier = mult;
                }
            }
        }

        // Calculate shutdown bonus
        double shutdownBonus = 0.0;
        if (killstreakEnabled && victimStreak >= 3 && settings.killstreakSettings.shutdownBonusPerKill > 0) {
            shutdownBonus = victimStreak * settings.killstreakSettings.shutdownBonusPerKill;
        }

        final double finalReward = (baseReward * permissionMultiplier * streakMultiplier) + shutdownBonus;

        // Visual & Audio feedback
        handleDeathEffects(killer, victim, settings);

        if (killstreakEnabled) {
            double finalStreakMultiplier = streakMultiplier;
            if (settings.killstreakSettings.showActionBar) {
                messageService.sendActionBar(killer, "pvp.action-bar", Map.of(
                    "streak", String.valueOf(newStreak),
                    "multiplier", String.format("%.2f", finalStreakMultiplier)
                ));
            }
            messageService.playSound(killer, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // Server-wide Announcement Milestones
            if (settings.streakAnnouncements != null && settings.streakAnnouncements.containsKey(String.valueOf(newStreak))) {
                String announcement = settings.streakAnnouncements.get(String.valueOf(newStreak));
                if (announcement != null) {
                    messageService.broadcastRaw(announcement, Map.of(
                        "player", killer.getName(),
                        "killer", killer.getName(),
                        "streak", String.valueOf(newStreak)
                    ));
                    messageService.broadcastSound(org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                }
            }
        }

        // Put killer on cooldown for this victim immediately to prevent race conditions during async operations
        if (settings.antiAbuse.killCooldown > 0) {
            cooldownCache.recordKill(killer.getUniqueId(), victim.getUniqueId(), settings.antiAbuse.killCooldown);
        }

        // Perform economy operations asynchronously off-thread to avoid blocking tick speed
        double finalShutdownBonus = shutdownBonus;
        int finalNewStreak = newStreak;
        int finalVictimStreak = victimStreak;
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

                        if (killstreakEnabled) {
                            // Notify killer of their current streak
                            if (settings.killstreakSettings.showStreakChat) {
                                messageService.sendMessage(killer, "pvp.streak-active", Map.of("streak", String.valueOf(finalNewStreak)));
                            }

                            if (finalShutdownBonus > 0) {
                                messageService.broadcast("pvp.shutdown", Map.of(
                                    "killer", killer.getName(),
                                    "player", killer.getName(),
                                    "victim", victim.getName(),
                                    "streak", String.valueOf(finalVictimStreak),
                                    "bonus", String.format("%.2f", finalShutdownBonus)
                                ));
                            }
                        }
                    }
                }).execute();
            }
        }).execute();
    }

    private void handleDeathEffects(Player killer, Player victim, PluginConfig settings) {
        if (settings == null || settings.deathEffects == null) return;

        org.bukkit.Location deathLoc = victim.getLocation();
        PluginConfig.DeathEffects effects = settings.deathEffects;

        // 1. Process Lightning Effect
        if (effects.lightning != null && effects.lightning.enabled) {
            if ("RADIUS".equalsIgnoreCase(effects.lightning.rangeMode)) {
                double radiusSq = effects.lightning.radius * effects.lightning.radius;
                for (Player player : victim.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(deathLoc) <= radiusSq) {
                        messageService.playLightningEffect(player, deathLoc);
                    }
                }
            } else {
                // Default: KILLER_AND_VICTIM
                messageService.playLightningEffect(killer, deathLoc);
                messageService.playLightningEffect(victim, deathLoc);
            }
        } else if (settings.lightningKillEffect) {
            // Backward compatibility fallback
            messageService.playLightningEffect(killer, deathLoc);
        }

        // 2. Process Sound Effect
        if (effects.sound != null && effects.sound.enabled && effects.sound.type != null && !effects.sound.type.isEmpty()) {
            if ("RADIUS".equalsIgnoreCase(effects.sound.rangeMode)) {
                double radiusSq = effects.sound.radius * effects.sound.radius;
                for (Player player : victim.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(deathLoc) <= radiusSq) {
                        messageService.playSoundEffect(player, deathLoc, effects.sound.type, effects.sound.volume, effects.sound.pitch);
                    }
                }
            } else {
                // Default: KILLER_AND_VICTIM
                messageService.playSoundEffect(killer, deathLoc, effects.sound.type, effects.sound.volume, effects.sound.pitch);
                messageService.playSoundEffect(victim, deathLoc, effects.sound.type, effects.sound.volume, effects.sound.pitch);
            }
        }
    }
}
