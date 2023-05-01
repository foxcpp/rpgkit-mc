package com.github.sweetsnowywitch.csmprpgkit.components;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.items.CatalystBagItem;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.*;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveCastComponent implements ComponentV3, AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent, SpellCastController {
    // Calls are redirected to it on client-side after doing necessary client-side checks.
    public static SpellCastController CLIENT_CONTROLLER = null;

    private final PlayerEntity provider;
    private SpellCast activeCast; // populated only on server-side
    private SpellBuilder builder; // populated only on server-side
    private List<SpellElement> pendingElements;
    private List<SpellElement> availableElements;
    private int maxElements;
    private boolean hasBuilder;
    private boolean hasActiveCast;
    private boolean usingCatalystBag;
    private int channelAge;
    private int channelMaxAge;

    public ActiveCastComponent(PlayerEntity provider) {
        this.provider = provider;
        this.activeCast = null;
        this.builder = null;
        this.maxElements = 5; // TODO: populate from attributes?
        this.availableElements = new ArrayList<>();
        this.pendingElements = new ArrayList<>();
        this.hasBuilder = false;
        this.hasActiveCast = false;
        this.usingCatalystBag = false;
        this.channelAge = 0;
        this.channelMaxAge = 0;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("ActiveCast") && !this.provider.world.isClient) {
            // TODO: Properly synchronize ActiveCast to client (generic reactions are not serialized properly).
            this.activeCast = ServerSpellCast.readFromNbt(tag.getCompound("ActiveCast"));
        }
        if (tag.contains("ResetActiveCast") && tag.getBoolean("ResetActiveCast")) {
            this.activeCast = null;
        }
        this.hasActiveCast = tag.getBoolean("HasActiveCast");

        this.hasBuilder = tag.getBoolean("HasBuilder");
        this.usingCatalystBag = tag.getBoolean("UsingCatalystBag");
        if (tag.contains("AvailableElements")) {
            this.availableElements.clear();
            var list = tag.getList("AvailableElements", NbtElement.COMPOUND_TYPE);
            for (var el : list) {
                var elNBT = (NbtCompound)el;
                this.availableElements.add(SpellElement.readFromNbt(elNBT));
            }
        }
        if (tag.contains("PendingElements")) {
            this.pendingElements.clear();
            var list = tag.getList("PendingElements", NbtElement.COMPOUND_TYPE);
            for (var el : list) {
                var elNBT = (NbtCompound)el;
                this.pendingElements.add(SpellElement.readFromNbt(elNBT));
            }
        }

        this.maxElements = tag.getInt("MaxElements");

        if (!this.provider.world.isClient && this.hasBuilder) {
            this.builder = new SpellBuilder(this.maxElements);
            // TODO: Recheck if items can still be used.
            for (var element : this.pendingElements) {
                this.builder.addElement(element);
            }
        }

        this.channelAge = tag.getInt("ChannelAge");
        this.channelMaxAge = tag.getInt("ChannelMaxAge");
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player.equals(this.provider);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        if (this.activeCast != null) {
            var castNBT = new NbtCompound();
            this.activeCast.writeToNbt(castNBT);
            tag.put("ActiveCast", castNBT);
        } else {
            tag.putBoolean("ResetActiveCast", true);
        }

        tag.putBoolean("HasBuilder", this.hasBuilder);
        tag.putBoolean("HasActiveCast", this.hasActiveCast);
        tag.putBoolean("UsingCatalystBag", this.usingCatalystBag);
        if (this.isBuilding()) {
            var availableElements = new NbtList();
            for (var el : this.availableElements) {
                var elNBT = new NbtCompound();
                el.writeToNbt(elNBT);
                availableElements.add(elNBT);
            }
            tag.put("AvailableElements", availableElements);

            var pendingElements = new NbtList();
            for (var el : this.pendingElements) {
                var elNBT = new NbtCompound();
                el.writeToNbt(elNBT);
                pendingElements.add(elNBT);
            }
            tag.put("PendingElements", pendingElements);
        } else {
            tag.put("AvailableElements", new NbtList());
            tag.put("PendingElements", new NbtList());
        }

        tag.putInt("MaxElements", this.maxElements);
        tag.putInt("ChannelAge", this.channelAge);
        tag.putInt("ChannelMaxAge", this.channelMaxAge);
    }

    public boolean isBuilding() {
        return hasBuilder;
    }

    public boolean hasCatalystBag() {
        var inv = this.provider.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem().equals(ModItems.CATALYST_BAG)) {
                return true;
            }
        }
        return false;
    }

    public void startBuild() {
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.startBuild();
            }
            return;
        }

        if (this.provider.getComponent(ModComponents.MANA).getValue() == 0) {
            this.provider.sendMessage(Text.translatable("csmprpgkit.magic.must_have_mana"), true);
            return;
        }

        if (this.isChanneling()) {
            this.endCast();
        }
        if (!this.provider.getMainHandStack().isEmpty()) {
            this.provider.sendMessage(Text.translatable("csmprpgkit.magic.must_have_empty_hand"), true);
            return;
        }

        var spell = new ItemStack(ModItems.SPELL_ITEM);
        EnchantmentHelper.set(Map.of(Enchantments.VANISHING_CURSE, 1), spell);
        this.provider.getInventory().setStack(this.provider.getInventory().selectedSlot, spell);

        this.builder = new SpellBuilder(this.maxElements);
        this.hasBuilder = true;
        this.usingCatalystBag = false;
        this.pendingElements = List.of();
        this.availableElements = ModRegistries.ASPECTS.values().stream().filter(
                (a) -> a.isPrimary() && a.getKind() == Aspect.Kind.EFFECT
        ).sorted().collect(Collectors.toList());

        ModComponents.CAST.sync(this.provider);
    }

    public void cancelBuild() {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot cancel build on the client-side");
        }

        this.builder = null;
        this.hasBuilder = false;
        this.usingCatalystBag = false;

        ModComponents.CAST.sync(this.provider);
    }

    public void switchCatalystBag() {
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.switchCatalystBag();
            }
            return;
        }

        if (!this.hasCatalystBag()) {
            this.usingCatalystBag = false;
        } else {
            this.usingCatalystBag = !this.usingCatalystBag;
        }

        if (this.usingCatalystBag) {
            this.availableElements = this.getAvailableCatalysts();
        } else {
            this.availableElements = ModRegistries.ASPECTS.values().stream().filter(
                    (a) -> a.isPrimary() && a.getKind() == Aspect.Kind.EFFECT
            ).sorted().collect(Collectors.toList());
        }

        ModComponents.CAST.sync(this.provider);
    }

    public int getMaxElements() {
        return maxElements;
    }

    public List<SpellElement> getPendingElements() {
        return this.pendingElements;
    }

    public List<SpellElement> getAvailableElements() {
        return this.availableElements;
    }

    public void addElement(int index) {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.addElement(index);
            }
            return;
        }

        assert this.builder != null;

        if (this.availableElements.size() <= index) {
            throw new IllegalArgumentException("available elements does not contain an element %d".formatted(index));
        }
        this.builder.addElement(this.availableElements.get(index));
        this.pendingElements = this.builder.getPendingElements();

        var pendingForm = this.builder.determineUseForm();
        if (pendingForm instanceof ChanneledForm cf) {
            var spell = this.builder.determinePendingSpell();
            this.channelMaxAge = cf.getMaxChannelDuration(spell, spell.getForcedEffectReactions());
        } else {
            this.channelMaxAge = 0;
        }

        var handStack = this.provider.getMainHandStack();
        if (handStack.getItem().equals(ModItems.SPELL_ITEM)) {
            handStack.setSubNbt("Color", NbtInt.of(SpellElement.calculateBaseColor(builder.getFullRecipe())));
            handStack.setSubNbt("MaxChannelDuration", NbtInt.of(this.channelMaxAge));
        }

        ModComponents.CAST.sync(this.provider);
    }

    private void performCast(SpellForm form) {
        assert this.builder != null;

        if (this.builder.getFullRecipe().size() == 0) {
            this.cancelBuild();
            return;
        }

        var cast = this.builder.toServerCast(this.provider, form);
        cast.perform((ServerWorld) this.provider.world);
        this.activeCast = cast;
        this.hasActiveCast = true;

        if (form instanceof ChanneledForm cf) {
            this.channelMaxAge = cf.getMaxChannelDuration(cast.getSpell(), cast.getReactions());
        } else {
            this.channelMaxAge = 0;
        }
        this.channelAge = 0;

        this.builder = null;
        this.hasBuilder = false;
        this.usingCatalystBag = false;

        var handStack = this.provider.getMainHandStack();
        if (handStack.getItem().equals(ModItems.SPELL_ITEM) && !this.isChanneling()) {
            handStack.decrement(1);
        }

        ModComponents.CAST.sync(this.provider);
    }

    public void performSelfCast() {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performSelfCast();
            }
            return;
        }

        this.performCast(ModForms.SELF);
    }

    public void performItemCast() {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performItemCast();
            }
            return;
        }

        throw new IllegalStateException("not implemented yet");
    }

    public void performAreaCast() {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performAreaCast();
            }
            return;
        }

        this.performCast(ModForms.AREA);
    }

    public void performUseCast() {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performUseCast();
            }
            return;
        }

        this.performCast(this.builder.determineUseForm());
    }

    private List<SpellElement> getAvailableCatalysts() {
        var elements = new ArrayList<SpellElement>(6);
        var inv = this.provider.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem().equals(ModItems.CATALYST_BAG)) {
                var bagInv = CatalystBagItem.getInventory(inv.getStack(i));
                for (int j = 0; j < bagInv.size(); j++) {
                    if (bagInv.getStack(j).isEmpty()) continue;
                    elements.add(new ItemElement.Stack(bagInv.getStack(j), bagInv));
                }
            }
        }
        return elements;
    }

    public boolean isChanneling() {
        return this.hasActiveCast && this.channelMaxAge > 0;
    }

    public int getChannelAge() {
        return this.channelAge;
    }

    public int getChannelMaxAge() {
        return this.channelMaxAge;
    }

    public void interruptChanneling() {
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.interruptChanneling();
            }
            return;
        }

        if (this.activeCast == null) {
            return;
        }

        this.endCast();
    }

    private void endCast() {
        var cast = (ServerSpellCast)this.activeCast;
        cast.getForm().endCast(cast, (ServerWorld)this.provider.world);
        this.activeCast = null;
        this.hasActiveCast = false;
        this.channelAge = 0;
        this.channelMaxAge = 0;

        var handStack = this.provider.getMainHandStack();
        if (handStack.getItem().equals(ModItems.SPELL_ITEM)) {
            handStack.decrement(1);
        }

        this.cancelBuild();
    }

    @Override
    public void serverTick() {
        if (this.usingCatalystBag && !this.hasCatalystBag()) {
            this.usingCatalystBag = false;
            ModComponents.CAST.sync(this.provider);
        }

        if (this.activeCast == null) {
            return;
        }
        var cast = (ServerSpellCast)this.activeCast;

        if (this.isChanneling()) {
            if (!(cast.getForm() instanceof ChanneledForm cf)) {
                RPGKitMod.LOGGER.error("Cast form for {} is not channelable, but channelMaxAge is set, cancelling cast.", this.provider);
                this.endCast();
                return;
            }

            cf.channelTick(cast, this.provider);
            this.channelAge++;

            if (this.channelAge >= this.channelMaxAge) {
                this.endCast();
            }
        }
    }

    @Override
    public void clientTick() {
        if (this.isChanneling()) {
            this.channelAge++;
        }
    }
}
