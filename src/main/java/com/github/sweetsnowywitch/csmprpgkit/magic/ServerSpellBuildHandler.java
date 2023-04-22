package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.items.CatalystBagItem;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles packets sent by client UI when performing a cast.
 */
public class ServerSpellBuildHandler implements ServerLivingEntityEvents.AfterDeath, ServerPlayConnectionEvents.Disconnect {
    private final Map<UUID, SpellBuilder> builders = new HashMap<>();
    public static final Identifier PACKET_ID = new Identifier(RPGKitMod.MOD_ID, "spell_builder");

    public enum Action {
        START,
        ADD_ASPECT,
        ADD_ITEM_STACK,
        FINISH_SPELL,
        FINISH_REACTION,
        CAST,
    }

    public boolean isActive(@NotNull PlayerEntity ent) {
        return builders.containsKey(ent.getUuid());
    }

    public void register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, this::onPacket);
    }

    private void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var value = buf.readString(25);
        var action = Action.valueOf(value);
        switch (action) {
            case START -> this.onStartSpellBuild(server, player, handler, buf, responseSender);
            case ADD_ASPECT -> this.onAddAspect(server, player, handler, buf, responseSender);
            case ADD_ITEM_STACK -> this.onAddItemStack(server, player, handler, buf, responseSender);
            case FINISH_SPELL -> this.onFinishSpell(server, player, handler, buf, responseSender);
            case FINISH_REACTION -> this.onFinishReaction(server, player, handler, buf, responseSender);
            case CAST -> this.onCast(server, player, handler, buf, responseSender);
            default -> RPGKitMod.LOGGER.error("Unknown spell builder action received from {}", player);
        }
    }

    private void onStartSpellBuild(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (!player.getMainHandStack().isEmpty()) {
            player.sendMessage(Text.translatable("csmprpgkit.magic.must_have_empty_hand"), true);
            return;
        }

        player.getInventory().setStack(player.getInventory().selectedSlot, new ItemStack(ModItems.SPELL_ITEM));

        builders.put(player.getUuid(), new SpellBuilder(5));
    }

    private void onAddAspect(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var aspectID = buf.readIdentifier();

        server.execute(() -> {
            var aspect = ModRegistries.ASPECTS.get(aspectID);
            if (aspect == null) {
                RPGKitMod.LOGGER.error("Unknown aspect ID received from {}: {}", player, aspectID);
                return;
            }
            var builder = builders.get(player.getUuid());
            if (builder == null) {
                RPGKitMod.LOGGER.error("onAddAspect called for player {} that is not building a spell", player);
                return;
            }

            if (player.getMainHandStack().getItem().equals(ModItems.SPELL_ITEM)) {
                player.getMainHandStack().setSubNbt("Color", NbtInt.of(SpellElement.calculateBaseColor(builder.getFullRecipe())));
            }

            builder.addElement(aspect);
        });
    }

    private void onAddItemStack(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var targetStack = buf.readItemStack();

        server.execute(() -> {
            var slot = player.getInventory().getSlotWithStack(new ItemStack(ModItems.CATALYST_BAG));
            if (slot == -1) {
                RPGKitMod.LOGGER.error("onAddAspect called for player {} without a catalyst bag in inventory", player);
                return;
            }
            var bagStack = player.getInventory().getStack(slot);

            var builder = builders.get(player.getUuid());
            if (builder == null) {
                RPGKitMod.LOGGER.error("onAddAspect called for player {} that is not building a spell", player);
                return;
            }

            var inv = CatalystBagItem.getInventory(bagStack);
            for (int i = 0; i < inv.size(); i++) {
                if (!inv.getStack(i).isEmpty() && ItemStack.canCombine(inv.getStack(i), targetStack)) {
                    builder.addElement(new ItemElement.Stack(inv.getStack(i), inv));
                    break;
                }
            }
        });
    }

    private void onFinishSpell(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            var builder = builders.get(player.getUuid());
            if (builder == null) {
                RPGKitMod.LOGGER.error("onFinishSpell called for player {} that is not building a spell", player);
                return;
            }

            builder.finishSpell();
        });
    }

    private void onFinishReaction(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            var builder = builders.get(player.getUuid());
            if (builder == null) {
                RPGKitMod.LOGGER.error("onFinishSpell called for player {} that is not building a spell", player);
                return;
            }

            builder.finishReaction();
        });
    }

    private void onCast(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var formID = buf.readIdentifier();

        var form = ModRegistries.SPELL_FORMS.get(formID);
        if (form == null) {
            RPGKitMod.LOGGER.error("onCast called for player {} with unknown form ID {}", player, formID);
            return;
        }

        server.execute(() -> {
            var builder = builders.remove(player.getUuid());
            if (builder == null) {
                RPGKitMod.LOGGER.error("onCast called for player {} that is not building a spell", player);
                return;
            }

            if (player.getMainHandStack().getItem().equals(ModItems.SPELL_ITEM)) {
                player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
            }

            builder.toServerCast(player, form).perform(player.getWorld());
        });
    }

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayerEntity spe) {
            if (builders.remove(spe.getUuid()) != null) {
                RPGKitMod.LOGGER.info("Incomplete spell cast aborted due to player {} death", spe);
            }
        }
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        if (handler.player != null) {
            if (builders.remove(handler.player.getUuid()) != null) {
                RPGKitMod.LOGGER.info("Incomplete spell cast aborted due to player {} disconnect", handler.player);
            }
        }
    }
}
