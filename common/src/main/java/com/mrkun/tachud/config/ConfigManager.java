package com.mrkun.tachud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrkun.tachud.TacHud;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves {@link TacHudConfig} to {@code config/tachud.json}.
 *
 * <p>Deliberately dependency-light: it uses the Gson bundled with Minecraft and
 * Architectury's platform config folder, so the exact same code path runs on
 * both Fabric and NeoForge with no loader-specific config library.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "tachud.json";

    private static volatile TacHudConfig config = new TacHudConfig();

    private ConfigManager() {
    }

    public static TacHudConfig get() {
        return config;
    }

    private static Path configPath() {
        return Platform.getConfigFolder().resolve(FILE_NAME);
    }

    /** Load from disk, creating a default file if none exists. */
    public static synchronized void load() {
        Path path = configPath();
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                TacHudConfig loaded = GSON.fromJson(json, TacHudConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
                // Re-save to backfill any newly added fields with their defaults.
                save();
                TacHud.LOGGER.info("[{}] config loaded from {}", TacHud.MOD_NAME, path);
            } else {
                config = new TacHudConfig();
                save();
                TacHud.LOGGER.info("[{}] default config written to {}", TacHud.MOD_NAME, path);
            }
        } catch (Exception e) {
            TacHud.LOGGER.error("[{}] failed to load config, using defaults", TacHud.MOD_NAME, e);
            config = new TacHudConfig();
        }
    }

    public static synchronized void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            TacHud.LOGGER.error("[{}] failed to save config", TacHud.MOD_NAME, e);
        }
    }
}
