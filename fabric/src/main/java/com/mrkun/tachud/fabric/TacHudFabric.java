package com.mrkun.tachud.fabric;

import com.mrkun.tachud.TacHud;
import net.fabricmc.api.ModInitializer;

/** Fabric main entrypoint. Delegates all logic to the common initializer. */
public final class TacHudFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TacHud.init();
    }
}
