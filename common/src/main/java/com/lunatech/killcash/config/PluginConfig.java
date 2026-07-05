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

        @Comment("Enable client-side lightning strike visual effect for the killer when they kill a player")
        public boolean lightningKillEffect = true;

        @Comment("Killstreak settings")
        public KillstreakSettings killstreakSettings = new KillstreakSettings();

        @ConfigSerializable
        public static class KillstreakSettings {
            @Comment("Enable or disable the overall killstreak system")
            public boolean enabled = true;

            @Comment("If true, streaks apply to the next bracket up (e.g., streak of 4 gets 3's multiplier)")
            public boolean useRangeSystem = true;

            @Comment("Time in seconds before a streak starts resetting due to inactivity")
            public long decayTime = 60;

            @Comment("Cash rewarded per streak level to the person who ends the streak")
            public double shutdownBonusPerKill = 25.0;

            @Comment("Whether to send action bar messages showing active streak and multiplier")
            public boolean showActionBar = true;

            @Comment("Whether to send personal chat messages notifying players of their active streak")
            public boolean showStreakChat = true;
        }

        @Comment("Server-wide announcements for major killstreak milestones.")
        public Map<String, String> streakAnnouncements = Map.of(
            "5", "<red><bold>[KILLSTREAK]</bold></red> <yellow><player> is on a <gold>5 Kill Streak</gold>!</yellow>",
            "10", "<dark_red><bold>[RAMPAGE]</bold></dark_red> <yellow><player> is unstoppable with a <red>10 Kill Streak</red>!</yellow>"
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

    @Comment("Currency Conversion Settings")
    public ConversionSettings conversionSettings = new ConversionSettings();

    @ConfigSerializable
    public static class ConversionSettings {
        @Comment("Enable or disable currency conversion from KillCash to main server economy")
        public boolean enabled = true;

        @Comment("Exchange rate: how many server economy dollars you get per 1 KillCash token")
        public double exchangeRate = 100.0;

        @Comment("Minimum amount of KillCash a player can convert at once")
        public double minimumConversion = 1.0;
    }

    @Comment("Storage Settings")
    public StorageSettings storage = new StorageSettings();

    @ConfigSerializable
    public static class StorageSettings {
        @Comment("Storage backend to use for player balances. Options: PDC (standard vanilla player NBT files), DATABASE (SQLite/MySQL database). Default: PDC")
        public String backend = "PDC";
    }

    @Comment("Death Message Settings")
    public DeathMessages deathMessages = new DeathMessages();

    @ConfigSerializable
    public static class DeathMessages {
        @Comment("Enable or disable custom death messages")
        public boolean enabled = true;

        @Comment("When a player kills another player (PVP)")
        public java.util.List<String> pvpFormats = java.util.List.of(
            "<red><victim> <gray>was sliced to pieces by <gold><killer> <gray>using <weapon_type>[<item>]",
            "<gold><killer> <gray>eliminated <red><victim> <gray>via <weapon_type>[<item>] <dark_gray>(Streak: <streak>)"
        );

        @Comment("When a player kills another player using their bare hands")
        public java.util.List<String> pvpFistFormats = java.util.List.of(
            "<red><victim> <gray>was beaten to a pulp by <gold><killer>'s <weapon_type>bare fists!"
        );

        @Comment("Mob-specific death messages. Keys are EntityType names (e.g. CREEPER, ZOMBIE, SKELETON) or DEFAULT. " +
                 "Formats are divided into 'weapon' (used when the mob holds an item) and 'unarmed' (used when the mob is unarmed).")
        public java.util.Map<String, MobFormatGroup> mobFormats = java.util.Map.of(
            "CREEPER", new MobFormatGroup(
                java.util.List.of(),
                java.util.List.of("<red><victim> <gray>was blown to smithereens by <gold><killer>!")
            ),
            "DEFAULT", new MobFormatGroup(
                java.util.List.of("<red><victim> <gray>was slain by <gold><killer> <gray>using <weapon_type>[<item>]"),
                java.util.List.of("<red><victim> <gray>was shredded by <gold><killer>")
            )
        );

        @Comment("Natural/Environmental deaths (e.g. FALL, LAVA, VOID, DROWNING, etc.)")
        public java.util.Map<String, String> naturalFormats = java.util.Map.of(
            "FALL", "<red><victim> <gray>discovered that gravity works.",
            "LAVA", "<red><victim> <gray>tried to swim in lava.",
            "SUFFOCATION", "<red><victim> <gray>ran out of breathing room.",
            "DEFAULT", "<red><victim> <gray>died mysteriously."
        );

        @Comment("Custom mappings of Material types to custom weapon type prefixes/icons. " +
                 "Used via the <weapon_type> placeholder in death messages. DEFAULT is the fallback.")
        public java.util.Map<String, String> weaponTypes = java.util.Map.of(
            "DIAMOND_SWORD", "⚔ ",
            "NETHERITE_SWORD", "⚔ ",
            "IRON_SWORD", "⚔ ",
            "STONE_SWORD", "⚔ ",
            "WOODEN_SWORD", "⚔ ",
            "GOLDEN_SWORD", "⚔ ",
            "BOW", "🏹 ",
            "CROSSBOW", "🏹 ",
            "NETHERITE_AXE", "🪓 ",
            "DIAMOND_AXE", "🪓 ",
            "IRON_AXE", "🪓 ",
            "STONE_AXE", "🪓 ",
            "WOODEN_AXE", "🪓 ",
            "GOLDEN_AXE", "🪓 ",
            "AIR", "👊 ",
            "DEFAULT", ""
        );
    }

    @ConfigSerializable
    public static class MobFormatGroup {
        @Comment("Templates used when the mob is holding an item")
        public java.util.List<String> weapon;

        @Comment("Templates used when the mob is unarmed")
        public java.util.List<String> unarmed;

        public MobFormatGroup() {}

        public MobFormatGroup(java.util.List<String> weapon, java.util.List<String> unarmed) {
            this.weapon = weapon;
            this.unarmed = unarmed;
        }
    }
}
