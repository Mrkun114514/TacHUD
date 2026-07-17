package com.mrkun.tachud.neoforge;

import com.mrkun.tachud.TacHud;
import com.mrkun.tachud.client.TacHudClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * NeoForge main entrypoint. Runs the common init on both sides and the common
 * client init only on the physical client. The {@code Dist.CLIENT} guard keeps
 * {@link TacHudClient} from being class-loaded on a dedicated server.
 */
@Mod(TacHud.MOD_ID)
public final class TacHudNeoForge {

    public TacHudNeoForge(IEventBus modEventBus) {
        TacHud.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TacHudClient.init();
        }
    }
}
