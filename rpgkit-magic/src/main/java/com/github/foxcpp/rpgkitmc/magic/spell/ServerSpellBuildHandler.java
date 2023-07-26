package com.github.foxcpp.rpgkitmc.magic.spell;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Handles packets sent by client UI when performing a cast.
 */
public class ServerSpellBuildHandler {
    public static final Identifier PACKET_ID = new Identifier(RPGKitMagicMod.MOD_ID, "spell_builder");

    public enum Action {
        START,
        ADD_ELEMENT,
        SWITCH_BAG,
        CAST,
        INTERRUPT_CAST,
    }

    public enum CastType {
        SELF,
        ITEM,
        AREA,
        USE,
        USE_BLOCK,
        USE_ENTITY,
    }

    public void register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, this::onPacket);
    }

    private void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var value = buf.readString(25);
        var action = Action.valueOf(value);
        switch (action) {
            case START -> this.onStartSpellBuild(server, player, handler, buf, responseSender);
            case ADD_ELEMENT -> this.onAddElement(server, player, handler, buf, responseSender);
            case SWITCH_BAG -> this.onSwitchBag(server, player, handler, buf, responseSender);
            case CAST -> this.onCast(server, player, handler, buf, responseSender);
            case INTERRUPT_CAST -> this.onInterruptCast(server, player, handler, buf, responseSender);
            default -> RPGKitMagicMod.LOGGER.error("Unknown spell builder action received from {}", player);
        }
    }

    private void onStartSpellBuild(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            try {
                player.getComponent(ModComponents.CAST).startBuild();
            } catch (Exception ex) {
                RPGKitMagicMod.LOGGER.error("Exception happened while handling a spell build packet from %s".formatted(player.toString()), ex);
            }
        });
    }

    private void onAddElement(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var index = buf.readInt();

        server.execute(() -> {
            try {
                player.getComponent(ModComponents.CAST).addElement(index);
            } catch (Exception ex) {
                RPGKitMagicMod.LOGGER.error("Exception happened while handling a spell build packet from %s".formatted(player.toString()), ex);
            }
        });
    }

    private void onSwitchBag(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            try {
                player.getComponent(ModComponents.CAST).switchCatalystBag();
            } catch (Exception ex) {
                RPGKitMagicMod.LOGGER.error("Exception happened while handling a spell build packet from %s".formatted(player.toString()), ex);
            }
        });
    }

    private void onCast(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var type = CastType.valueOf(buf.readString(25));

        BlockPos pos;
        Direction direction;
        int entityId;

        if (type.equals(CastType.USE_BLOCK)) {
            pos = buf.readBlockPos();
            direction = Direction.byId(buf.readInt());
        } else {
            pos = null;
            direction = null;
        }
        if (type.equals(CastType.USE_ENTITY)) {
            entityId = buf.readInt();
        } else {
            entityId = 0;
        }

        server.execute(() -> {
            try {
                var comp = player.getComponent(ModComponents.CAST);
                switch (type) {
                    case SELF -> comp.performSelfCast();
                    case ITEM -> comp.performItemCast();
                    case AREA -> comp.performAreaCast();
                    case USE -> comp.performRangedCast();
                    case USE_BLOCK -> comp.performCastOnEntity(player.getWorld().getEntityById(entityId));
                    case USE_ENTITY -> comp.performCastOnBlock(pos, direction);
                }
            } catch (Exception ex) {
                RPGKitMagicMod.LOGGER.error("Exception happened while handling a spell build packet from %s".formatted(player.toString()), ex);
            }
        });
    }

    private void onBlockCast(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var type = CastType.valueOf(buf.readString(25));

        server.execute(() -> {
            try {
                var comp = player.getComponent(ModComponents.CAST);
                switch (type) {
                    case SELF -> comp.performSelfCast();
                    case ITEM -> comp.performItemCast();
                    case AREA -> comp.performAreaCast();
                    case USE -> comp.performRangedCast();
                }
            } catch (Exception ex) {
                RPGKitMagicMod.LOGGER.error("Exception happened while handling a spell build packet from %s".formatted(player.toString()), ex);
            }
        });
    }


    private void onInterruptCast(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            try {
                player.getComponent(ModComponents.CAST).interruptChanneling();
            } catch (Exception ex) {
                RPGKitMagicMod.LOGGER.error("Exception happened while handling a spell build packet from %s".formatted(player.toString()), ex);
            }
        });
    }
}
