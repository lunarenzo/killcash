package com.lunatech.killcash.config;

import com.lunatech.killcash.config.exception.ConfigValidationException;
import com.lunatech.killcash.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public class DeathMessagesConfig implements VersionedConfig {
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
        DEATH MESSAGES CONFIGURATION
        ============================
        Configure custom death announcements for PvP, PvE, and natural environmental deaths.
        Supports rich MiniMessage text styling.
        """)
    public boolean enabled = true;

    @Comment("""
        PVP DEATH FORMATS
        =================
        Randomly selected templates used when a player kills another player.
        Placeholders: <victim>, <killer>, <item>, <weapon_type>, <streak>
        """)
    public List<String> pvpFormats = List.of(
        "<red><victim> <gray>was sliced to pieces by <gold><killer> <gray>using <weapon_type>[<item>]",
        "<gold><killer> <gray>eliminated <red><victim> <gray>via <weapon_type>[<item>] <dark_gray>(Streak: <streak>)"
    );

    @Comment("Randomly selected templates used when a player kills another player with bare hands (unarmed).")
    public List<String> pvpFistFormats = List.of(
        "<red><victim> <gray>was beaten to a pulp by <gold><killer>'s <weapon_type>bare fists!"
    );

    @Comment("""
        MOB DEATH FORMATS
        =================
        Templates used when a player is killed by a mob.
        Keys are EntityType names (e.g. ZOMBIE, CREEPER, SKELETON) or DEFAULT.
        Each entry has 'weapon' formats (mob held an item) and 'unarmed' formats (melee/default).
        """)
    public Map<String, MobFormatGroup> mobFormats = Map.of(
        "CREEPER", new MobFormatGroup(
            List.of(),
            List.of("<red><victim> <gray>was blown to smithereens by <gold><killer>!")
        ),
        "DEFAULT", new MobFormatGroup(
            List.of("<red><victim> <gray>was slain by <gold><killer> <gray>using <weapon_type>[<item>]"),
            List.of("<red><victim> <gray>was shredded by <gold><killer>")
        )
    );

    @Comment("""
        ENVIRONMENTAL / NATURAL DEATHS
        ==============================
        Announcements used when a player dies of non-entity sources.
        Keys match standard Bukkit DamageCause names (e.g. FALL, LAVA, DROWNING, VOID) or DEFAULT.
        """)
    public Map<String, String> naturalFormats = Map.of(
        "FALL", "<red><victim> <gray>discovered that gravity works.",
        "LAVA", "<red><victim> <gray>tried to swim in lava.",
        "SUFFOCATION", "<red><victim> <gray>ran out of breathing room.",
        "DEFAULT", "<red><victim> <gray>died mysteriously."
    );

    @Comment("""
        WEAPON TYPE SYMBOLS/PREFIXES
        ============================
        Custom mappings of Material types to custom weapon prefixes or icons.
        Used via the <weapon_type> placeholder in death messages. DEFAULT is the fallback.
        """)
    public Map<String, String> weaponTypes = Map.ofEntries(
        Map.entry("DIAMOND_SWORD", "⚔ "),
        Map.entry("NETHERITE_SWORD", "⚔ "),
        Map.entry("IRON_SWORD", "⚔ "),
        Map.entry("STONE_SWORD", "⚔ "),
        Map.entry("WOODEN_SWORD", "⚔ "),
        Map.entry("GOLDEN_SWORD", "⚔ "),
        Map.entry("BOW", "🏹 "),
        Map.entry("CROSSBOW", "🏹 "),
        Map.entry("NETHERITE_AXE", "🪓 "),
        Map.entry("DIAMOND_AXE", "🪓 "),
        Map.entry("IRON_AXE", "🪓 "),
        Map.entry("STONE_AXE", "🪓 "),
        Map.entry("WOODEN_AXE", "🪓 "),
        Map.entry("GOLDEN_AXE", "🪓 "),
        Map.entry("AIR", "👊 "),
        Map.entry("DEFAULT", "")
    );

    @ConfigSerializable
    public static class MobFormatGroup {
        @Comment("Templates used when the mob is holding an item.")
        public List<String> weapon;

        @Comment("Templates used when the mob is unarmed.")
        public List<String> unarmed;

        public MobFormatGroup() {}

        public MobFormatGroup(List<String> weapon, List<String> unarmed) {
            this.weapon = weapon;
            this.unarmed = unarmed;
        }
    }
}
