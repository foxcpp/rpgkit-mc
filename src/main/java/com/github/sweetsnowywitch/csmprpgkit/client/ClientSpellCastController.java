package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ClientSpellCastController implements SpellCastController {
    public static final Identifier PACKET_ID = ServerSpellBuildHandler.PACKET_ID;

    public void startBuild() {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.START.name());
        ClientPlayNetworking.send(PACKET_ID, buf);
    }

    public void addElement(int index) {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.ADD_ELEMENT.name());
        buf.writeInt(index);
        ClientPlayNetworking.send(PACKET_ID, buf);
    }

    public void switchCatalystBag() {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.SWITCH_BAG.name());
        ClientPlayNetworking.send(PACKET_ID, buf);
    }

    public void performSelfCast() {
        this.performCast(ServerSpellBuildHandler.CastType.SELF);
    }

    public void performAreaCast() {
        this.performCast(ServerSpellBuildHandler.CastType.AREA);
    }

    public void performItemCast() {
        this.performCast(ServerSpellBuildHandler.CastType.ITEM);
    }

    public void performUseCast() {
        this.performCast(ServerSpellBuildHandler.CastType.USE);
    }

    public void interruptChanneling() {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.INTERRUPT_CAST.name());
        ClientPlayNetworking.send(PACKET_ID, buf);
    }

    private void performCast(ServerSpellBuildHandler.CastType type) {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.CAST.name());
        buf.writeString(type.name());
        ClientPlayNetworking.send(PACKET_ID, buf);
    }
}
