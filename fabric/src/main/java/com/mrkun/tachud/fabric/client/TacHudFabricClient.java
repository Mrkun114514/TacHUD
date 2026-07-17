package com.mrkun.tachud.fabric.client;

import com.mrkun.tachud.client.TacHudClient;
import net.fabricmc.api.ClientModInitializer;

/** Fabric client entrypoint. Delegates to the common client initializer. */
public final class TacHudFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TacHudClient.init();
    }
}
