package com.lunatech.killcash.config;

import com.lunatech.killcash.config.exception.ConfigValidationException;
import com.lunatech.killcash.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

@ConfigSerializable
public class PluginConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    @Exclude
    public int configVersion() {
        return configVersion;
    }

    @Override
    @Exclude
    public @NotNull Map<Integer, Migration> migrations() {
        return Map.of();
    }

    @Override
    @Exclude
    public void validate() throws ConfigValidationException {
    }

    @Comment("Update Checker Settings")
    public UpdateChecker updateChecker = new UpdateChecker();

    @ConfigSerializable
    public static class UpdateChecker {
        @Comment("Should the plugin check for plugin updates on startup?")
        public boolean enabled = true;

        @Comment("Send update notifications to the console?")
        public boolean console = true;

        @Comment("Send update notifications to opped players on join?")
        public boolean op = true;
    }

    @Comment("Language, specify the language file to use, for example `en_US` which will load `/lang/en_US.json`")
    public String language = "en_US";

    @Comment("PvP Reward Settings")
    public PvpReward pvpReward = new PvpReward();

    @ConfigSerializable
    public static class PvpReward {
        @Comment("Enable or disable PvP cash rewards")
        public boolean enabled = true;

        @Comment("Minimum cash reward per kill")
        public double minReward = 10.0;

        @Comment("Maximum cash reward per kill")
        public double maxReward = 20.0;

        @Comment("Permission-based multipliers. The player receives the highest multiplier they have permission for.")
        public Map<String, Double> permissionMultipliers = Map.of(
            "killcash.multiplier.vip", 1.5,
            "killcash.multiplier.mvp", 2.0
        );

        @Comment("Streak-based multipliers. Compounding multiplier applied when the player reaches a specific killstreak.")
        public Map<String, Double> streakMultipliers = Map.of(
            "3", 1.1,
            "5", 1.25,
            "10", 1.5
        );

        @Comment("Anti-Abuse Settings")
        public AntiAbuse antiAbuse = new AntiAbuse();

        @ConfigSerializable
        public static class AntiAbuse {
            @Comment("Prevent rewards if the killer and victim share the same IP address")
            public boolean ipCheck = true;

            @Comment("Cooldown (in seconds) between rewards for killing the same player")
            public long killCooldown = 600; // 10 minutes

            @Comment("Minimum playtime (in seconds) the victim must have to be eligible for rewards")
            public long minPlaytime = 300; // 5 minutes
        }
    }
}
