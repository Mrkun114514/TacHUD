package com.mrkun.tachud.client;

import com.mrkun.tachud.client.hud.HitMarkerOverlay;
import com.mrkun.tachud.client.hud.KillFeedOverlay;
import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import com.mrkun.tachud.net.HitPayload;
import com.mrkun.tachud.net.KillPayload;

/**
 * Client-thread handlers for TacHUD's server -> client packets. These are only
 * ever invoked from inside {@code context.queue(...)}, i.e. on the render/client
 * thread, so touching client-only overlay state here is safe.
 */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    public static void onKill(KillPayload payload) {
        TacHudConfig cfg = ConfigManager.get();
        if (!cfg.masterEnabled || !cfg.killFeed.enabled) {
            return;
        }
        if (!payload.victimIsPlayer() && !cfg.killFeed.showMobKills) {
            return;
        }
        KillFeedOverlay.addKill(payload.victimName(), payload.victimIsPlayer());
    }

    public static void onHit(HitPayload payload) {
        TacHudConfig cfg = ConfigManager.get();
        if (!cfg.masterEnabled || !cfg.hitMarker.enabled) {
            return;
        }
        HitMarkerOverlay.trigger(payload.fatal());
    }
}
