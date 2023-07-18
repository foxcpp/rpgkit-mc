package com.github.sweetsnowywitch.rpgkit.client;

import com.github.clevernucleus.playerex.api.client.PageRegistry;
import com.github.sweetsnowywitch.rpgkit.RPGKitMod;
import com.github.sweetsnowywitch.rpgkit.client.gui.ClassPageLayer;
import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ClientRPGKitMod implements ClientModInitializer {
    public static final Gson GSON = new Gson();
    public static final Identifier RPGKIT_PAGE = new Identifier(RPGKitMod.MOD_ID, "rpgkit_page");

    @Override
    public void onInitializeClient() {

        RPGKitMod.DATA_SYNCER.setupClient();

        var isMagicDownloaded = false;
        for (var mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.toString().startsWith("rpgkit-magic")) {
                isMagicDownloaded = true;
                break;
            }
        }
        if (!isMagicDownloaded) {
            //PageRegistry.registerPage(RPGKIT_PAGE, new Identifier(RPGKitMod.MOD_ID, "textures/gui/icon_rpgkit.png"), Text.translatable("rpgkit.gui.page.rpgkit.title"));
            //PageRegistry.registerLayer(RPGKIT_PAGE, ClassPageLayer::new);
        }
    }
}
