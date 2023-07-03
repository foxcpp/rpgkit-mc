package com.github.sweetsnowywitch.rpgkit.magic.client;

import com.github.clevernucleus.playerex.api.client.PageRegistry;
import com.google.gson.Gson;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.client.overlays.ActiveCastOverlay;
import com.github.sweetsnowywitch.rpgkit.magic.client.overlays.ManaHudOverlay;
import com.github.sweetsnowywitch.rpgkit.magic.client.particle.GenericSpellParticle;
import com.github.sweetsnowywitch.rpgkit.magic.client.render.ModRenderers;
import com.github.sweetsnowywitch.rpgkit.magic.client.render.SpellItemRenderer;
import com.github.sweetsnowywitch.rpgkit.magic.client.screen.CatalystBagScreen;
import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import com.github.sweetsnowywitch.rpgkit.magic.components.entity.ActiveCastComponent;
import com.github.sweetsnowywitch.rpgkit.magic.items.CatalystBagItem;
import com.github.sweetsnowywitch.rpgkit.magic.items.ModItems;
import com.github.sweetsnowywitch.rpgkit.magic.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class ClientRPGKitMagicMod implements ClientModInitializer {
    public static final Gson GSON = new Gson();
    public static final Identifier MAGIC_PAGE = new Identifier(RPGKitMod.MOD_ID, "magic_page");

    public static final KeyBinding ACTIVATE_SPELL_BUILD_KEY = new KeyBinding(
            "key." + RPGKitMagicMod.MOD_ID + ".magic.spell_build",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "category." + RPGKitMagicMod.MOD_ID + ".magic"
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

            lines.add(Text.translatable("rpgkit.magic.help.intro"));
            lines.add(Text.translatable("rpgkit.magic.help.use_cast"));
            lines.add(Text.translatable("rpgkit.magic.help.area_cast",
                    SPELL_BUILD_KEYBOARD_HANDLER.getAreaCastKey().getLocalizedText()));
            lines.add(Text.translatable("rpgkit.magic.help.self_cast",
                    ACTIVATE_SPELL_BUILD_KEY.getBoundKeyLocalizedText()));
            lines.add(Text.translatable("rpgkit.magic.help.catalyst_bag",
                    SPELL_BUILD_KEYBOARD_HANDLER.getCatalystBagKey().getLocalizedText()));
        });
        ModelPredicateProviderRegistry.register(ModItems.CATALYST_BAG, new Identifier("open"), ((stack, world, entity, seed) -> {
            if (!(entity instanceof PlayerEntity)) {
                entity = MinecraftClient.getInstance().player;
            }
            if (entity == null) {
                return 0f;
            }
            var pe = (PlayerEntity) entity;
            var comp = pe.getComponent(ModComponents.CAST);
            if (!comp.isBuilding() || !stack.equals(comp.getCatalystBag())) {
                return 0f;
            }
            return CatalystBagItem.isOpen(stack) ? 1f : 0f;
        }));

        GeoItemRenderer.registerItemRenderer(ModItems.SPELL_ITEM, new SpellItemRenderer());

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((((atlasTexture, registry) -> {
            registry.register(new Identifier(RPGKitMagicMod.MOD_ID, "particle/generic_spell_0"));
        })));
        ParticleFactoryRegistry.getInstance().register(ModParticles.GENERIC_SPELL, GenericSpellParticle.Factory::new);

        HandledScreens.register(RPGKitMagicMod.CATALYST_BAG_SCREEN_HANDLER, CatalystBagScreen::new);
        RPGKitMod.DATA_SYNCER.setupClient();

        PageRegistry.registerPage(MAGIC_PAGE, new Identifier(RPGKitMod.MOD_ID, "textures/gui/icon_magic.png"), Text.translatable("csmprpgkit.gui.page.magic.title"));
        PageRegistry.registerLayer(MAGIC_PAGE, MagicPageLayer::new);
    }
}
