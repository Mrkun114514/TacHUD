package com.mrkun.tachud.fabric.client;

import com.mrkun.tachud.client.gui.TacHudConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

/**
 * Mod Menu integration — adds a "配置" button in the Mod Menu mod list
 * that opens our settings GUI. Only loaded when Mod Menu is installed.
 */
public final class TacHudModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> new TacHudConfigScreen(parent);
    }
}
