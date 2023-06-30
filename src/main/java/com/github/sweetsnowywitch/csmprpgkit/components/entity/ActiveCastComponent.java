package com.github.sweetsnowywitch.csmprpgkit.components.entity;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveCastComponent implements ComponentV3, AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent {
    // Calls are redirected to it on client-side after doing necessary client-side checks.
    public static SpellCastController CLIENT_CONTROLLER = null;

    private final PlayerEntity provider;
    private ServerSpellCast activeCast; // populated only on server-side
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
                var elNBT = (NbtCompound) el;
                this.availableElements.add(SpellElement.readFromNbt(elNBT));
            }
        }
        if (tag.contains("PendingElements")) {
            this.pendingElements.clear();
            var list = tag.getList("PendingElements", NbtElement.COMPOUND_TYPE);
            for (var el : list) {
                var elNBT = (NbtCompound) el;
                this.pendingElements.add(SpellElement.readFromNbt(elNBT));
            }
        }

        this.maxElements = tag.getInt("MaxElements");

        if (!this.provider.world.isClient && this.hasBuilder) {
            this.builder = new SpellBuilder(this.provider, this.maxElements);
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

    public @Nullable ItemStack getCatalystBag() {
        var inv = this.provider.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem().equals(ModItems.CATALYST_BAG)) {
                return inv.getStack(i);
            }
        }
        return null;
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
        spell.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        this.provider.getInventory().setStack(this.provider.getInventory().selectedSlot, spell);

        this.builder = new SpellBuilder(this.provider, this.maxElements);
        this.hasBuilder = true;
        this.usingCatalystBag = false;
        this.pendingElements = List.of();
        this.availableElements = MagicRegistries.ASPECTS.values().stream().filter(
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

        ItemStack bag = this.getCatalystBag();
        if (bag == null) {
            this.usingCatalystBag = false;
        } else {
            this.usingCatalystBag = !this.usingCatalystBag;
        }

        if (this.usingCatalystBag) {
            this.availableElements = this.getAvailableCatalysts(bag);
        } else {
            this.availableElements = MagicRegistries.ASPECTS.values().stream().filter(
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

        var spell = this.builder.determinePendingSpell();
        var useForm = spell.getUseForm();
        if (useForm instanceof ChanneledForm cf) {
            this.channelMaxAge = cf.getMaxChannelDuration(spell);
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

    private ActionResult performCast(SpellForm form, @Nullable BlockPos pos, @Nullable Direction direction, @Nullable Entity entity) {
        assert this.builder != null;

        if (this.builder.getFullRecipe().size() == 0) {
            this.cancelBuild();
            return ActionResult.FAIL;
        }

        ActionResult res = ActionResult.CONSUME;
        var cast = this.builder.toServerCast(form);
        cast.perform((ServerWorld) this.provider.world);
        if (form.equals(ModForms.USE)) {
            if (pos != null) {
                res = cast.getSpell().useOnBlock(cast, (ServerWorld) this.provider.world, pos, direction);
            } else if (entity != null) {
                res = cast.getSpell().useOnEntity(cast, entity);
            }
        }

        if (form instanceof ChanneledForm cf) {
            this.channelMaxAge = cf.getMaxChannelDuration(cast.getSpell());
            this.activeCast = cast;
            this.hasActiveCast = true;
        } else {
            this.channelMaxAge = 0;
            this.activeCast = null;
            this.hasActiveCast = false;
        }
        this.channelAge = 0;

        this.builder = null;
        this.hasBuilder = false;
        this.usingCatalystBag = false;

        var handStack = this.provider.getMainHandStack();
        if (handStack.getItem().equals(ModItems.SPELL_ITEM)) {
            if (!this.isChanneling()) {
                handStack.decrement(1);
            }
        }

        ModComponents.CAST.sync(this.provider);

        return res;
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

        this.performCast(ModForms.SELF, null, null, null);
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

        this.performCast(ModForms.ITEM, null, null, null);
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

        this.performCast(ModForms.AREA, null, null, null);
    }

    public ActionResult performCastOnBlock(BlockPos pos, Direction direction) {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performCastOnBlock(pos, direction);
            }
            return ActionResult.SUCCESS;
        }

        if (pos.getSquaredDistance(this.provider.getEyePos().x, this.provider.getEyePos().y, this.provider.getEyePos().z) > 36) {
            RPGKitMod.LOGGER.warn("Player {} sent CastType.USE_BLOCK for a block more than 6 blocks away, ignoring and canceling spell", this.provider);
            this.cancelBuild();
            return ActionResult.FAIL;
        }

        return this.performCast(ModForms.USE, pos, direction, null);
    }

    public ActionResult performCastOnEntity(Entity target) {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }

        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performCastOnEntity(target);
            }
            return ActionResult.SUCCESS;
        }

        if (target.getPos().squaredDistanceTo(this.provider.getEyePos()) > 36) {
            RPGKitMod.LOGGER.warn("Player {} sent CastType.USE_ENTITY for an entity more than 6 blocks away, ignoring and canceling spell", this.provider);
            this.cancelBuild();
            return ActionResult.FAIL;
        }

        return this.performCast(ModForms.USE, null, null, target);
    }

    public ActionResult performRangedCast() {
        if (!this.hasBuilder) {
            throw new IllegalStateException("cannot perform cast when not building a spell");
        }
        if (this.provider.world.isClient) {
            if (CLIENT_CONTROLLER != null) {
                CLIENT_CONTROLLER.performRangedCast();
            }
            return ActionResult.SUCCESS;
        }

        return this.performCast(this.builder.determineUseForm(), null, null, null);
    }

    private List<SpellElement> getAvailableCatalysts(ItemStack bag) {
        var elements = new ArrayList<SpellElement>(6);

        var bagInv = CatalystBagItem.getInventory(bag);
        for (int j = 0; j < bagInv.size(); j++) {
            if (bagInv.getStack(j).isEmpty()) continue;
            elements.add(new ItemElement.Stack(bagInv.getStack(j), bagInv));
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
        var cast = (ServerSpellCast) this.activeCast;
        cast.getForm().endCast(cast, (ServerWorld) this.provider.world);
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
        if (this.usingCatalystBag && this.getCatalystBag() == null) {
            this.usingCatalystBag = false;
            ModComponents.CAST.sync(this.provider);
        }

        var bag = this.getCatalystBag();
        if (bag != null && CatalystBagItem.isOpen(bag) != this.usingCatalystBag) {
            CatalystBagItem.setOpen(bag, this.usingCatalystBag);
            this.provider.getInventory().markDirty();
        }

        if (this.activeCast == null) {
            return;
        }
        var cast = (ServerSpellCast) this.activeCast;

        if (this.isChanneling()) {
            if (!(cast.getForm() instanceof ChanneledForm cf)) {
                RPGKitMod.LOGGER.error("Cast form for {} is not channelable, but channelMaxAge is set, cancelling cast.", this.provider);
                this.endCast();
                return;
            }

            cf.channelTick(cast, this.provider);

            this.channelAge++;

            if (this.channelAge > 10) {
                var caster = cast.getCaster((ServerWorld) this.provider.world);
                if (caster instanceof ServerPlayerEntity spe) {
                    spe.getComponent(ModComponents.MANA).spendMana(cast.getCost(SpellElement.COST_MAGICAE) / 20);
                }
            }

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
