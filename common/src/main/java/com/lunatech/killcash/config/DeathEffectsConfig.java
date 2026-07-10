package com.lunatech.killcash.config;

import com.lunatech.killcash.config.exception.ConfigValidationException;
import com.lunatech.killcash.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

@ConfigSerializable
public class DeathEffectsConfig implements VersionedConfig {
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
        DEATH EFFECTS CONFIGURATION
        ===========================
        Configure visual and audio effects that trigger when a player dies in PvP.
        All effects are client-side packet-based to ensure zero server-side lag.
        """)
    public LightningSettings lightning = new LightningSettings();

    public SoundSettings sound = new SoundSettings();

    @ConfigSerializable
    public static class LightningSettings {
        @Comment("Enable packet-based client-side lightning strike effect on player death.")
        public boolean enabled = true;

        @Comment("""
            Who should see the lightning strike effect.
            Options:
            - KILLER_AND_VICTIM: Private effect visible only to the killer and victim.
            - RADIUS: Broadcast to all players within a certain distance.
            """)
        public String rangeMode = "KILLER_AND_VICTIM";

        @Comment("Radius (in blocks) to show the lightning strike if rangeMode is set to RADIUS.")
        public double radius = 32.0;
    }

    @ConfigSerializable
    public static class SoundSettings {
        @Comment("Enable custom sound effect played on player death.")
        public boolean enabled = true;

        @Comment("""
            The Bukkit Sound enum name or sound resource key to play (e.g. ENTITY_LIGHTNING_BOLT_THUNDER).
            Valid Bukkit sounds: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html
            """)
        public String type = "ENTITY_LIGHTNING_BOLT_THUNDER";

        @Comment("""
            Who should hear the sound.
            Options:
            - KILLER_AND_VICTIM: Private sound audible only to the killer and victim.
            - RADIUS: Broadcast to all players within a certain distance.
            """)
        public String rangeMode = "KILLER_AND_VICTIM";

        @Comment("Radius (in blocks) to play the sound if rangeMode is set to RADIUS.")
        public double radius = 32.0;

        @Comment("Sound volume (distance multiplier). A value of 1.0 = 16 blocks radius. High values increase audibility range.")
        public float volume = 1.0f;

        @Comment("Sound pitch multiplier (0.5 to 2.0). Default is 1.0.")
        public float pitch = 1.0f;
    }
}
