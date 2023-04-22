package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.events.DataRegistryReloadCallback;
import com.github.sweetsnowywitch.csmprpgkit.magic.*;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientSpellBuildHandler implements DataRegistryReloadCallback, ClientTickEvents.EndTick {
    public static final Identifier PACKET_ID = ServerSpellBuildHandler.PACKET_ID;
    private final MinecraftClient client;
    private @Nullable InterceptableKeyboard keyboard = null;

    // Local copy of the builder, that mirrors actions sent to the server.
    private @Nullable SpellBuilder builder;
    private List<Aspect> primaryEffectAspects;

    public ClientSpellBuildHandler() {
        this.client = MinecraftClient.getInstance();
        this.primaryEffectAspects = List.of();
    }

    public List<Aspect> getPrimaryEffectAspects() {
        return primaryEffectAspects;
    }

    public @Nullable SpellBuilder getBuilder() {
        return builder;
    }

    public void start() {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        assert client.player != null;
        if (!client.player.getMainHandStack().isEmpty()) {
            client.player.sendMessage(Text.translatable("csmprpgkit.magic.must_have_empty_hand"), true);
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.START.name());

        ClientPlayNetworking.send(PACKET_ID, buf);
        this.builder = new SpellBuilder(5);
    }

    public void addAspect(Aspect aspect) {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }
        if (builder == null) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.ADD_ASPECT.name());
        buf.writeIdentifier(aspect.id);
        ClientPlayNetworking.send(PACKET_ID, buf);

        this.builder.addElement(aspect);
    }

    public void addItemStack(ItemStack stack) {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }
        if (builder == null) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.ADD_ITEM_STACK.name());
        buf.writeItemStack(stack);
        ClientPlayNetworking.send(PACKET_ID, buf);

        this.builder.addElement(new ItemElement.Stack(stack));
    }

    public void finishSpell() {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }
        if (builder == null) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.FINISH_SPELL.name());
        ClientPlayNetworking.send(PACKET_ID, buf);

        this.builder.finishSpell();
    }

    public void finishReaction() {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }
        if (builder == null) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.FINISH_REACTION.name());
        ClientPlayNetworking.send(PACKET_ID, buf);

        this.builder.finishReaction();
    }

    public void doCast(SpellForm form) {
        if (!ClientPlayNetworking.canSend(PACKET_ID)) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }
        if (builder == null) {
            RPGKitMod.LOGGER.error("Cannot send a spell build packet");
            return;
        }

        var buf = PacketByteBufs.create();
        buf.writeString(ServerSpellBuildHandler.Action.CAST.name());
        buf.writeIdentifier(ModRegistries.SPELL_FORMS.getId(form));
        ClientPlayNetworking.send(PACKET_ID, buf);

        builder = null;
    }

    @Override
    public ActionResult onReloaded() {
        // For some reason it is initialized way past client initialization.
        this.keyboard = ((InterceptableKeyboard)this.client.keyboard);
        for (int key = GLFW.GLFW_KEY_1; key <= GLFW.GLFW_KEY_9; key++) {
            this.keyboard.intercept(key, (k) -> this.builder != null);
        }
        this.keyboard.intercept(GLFW.GLFW_KEY_Q, (k) -> this.builder != null);
        this.keyboard.intercept(GLFW.GLFW_KEY_F, (k) -> this.builder != null);

        this.primaryEffectAspects = ModRegistries.ASPECTS.values().stream().filter(
                (a) -> a.isPrimary() && a.getKind() == Aspect.Kind.EFFECT
        ).sorted().toList();
        return ActionResult.SUCCESS;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        while (ClientRPGKitMod.ACTIVATE_SPELL_BUILD_KEY.wasPressed()) {
            if (this.builder == null) {
                this.start();
            } else {
                this.doCast(ModForms.SELF);
            }
        }

        if (this.builder == null) {
            return;
        }

        assert this.keyboard != null; // onReloaded called at least once before onEndTick
        for (int key = GLFW.GLFW_KEY_1; key <= GLFW.GLFW_KEY_9; key++) {
            if (this.primaryEffectAspects.size() <= key - GLFW.GLFW_KEY_1) {
                break;
            }
            while (this.keyboard.wasInterceptPressed(key)) {
                this.addAspect(this.primaryEffectAspects.get(key - GLFW.GLFW_KEY_1));
            }
        }

        while (this.keyboard.wasInterceptPressed(GLFW.GLFW_KEY_Q)) {
            this.doCast(ModForms.AREA);
        }
//        while (this.keyboard.wasInterceptPressed(GLFW.GLFW_KEY_F)) {
//            this.doCast(ModForms.ITEM);
//        }
    }
}
