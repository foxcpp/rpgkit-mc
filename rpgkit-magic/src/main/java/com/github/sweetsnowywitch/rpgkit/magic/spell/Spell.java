package com.github.sweetsnowywitch.rpgkit.magic.spell;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Spell {

    public static final Spell EMPTY = new Spell();
    private final ImmutableList<ItemEffect.Used> itemEffects;
    private final ImmutableList<AreaEffect.Used> areaEffects;
    private final ImmutableList<UseEffect.Used> useEffects;
    private final ImmutableList<SpellReaction> formReactions;
    private final SpellForm useForm;

    private Spell() {
        this.itemEffects = ImmutableList.of();
        this.areaEffects = ImmutableList.of();
        this.useEffects = ImmutableList.of();
        this.formReactions = ImmutableList.of();
        this.useForm = ModForms.RAY;
    }

    public Spell(ImmutableList<ItemEffect.Used> item,
                 ImmutableList<AreaEffect.Used> area,
                 ImmutableList<UseEffect.Used> interact,
                 ImmutableList<SpellReaction> formReactions, SpellForm useForm) {
        this.itemEffects = item;
        this.areaEffects = area;
        this.useEffects = interact;
        this.formReactions = formReactions;
        this.useForm = useForm;
    }

    public Spell(ImmutableList<UseEffect.Used> interact,
                 ImmutableList<SpellReaction> formReactions, SpellForm useForm) {
        this.itemEffects = ImmutableList.of();
        this.areaEffects = ImmutableList.of();
        this.useEffects = interact;
        this.formReactions = formReactions;
        this.useForm = useForm;
    }

    public Spell(JsonObject obj) {
        this.itemEffects = JsonHelpers.fromJsonList(obj.getAsJsonArray("item_effects"), ItemEffect.Used::fromJson);
        this.areaEffects = JsonHelpers.fromJsonList(obj.getAsJsonArray("area_effects"), AreaEffect.Used::fromJson);
        this.useEffects = JsonHelpers.fromJsonList(obj.getAsJsonArray("use_effects"), UseEffect.Used::fromJson);
        this.formReactions = JsonHelpers.fromJsonList(obj.getAsJsonArray("form_reactions"), SpellReaction::fromJson);

        var useFormId = new Identifier(obj.get("use_form").getAsString());
        var useForm = MagicRegistries.FORMS.get(useFormId);
        if (useForm == null) {
            RPGKitMagicMod.LOGGER.warn("Unknown use form in loaded Spell, replacing with RAY: {}", useFormId);
            useForm = ModForms.RAY;
        }
        this.useForm = useForm;
    }

    public void toJson(@NotNull JsonObject obj) {
        obj.add("item_effects", JsonHelpers.toJsonList(this.itemEffects));
        obj.add("area_effects", JsonHelpers.toJsonList(this.areaEffects));
        obj.add("use_effects", JsonHelpers.toJsonList(this.useEffects));
        obj.add("form_reactions", JsonHelpers.toJsonList(this.formReactions));
        obj.addProperty("use_form", Objects.requireNonNull(MagicRegistries.FORMS.getId(this.useForm)).toString());
    }

    public ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaEffect.AreaCollider collider) {
        ActionResult lastResult = ActionResult.SUCCESS;
        for (var eff : this.areaEffects) {
            lastResult = eff.useOnArea(cast, world, boundingBox, origin, collider);
            if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                break;
            }
        }
        return lastResult;
    }

    public TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
        TypedActionResult<ItemStack> lastResult = TypedActionResult.success(stack);
        for (var eff : this.itemEffects) {
            lastResult = eff.useOnItem(cast, world, stack, container, holder);
            if (lastResult.getResult().equals(ActionResult.CONSUME) || lastResult.getResult().equals(ActionResult.FAIL)) {
                break;
            }
        }
        return lastResult;
    }

    public boolean canInteract(ServerSpellCast cast) {
        return true; // TODO
    }

    public ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
        ActionResult lastResult = ActionResult.CONSUME;
        for (var eff : this.useEffects) {
            lastResult = eff.useOnBlock(cast, world, pos, direction);
            if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                break;
            }
        }
        return lastResult;
    }

    public ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
        ActionResult lastResult = ActionResult.CONSUME;
        for (var eff : this.useEffects) {
            lastResult = eff.useOnEntity(cast, entity);
            if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                break;
            }
        }
        return lastResult;
    }

    public SpellForm getUseForm() {
        return this.useForm;
    }

    public ImmutableList<SpellReaction> getFormReactions() {
        return formReactions;
    }
}
