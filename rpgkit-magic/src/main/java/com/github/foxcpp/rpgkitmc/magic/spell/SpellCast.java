package com.github.foxcpp.rpgkitmc.magic.spell;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ClientSpellCast is a limited data object representing what both client
 * and server should know about spell cast.
 *
 * @see ServerSpellCast
 */
public class SpellCast {
    public static final TrackedDataHandler<SpellCast> TRACKED_HANDLER = new TrackedDataHandler.ImmutableHandler<>() {
        @Override
        public void write(PacketByteBuf buf, SpellCast value) {
            var nbt = new NbtCompound();
            value.writeToNbt(nbt);
            buf.writeNbt(nbt);
        }

        @Override
        public SpellCast read(PacketByteBuf buf) {
            var nbt = buf.readNbt();
            return SpellCast.readFromNbt(nbt);
        }
    };

    public static final SpellCast EMPTY = new SpellCast(ModForms.SELF, Spell.EMPTY,
            Map.of(), List.of(), Vec3d.ZERO, 0, 0);

    protected final SpellForm form;
    protected final Spell spell;
    protected final ImmutableMap<String, Float> costs;
    protected final ImmutableList<SpellElement> fullRecipe;
    protected Vec3d originPos;
    protected float originYaw, originPitch;

    public SpellCast(SpellForm form, Spell spell,
                     Map<String, Float> costs, List<SpellElement> fullRecipe,
                     Vec3d originPos, float originYaw, float originPitch) {
        this.form = form;
        this.spell = spell;
        this.costs = ImmutableMap.copyOf(costs);
        this.fullRecipe = ImmutableList.copyOf(fullRecipe);
        this.originPos = originPos;
        this.originYaw = originYaw;
        this.originPitch = originPitch;
    }

    protected SpellCast(NbtCompound nbt) {
        var formID = Identifier.tryParse(nbt.getString("Form"));
        if (formID == null) {
            throw new IllegalArgumentException("malformed form identifier in NBT: %s".formatted(nbt.getString("form")));
        }
        var form = MagicRegistries.FORMS.get(formID);
        if (form == null) {
            RPGKitMagicMod.LOGGER.warn("Unknown form ID in NBT, replacing with SELF: {}", formID);
            form = ModForms.SELF;
        }

        Spell spell;
        try {
            var spellJson = RPGKitMagicMod.GSON.fromJson(nbt.getString("SpellData"), JsonObject.class);
            spell = new Spell(spellJson);
        } catch (JsonSyntaxException | IllegalArgumentException ex) {
            RPGKitMagicMod.LOGGER.error("Invalid SpellData, replacing with empty spell", ex);
            spell = Spell.EMPTY;
        }

        var casterID = nbt.getUuid("Caster");
        if (casterID == null) throw new IllegalArgumentException("missing caster UUID in NBT");

        ImmutableMap.Builder<String, Float> costs = ImmutableMap.builder();
        var costsNbt = nbt.getCompound("Costs");
        for (var key : costsNbt.getKeys()) {
            costs.put(key, costsNbt.getFloat(key));
        }

        ImmutableList.Builder<SpellElement> fullRecipe = ImmutableList.builder();
        var fullRecipeNbt = nbt.getList("Recipe", NbtElement.COMPOUND_TYPE);
        for (var element : fullRecipeNbt) {
            try {
                fullRecipe.add(SpellElement.readFromNbt((NbtCompound) element));
            } catch (IllegalArgumentException e) {
                RPGKitMagicMod.LOGGER.warn("Failed to load full recipe item, ignoring", e);
            }
        }

        this.form = form;
        this.spell = spell;
        this.costs = costs.build();
        this.fullRecipe = fullRecipe.build();
        this.originPos = new Vec3d(nbt.getDouble("StartX"), nbt.getDouble("StartY"), nbt.getDouble("StartZ"));
        this.originYaw = nbt.getFloat("Yaw");
        this.originPitch = nbt.getFloat("Pitch");
    }

    public static SpellCast readFromNbt(NbtCompound nbt) {
        return new SpellCast(nbt);
    }

    public void writeToNbt(NbtCompound nbt) {
        var formID = MagicRegistries.FORMS.getId(this.form);
        if (formID == null)
            throw new IllegalStateException("writeToNbt called with unregistered spell form: %s".formatted(this.form));
        nbt.putString("Form", formID.toString());

        var spellJson = new JsonObject();
        this.spell.toJson(spellJson);
        nbt.putString("SpellData", RPGKitMagicMod.GSON.toJson(spellJson));

        var costs = new NbtCompound();
        for (var ent : this.costs.entrySet()) {
            costs.putFloat(ent.getKey(), ent.getValue());
        }
        nbt.put("Costs", costs);

        var fullRecipe = new NbtList();
        for (var element : this.fullRecipe) {
            var elementNBT = new NbtCompound();
            element.writeToNbt(elementNBT);
            fullRecipe.add(elementNBT);
        }
        nbt.put("Recipe", fullRecipe);

        nbt.putDouble("StartX", this.originPos.x);
        nbt.putDouble("StartY", this.originPos.y);
        nbt.putDouble("StartZ", this.originPos.z);
        nbt.putFloat("OriginYaw", this.originYaw);
        nbt.putFloat("OriginPitch", this.originPitch);
    }

    public SpellForm getForm() {
        return form;
    }

    public Spell getSpell() {
        return spell;
    }

    public float getCost(String key) {
        return Objects.requireNonNull(this.costs.getOrDefault(key, 0f));
    }

    public ImmutableList<SpellElement> getFullRecipe() {
        return this.fullRecipe;
    }

    public Vec3d getOriginPos() {
        return this.originPos;
    }

    public float getOriginYaw() {
        return this.originYaw;
    }

    public float getOriginPitch() {
        return this.originPitch;
    }

    public boolean isChanneled() {
        return this.getForm() instanceof ChanneledForm;
    }

    public void updateOrigin(Vec3d pos) {
        this.originPos = pos;
    }

    public void updateOriginRotation(float yaw, float pitch) {
        this.originYaw = yaw;
        this.originPitch = pitch;
    }
}
