package com.github.sweetsnowywitch.rpgkit.client;

import com.github.sweetsnowywitch.rpgkit.RPGKitMod;
import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientRPGKitMod implements ClientModInitializer {
    public static final Gson GSON = new Gson();

    @Override
    public void onInitializeClient() {

        RPGKitMod.DATA_SYNCER.setupClient();
    }
}
