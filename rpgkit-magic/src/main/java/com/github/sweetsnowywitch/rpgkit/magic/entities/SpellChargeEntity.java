package com.github.sweetsnowywitch.rpgkit.magic.entities;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.particle.GenericSpellParticleEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellElement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SpellChargeEntity extends PersistentProjectileEntity {
    public static final TrackedData<SpellCast> CAST = DataTracker.registerData(SpellChargeEntity.class, SpellCast.TRACKED_HANDLER);

    // Populated only on logical server side.
    public ServerSpellCast cast = null;
    private float bounceFactor = 0.1f;
    public int baseColor = 0x00FFFFFF; // ARGB, calculated on client-side only
    protected ParticleEffect particleEffect;

    public SpellChargeEntity(EntityType<? extends SpellChargeEntity> entityType, World world) {
        super(entityType, world);
    }

    public static SpellChargeEntity empty(EntityType<? extends SpellChargeEntity> entityType, World world) {
        return new SpellChargeEntity(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CAST, SpellCast.EMPTY);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("Cast")) {
            this.cast = ServerSpellCast.readFromNbt(nbt.getCompound("Cast")); // full data for server
            this.dataTracker.set(CAST, this.cast); // sync some spell data to client
        }
        this.bounceFactor = nbt.getFloat("BounceFactor");
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (CAST.equals(data)) {
            this.baseColor = SpellElement.calculateBaseColor(this.dataTracker.get(CAST).getFullRecipe());
            this.particleEffect = new GenericSpellParticleEffect(this.baseColor, 10);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        var castNBT = new NbtCompound();
        if (this.cast != null) {
            // Server with full data.
            this.cast.writeToNbt(castNBT);
        } else {
            // Client with partial data.
            this.dataTracker.get(CAST).writeToNbt(castNBT);
        }
        nbt.put("Cast", castNBT);
        nbt.putFloat("BounceFactor", this.bounceFactor);
    }

    public void setBounceFactor(float bounceFactor) {
        this.bounceFactor = bounceFactor;
    }

    public void setCast(@NotNull ServerSpellCast cast) {
        this.cast = cast;
        this.dataTracker.set(CAST, cast);
    }

    public SpellCast getCast() {
        if (this.cast != null) {
            return this.cast;
        }
        return this.dataTracker.get(CAST);
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult ehr) {
        if (this.world.isClient) {
            return;
        }

        this.cast.getSpell().useOnEntity(this.cast, ehr.getEntity());
    }

    private Vec3d bounceDirection(BlockHitResult bhr) {
        var side = bhr.getSide();
        var rotation = this.getVelocity();

        var factor = this.cast.getCost(SpellElement.COST_INTERITIO) * 0.3f + 0.1f;
        var degreesDelta = RPGKitMagicMod.RANDOM.nextFloat(factor) - factor / 2;

        var bounce = switch (side.getAxis()) {
            case X -> rotation.rotateX(180 + degreesDelta);
            case Y -> rotation.rotateY(180 + degreesDelta);
            case Z -> rotation.rotateZ(180 + degreesDelta);
        };
        return bounce.multiply(-1);
    }

    @Override
    protected void onBlockHit(BlockHitResult bhr) {
        this.calculateDimensions();
        if (this.world.isClient) {
            return;
        }

        var result = this.cast.getSpell().useOnBlock(this.cast, (ServerWorld) this.world, bhr.getBlockPos(), bhr.getSide());
        if (result.equals(ActionResult.CONSUME)) {
            this.setVelocity(bounceDirection(bhr).multiply(this.bounceFactor));
            if (this.getVelocity().lengthSquared() >= 0.01f) {
                return;
            }
        }

        super.onBlockHit(bhr);
    }

    protected void spawnParticles() {
        this.world.addParticle(this.particleEffect,
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient && !this.inGround) {
            this.spawnParticles();
        }

        if (this.inGround && this.getVelocity().lengthSquared() <= 0.1f && this.inGroundTime >= 20) {
            this.discard();
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }
}
