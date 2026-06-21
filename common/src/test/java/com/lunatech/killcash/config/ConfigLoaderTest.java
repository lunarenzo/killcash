package com.lunatech.killcash.config;

import com.lunatech.killcash.config.exception.ConfigValidationException;
import com.lunatech.killcash.config.loading.ConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {

    @ConfigSerializable
    public static class TestConfig implements VersionedConfig {
        public int configVersion = 1;

        @Override
        public int configVersion() {
            return configVersion;
        }

        public String name = "DefaultName";
        public int value = 42;
        public boolean flag = true;
    }

    @Test
    public void testAutomaticConfigUpdating(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("config.yml");

        // 1. Write an incomplete configuration file (missing "flag" and "value" keys)
        String initialContent = "config-version: 1\nname: CustomName\n";
        Files.writeString(configFile, initialContent);

        // 2. Load it via ConfigLoader
        ConfigLoader loader = new ConfigLoader()
                .withPath(configFile);
        TestConfig loadedConfig = loader.buildOrThrow(TestConfig.class);

        // 3. Assert values are correct (custom values preserved, missing keys get defaults)
        assertEquals("CustomName", loadedConfig.name);
        assertEquals(42, loadedConfig.value);
        assertTrue(loadedConfig.flag);

        // 4. Verify that the file on disk was automatically updated (since defaults were copied)
        String updatedContent = Files.readString(configFile);
        assertTrue(updatedContent.contains("flag: true"));
        assertTrue(updatedContent.contains("value: 42"));
        assertTrue(updatedContent.contains("name: CustomName"));
    }

    @Test
    public void testNoWriteIfNoChange(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("config.yml");

        // 1. Write a complete configuration file (all keys present)
        String initialContent = "config-version: 1\nname: CustomName\nvalue: 100\nflag: false\n";
        Files.writeString(configFile, initialContent);

        // 2. Load it via ConfigLoader
        ConfigLoader loader = new ConfigLoader()
                .withPath(configFile);
        TestConfig loadedConfig = loader.buildOrThrow(TestConfig.class);

        assertEquals("CustomName", loadedConfig.name);
        assertEquals(100, loadedConfig.value);
        assertFalse(loadedConfig.flag);

        // 3. Since there were no missing keys/defaults copied, and no migration, the file shouldn't be re-written
        String currentContent = Files.readString(configFile);
        assertEquals("config-version: 1\nname: CustomName\nvalue: 100\nflag: false\n", currentContent);
    }
}
