package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.listener.AdvancementsListener;
import com.github.sweetsnowywitch.csmprpgkit.classes.listener.ClassReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.client.overlays.ManaHudOverlay;
import com.github.sweetsnowywitch.csmprpgkit.client.overlays.ActiveCastOverlay;
import com.github.sweetsnowywitch.csmprpgkit.client.particle.GenericSpellParticle;
import com.github.sweetsnowywitch.csmprpgkit.client.render.ModRenderers;
import com.github.sweetsnowywitch.csmprpgkit.client.render.SpellItemRenderer;
import com.github.sweetsnowywitch.csmprpgkit.client.screen.CatalystBagScreen;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.github.sweetsnowywitch.csmprpgkit.components.entity.ActiveCastComponent;
import com.github.sweetsnowywitch.csmprpgkit.events.DataRegistryReloadCallback;
import com.github.sweetsnowywitch.csmprpgkit.items.CatalystBagItem;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.AspectReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.ReactionReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.SpellReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.particle.ModParticles;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class ClientRPGKitMod implements ClientModInitializer {
    public static final Gson GSON = new Gson();

    public static final KeyBinding ACTIVATE_SPELL_BUILD_KEY = new KeyBinding(
            "key."+RPGKitMod.MOD_ID+".magic.spell_build",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "category."+RPGKitMod.MOD_ID+".magic"
    );

    public static final SpellBuildKeyboardHandler SPELL_BUILD_KEYBOARD_HANDLER = new SpellBuildKeyboardHandler();

    @Override
    public void onInitializeClient() {
        ModRenderers.register();
        HudRenderCallback.EVENT.register(new ManaHudOverlay());
        HudRenderCallback.EVENT.register(new ActiveCastOverlay());

        KeyBindingHelper.registerKeyBinding(ACTIVATE_SPELL_BUILD_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(SPELL_BUILD_KEYBOARD_HANDLER);
        ActiveCastComponent.CLIENT_CONTROLLER = new ClientSpellCastController();
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            var client = MinecraftClient.getInstance();
            if (client.player == null) {
                return;
            }
            if (!client.player.getComponent(ModComponents.CAST).isBuilding()) {
                return;
            }

            lines.add(Text.translatable("csmprpgkit.magic.help.intro"));
            lines.add(Text.translatable("csmprpgkit.magic.help.use_cast"));
            lines.add(Text.translatable("csmprpgkit.magic.help.area_cast",
                    SPELL_BUILD_KEYBOARD_HANDLER.getAreaCastKey().getLocalizedText()));
            lines.add(Text.translatable("csmprpgkit.magic.help.self_cast",
                    ACTIVATE_SPELL_BUILD_KEY.getBoundKeyLocalizedText()));
            lines.add(Text.translatable("csmprpgkit.magic.help.catalyst_bag",
                    SPELL_BUILD_KEYBOARD_HANDLER.getCatalystBagKey().getLocalizedText()));
        });
        ModelPredicateProviderRegistry.register(ModItems.CATALYST_BAG, new Identifier("open"), ((stack, world, entity, seed) -> {
            if (!(entity instanceof PlayerEntity)) {
                entity = MinecraftClient.getInstance().player;
            }
            if (entity == null) {
                return 0f;
            }
            var pe = (PlayerEntity)entity;
            var comp = pe.getComponent(ModComponents.CAST);
            if (!comp.isBuilding() || !stack.equals(comp.getCatalystBag())) {
                return 0f;
            }
            return CatalystBagItem.isOpen(stack) ? 1f : 0f;
        }));

        GeoItemRenderer.registerItemRenderer(ModItems.SPELL_ITEM, new SpellItemRenderer());

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((((atlasTexture, registry) -> {
            registry.register(new Identifier(RPGKitMod.MOD_ID, "particle/generic_spell_0"));
        })));
        ParticleFactoryRegistry.getInstance().register(ModParticles.GENERIC_SPELL, GenericSpellParticle.Factory::new);

        HandledScreens.register(RPGKitMod.CATALYST_BAG_SCREEN_HANDLER, CatalystBagScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(RPGKitMod.SERVER_DATA_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            var jsonBlob = buf.readString(1024*1024);
            client.execute(() -> this.loadServerData(jsonBlob));
        });
    }

    private static HashMap<Identifier, JsonElement> fromJsonMap(JsonObject obj) {
        var res = new HashMap<Identifier, JsonElement>();
        for (var entry : obj.entrySet()) {
            var id = Identifier.tryParse(entry.getKey());
            if (id == null) {
                throw new JsonSyntaxException("Invalid identifier: %s".formatted(entry.getKey()));
            }
            res.put(id, entry.getValue());
        }
        return res;
    }

    public void loadServerData(String jsonBlob) {
        RPGKitMod.LOGGER.info("Loading server data...");

        HashMap<Identifier, JsonElement> classes, advancements;
        HashMap<Identifier, JsonElement> aspects, spells, reactions;

        try {
            var json = GSON.fromJson(jsonBlob, JsonObject.class);

            classes = fromJsonMap(json.getAsJsonObject("classes"));
            advancements = fromJsonMap(json.getAsJsonObject("advancements"));

            aspects = fromJsonMap(json.getAsJsonObject("aspects"));
            spells = fromJsonMap(json.getAsJsonObject("spells"));
            reactions = fromJsonMap(json.getAsJsonObject("reactions"));
        } catch (JsonSyntaxException ex) {
            RPGKitMod.LOGGER.error("Error occurred while decoding JSON data from server: {}", ex.toString());
            return;
        }

        try {
            ClassReloadListener.load(classes);
            AdvancementsListener.load(advancements);

            AspectReloadListener.load(aspects);
            SpellReloadListener.load(spells);
            ReactionReloadListener.load(reactions);
        } catch (Exception ex) {
            RPGKitMod.LOGGER.error("Error occurred while loading JSON data from server, registries may be in a broken state! {}", ex.toString());
        }

        DataRegistryReloadCallback.EVENT.invoker().onReloaded();
    }
}
