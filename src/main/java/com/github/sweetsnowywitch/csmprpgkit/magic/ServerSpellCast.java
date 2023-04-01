package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

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

    public ServerSpellCast(SpellForm form, Spell spell, UUID casterID, List<SpellReaction> formReactions,
                           List<SpellReaction> effectReactions, Map<String, Float> costs, List<SpellElement> fullRecipe) {
        super(form, spell, formReactions, effectReactions, costs, fullRecipe);

        this.casterUuid = casterID;
    }

    public ServerSpellCast(SpellForm form, Spell spell, @NotNull LivingEntity caster, List<SpellReaction> formReactions,
                           List<SpellReaction> effectReactions, Map<String, Float> costs, List<SpellElement> fullRecipe) {
        super(form, spell, formReactions, effectReactions, costs, fullRecipe);

        if (caster.getWorld().isClient) {
            throw new IllegalStateException("cannot instantiate ServerSpellCast on logical client");
        }

        this.casterUuid = caster.getUuid();
    }

    protected ServerSpellCast(NbtCompound nbt) {
        super(nbt);

        var casterID = nbt.getUuid("Caster");
        if (casterID == null) throw new IllegalArgumentException("missing caster UUID in NBT");
        this.casterUuid = casterID;
    }

    public void perform(ServerWorld world) {
        var caster = (LivingEntity)world.getEntity(this.casterUuid);

        this.form.startCast(this, world, caster);

        if (caster instanceof PlayerEntity player) {
            var cost = this.costs.getOrDefault(SpellElement.COST_MAGICAE, (float)0);
            if (cost != null) {
                RPGKitMod.LOGGER.debug("Consuming {} mana points of {}", cost, player);
                player.getComponent(ModComponents.MANA).spendMana((int)((float)cost));
            }
        }
    }

    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);

        nbt.putUuid("Caster", this.casterUuid);
    }

    public UUID getCasterUuid() {
        return this.casterUuid;
    }

    public static ServerSpellCast readFromNbt(NbtCompound nbt) {
        return new ServerSpellCast(nbt);
    }

    @Override
    public String toString() {
        return "ServerSpellSpell[form=%s,spell=%s,caster=%s]".formatted(this.form.toString(), this.spell.toString(), this.casterUuid.toString());
    }
}
