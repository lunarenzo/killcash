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
    @Comment("Do not change this value! Internal configuration version tracker.")
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
        UPDATE CHECKER SETTINGS
        =======================
        Control whether the plugin checks for updates and alerts administrators.
        Checks are run asynchronously on startup and join.
        """)
    public UpdateChecker updateChecker = new UpdateChecker();

    @ConfigSerializable
    public static class UpdateChecker {
        @Comment("Should the plugin check for newer versions on GitHub?")
        public boolean enabled = true;

        @Comment("Print update notifications to the server console log?")
        public boolean console = true;

        @Comment("Notify operators/admin players when they join the server?")
        public boolean op = true;
    }

    @Comment("""
        LANGUAGE SETTING
        ================
        Choose the locale/language file used for messages and commands.
        Example: 'en_US' loads resources/lang/en_US.json or plugins/KillCash/lang/en_US.json.
        """)
    public String language = "en_US";

    @Comment("""
        CURRENCY CONVERSION SETTINGS
        ============================
        Configure token exchange settings from KillCash tokens to server economy (Vault).
        Allows players to run '/killcash convert <amount>' to exchange tokens for server cash.
        """)
    public ConversionSettings conversionSettings = new ConversionSettings();

    @ConfigSerializable
    public static class ConversionSettings {
        @Comment("Allow players to convert their KillCash balance into main server money.")
        public boolean enabled = true;

        @Comment("Exchange rate: how many server economy dollars (Vault) are awarded per 1.0 KillCash token.")
        public double exchangeRate = 100.0;

        @Comment("Minimum amount of KillCash tokens required to perform a single conversion.")
        public double minimumConversion = 1.0;
    }

    @Comment("""
        STORAGE SETTINGS
        ================
        Choose where player balances and transaction data are stored.
        Available Options:
        - PDC: Store inside standard Vanilla player NBT files (PersistentDataContainer). Simple, zero setup, ideal for single servers.
        - DATABASE: Store in SQL Cache database (SQLite/MySQL/MariaDB/PostgreSQL/H2). Supports cross-server sync.
        """)
    public StorageSettings storage = new StorageSettings();

    @ConfigSerializable
    public static class StorageSettings {
        @Comment("The storage backend to use for balances. Options: PDC, DATABASE.")
        public String backend = "PDC";
    }
}
