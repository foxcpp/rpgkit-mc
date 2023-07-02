package com.github.sweetsnowywitch.rpgkit.magic.spell;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ServerSpellCast extends SpellCast with server-specific information that
 * should not be directly revealed to the client.
 * It also implements all necessary logic to perform casts.
 */
public class ServerSpellCast extends SpellCast {
    private final UUID casterUuid;
    public final NbtCompound customData;

    public ServerSpellCast(SpellForm form, Spell spell, UUID casterID,
                           Map<String, Float> costs, List<SpellElement> fullRecipe,
                           Vec3d startPos, float startYaw, float startPitch) {
        super(form, spell, costs, fullRecipe, startPos, startYaw, startPitch);

        this.casterUuid = casterID;
        this.customData = new NbtCompound();
    }

    public ServerSpellCast(SpellForm form, Spell spell, @NotNull Entity caster,
                           Map<String, Float> costs, List<SpellElement> fullRecipe,
                           Vec3d startPos, float startYaw, float startPitch) {
        super(form, spell, costs, fullRecipe, startPos, startYaw, startPitch);

        if (caster.getWorld().isClient) {
            throw new IllegalStateException("cannot instantiate ServerSpellCast on logical client");
        }

        this.casterUuid = caster.getUuid();
        this.customData = new NbtCompound();
    }

    protected ServerSpellCast(NbtCompound nbt) {
        super(nbt);

        var casterID = nbt.getUuid("Caster");
        if (casterID == null) throw new IllegalArgumentException("missing caster UUID in NBT");
        this.casterUuid = casterID;
        this.customData = nbt.getCompound("CustomData");
    }

    public ServerSpellCast withSpell(Spell spell) {
        return new ServerSpellCast(this.form, spell, this.casterUuid,
                this.costs, this.fullRecipe,
                this.originPos, this.originYaw, this.originPitch);
    }

    public void perform(ServerWorld world) {
        var caster = (LivingEntity) world.getEntity(this.casterUuid);

        if (caster == null) {
            RPGKitMagicMod.LOGGER.error("ServerSpellCast.perform is called but caster entity is missing in world");
            return;
        }

        this.form.startCast(this, world, caster);

        if (caster instanceof PlayerEntity player) {
            var cost = this.costs.getOrDefault(SpellElement.COST_MAGICAE, (float) 0);
            if (cost != null) {
                RPGKitMagicMod.LOGGER.debug("Consuming {} mana points of {}", cost, player);
                player.getComponent(ModComponents.MANA).spendMana((int) ((float) cost));
            }
        }
    }

    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);

        nbt.putUuid("Caster", this.casterUuid);
        nbt.put("CustomData", this.customData);
    }

    public UUID getCasterUuid() {
        return this.casterUuid;
    }

    public @Nullable Entity getCaster(@NotNull ServerWorld world) {
        return world.getEntity(this.casterUuid);
    }

    public @Nullable PlayerEntity getPlayerCaster(@NotNull ServerWorld world) {
        var caster = this.getCaster(world);
        if (caster instanceof PlayerEntity pe) {
            return pe;
        }
        return null;
    }

    public static ServerSpellCast readFromNbt(NbtCompound nbt) {
        return new ServerSpellCast(nbt);
    }

    @Override
    public String toString() {
        return "ServerSpellSpell[form=%s,spell=%s,caster=%s]".formatted(this.form.toString(), this.spell.toString(), this.casterUuid.toString());
    }
}
