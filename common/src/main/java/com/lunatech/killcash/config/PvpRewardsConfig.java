package com.lunatech.killcash.config;

import com.lunatech.killcash.config.exception.ConfigValidationException;
import com.lunatech.killcash.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

@ConfigSerializable
public class PvpRewardsConfig implements VersionedConfig {
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

    @Comment("""
        PVP REWARD SETTINGS
        ===================
        Configure the cash rewards players receive when they kill another player.
        This includes basic cash reward range, multipliers, and anti-abuse checks.
        """)
    public boolean enabled = true;

    @Comment("The minimum base cash amount players receive for a kill.")
    public double minReward = 10.0;

    @Comment("The maximum base cash amount players receive for a kill.")
    public double maxReward = 20.0;

    @Comment("If set to true, the calculated reward amount will be rounded to the nearest integer value.")
    public boolean wholeNumberOnly = false;

    @Comment("""
        PERMISSION MULTIPLIERS
        ======================
        Apply cash multipliers based on permissions (e.g. for VIP/MVP ranks).
        If a player has multiple permissions, the highest multiplier will be applied.
        Format: "permission.node": multiplier_value
        """)
    public Map<String, Double> permissionMultipliers = Map.of(
        "killcash.multiplier.vip", 1.5,
        "killcash.multiplier.mvp", 2.0
    );

    @Comment("""
        STREAK MULTIPLIERS
        ==================
        Compound multipliers applied to cash payouts when players build a killstreak.
        Format: "kills": multiplier_value
        """)
    public Map<String, Double> streakMultipliers = Map.of(
        "3", 1.1,
        "5", 1.25,
        "10", 1.5
    );

    @Comment("Killstreak system properties.")
    public KillstreakSettings killstreakSettings = new KillstreakSettings();

    @ConfigSerializable
    public static class KillstreakSettings {
        @Comment("""
            Enable or disable the overall killstreak tracking system.
            If disabled, streaks and their bonuses/announcements will not trigger.
            """)
        public boolean enabled = true;

        @Comment("""
            If true, the streak multiplier applies to all kills within that bracket range.
            For example, a streak of 4 will get the multiplier configured for 3, until they reach 5.
            If false, the multiplier is only applied exactly when the streak count matches.
            """)
        public boolean useRangeSystem = true;

        @Comment("Time in seconds a player has to get another kill before their streak resets due to inactivity.")
        public long decayTime = 60;

        @Comment("Cash awarded to a player when they end another player's active killstreak (bonus per level).")
        public double shutdownBonusPerKill = 25.0;

        @Comment("Send temporary action bar notifications to the killer detailing their current streak & active multiplier.")
        public boolean showActionBar = true;

        @Comment("Send chat notifications directly to the player when their streak changes or is updated.")
        public boolean showStreakChat = true;

        @Comment("Personal audio cues played to the killer when their streak level increments.")
        public StreakSound incrementSound = new StreakSound(true, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f);

        @Comment("Broadcast audio cue played to all players when a major milestone is reached.")
        public StreakSound milestoneSound = new StreakSound(true, "ENTITY_LIGHTNING_BOLT_THUNDER", 1.0f, 1.0f);
    }

    @Comment("""
        STREAK MILESTONES ANNOUNCEMENTS
        ==============================
        Custom chat announcements broadcast to the entire server when a player reaches a milestone.
        Format: "kills": "announcement message"
        """)
    public Map<String, String> streakAnnouncements = Map.of(
        "5", "<red><bold>[KILLSTREAK]</bold></red> <yellow><player> is on a <gold>5 Kill Streak</gold>!</yellow>",
        "10", "<dark_red><bold>[RAMPAGE]</bold></dark_red> <yellow><player> is unstoppable with a <red>10 Kill Streak</red>!</yellow>"
    );

    @Comment("Settings to prevent abuse and farming.")
    public AntiAbuse antiAbuse = new AntiAbuse();

    @ConfigSerializable
    public static class AntiAbuse {
        @Comment("Prevent rewards if the killer and victim share the same IP address.")
        public boolean ipCheck = true;

        @Comment("Cooldown period (in seconds) during which a killer cannot earn rewards for killing the same victim again.")
        public long killCooldown = 600;

        @Comment("Minimum total playtime (in seconds) the victim must have to prevent users from farming new alt accounts.")
        public long minPlaytime = 300;
    }

    @ConfigSerializable
    public static class StreakSound {
        @Comment("Enable this sound effect.")
        public boolean enabled = true;

        @Comment("The Bukkit Sound enum name or namespace key (e.g. ENTITY_PLAYER_LEVELUP).")
        public String type = "";

        @Comment("Sound volume.")
        public float volume = 1.0f;

        @Comment("Sound pitch.")
        public float pitch = 1.0f;

        public StreakSound() {}

        public StreakSound(boolean enabled, String type, float volume, float pitch) {
            this.enabled = enabled;
            this.type = type;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
