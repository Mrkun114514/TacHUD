package com.mrkun.tachud.client;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays TacHUD's hit / kill sound effects from the client thread.
 *
 * <p>Sounds are referenced by a {@code <namespace>:<path>} ResourceLocation, so
 * the same code plays both vanilla sounds (e.g. {@code minecraft:block.note_block.hat},
 * which need no assets) and any future custom mod sound registered in a
 * {@code sounds.json}. The latter is resolved via
 * {@link SoundEvent#createFixedRangeEvent}; a vanilla sound is fetched from the
 * registry when already registered.
 */
public final class ClientSound {

    private static final Logger LOGGER = LoggerFactory.getLogger("TacHUD");

    private ClientSound() {
    }

    public static void playHit(boolean fatal) {
        TacHudConfig cfg = ConfigManager.get();
        if (!cfg.masterEnabled || !cfg.hitSound.enabled) {
            return;
        }
        String loc = (fatal && cfg.hitSound.killDistinct)
                ? cfg.hitSound.killSound
                : cfg.hitSound.sound;
        play(loc, cfg.hitSound.volume, cfg.hitSound.pitch);
    }

    public static void playKill() {
        TacHudConfig cfg = ConfigManager.get();
        if (!cfg.masterEnabled || !cfg.hitSound.enabled || !cfg.hitSound.killDistinct) {
            return;
        }
        // Slightly lower pitch on the kill flourish for a meatier thump.
        play(cfg.hitSound.killSound, cfg.hitSound.volume, cfg.hitSound.pitch * 0.85f);
    }

    private static void play(String loc, float volume, float pitch) {
        if (loc == null || loc.isBlank()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) {
            return;
        }
        int sep = loc.indexOf(':');
        String ns = sep >= 0 ? loc.substring(0, sep) : "minecraft";
        String path = sep >= 0 ? loc.substring(sep + 1) : loc;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ns, path);

        // Build a SoundEvent straight from the location. For vanilla sounds the
        // sound manager already knows the definition; for a future custom
        // mod sound it is resolved from its sounds.json entry. No registry
        // lookup needed, so the exact same code works on both loaders.
        SoundEvent evt = SoundEvent.createFixedRangeEvent(id, 16.0f);
        try {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(evt, volume, pitch));
        } catch (Exception e) {
            LOGGER.debug("[TacHUD] failed to play sound {}", id, e);
        }
    }
}
