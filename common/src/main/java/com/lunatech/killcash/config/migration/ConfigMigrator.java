package com.lunatech.killcash.config.migration;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust, future-proof configuration migrator to handle splitting/decoupling config blocks across different files.
 */
public final class ConfigMigrator {
    private final Path configDir;
    private final Logger logger;
    private final List<Rule> rules = new ArrayList<>();

    public record Rule(
        String sourceFile,
        String sourceKey,
        String destFile,
        boolean overwrite
    ) {}

    public ConfigMigrator(Path configDir, Logger logger) {
        this.configDir = configDir;
        this.logger = logger;
    }

    public ConfigMigrator addRule(String sourceFile, String sourceKey, String destFile, boolean overwrite) {
        rules.add(new Rule(sourceFile, sourceKey, destFile, overwrite));
        return this;
    }

    public void migrate() {
        for (Rule rule : rules) {
            Path sourcePath = configDir.resolve(rule.sourceFile);
            Path destPath = configDir.resolve(rule.destFile);

            File sourceFile = sourcePath.toFile();
            File destFile = destPath.toFile();

            if (!sourceFile.exists()) {
                continue;
            }

            // Only migrate if we allow overwriting or if the destination file does not exist yet
            if (destFile.exists() && !rule.overwrite) {
                continue;
            }

            try {
                YamlConfigurationLoader sourceLoader = YamlConfigurationLoader.builder().path(sourcePath).build();
                CommentedConfigurationNode sourceRoot = sourceLoader.load();
                if (sourceRoot == null || sourceRoot.isNull()) {
                    continue;
                }

                CommentedConfigurationNode targetNode = sourceRoot.node(rule.sourceKey);
                if (targetNode.virtual() || targetNode.isNull()) {
                    continue;
                }

                YamlConfigurationLoader destLoader = YamlConfigurationLoader.builder().path(destPath).build();
                CommentedConfigurationNode destRoot = destLoader.createNode();
                
                // Copy the node content to destination root
                destRoot.set(targetNode);
                destLoader.save(destRoot);

                // Remove from source and save source
                sourceRoot.removeChild(rule.sourceKey);
                sourceLoader.save(sourceRoot);

                logger.info("Successfully migrated configuration section '{}' from '{}' to '{}'.", 
                    rule.sourceKey, rule.sourceFile, rule.destFile);
            } catch (Exception e) {
                logger.error("Failed to migrate configuration section '{}' from '{}' to '{}': {}", 
                    rule.sourceKey, rule.sourceFile, rule.destFile, e.getMessage(), e);
            }
        }
    }
}
