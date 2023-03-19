package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class SpellRayEntity extends Entity {
    public SpellCast cast = null;
    private int maxAge = 2*20;
    private float growthSpeed = 2;
    public static final TrackedData<Float> MAX_LENGTH = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> LENGTH = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Boolean> IS_GROWING = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public SpellRayEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
    }

    public static SpellRayEntity empty(EntityType<?> type, World world) {
        return new SpellRayEntity(type, world);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(MAX_LENGTH, 32f);
        this.dataTracker.startTracking(LENGTH, 1f);
        this.dataTracker.startTracking(IS_GROWING, true);
    }

    public void setCast(SpellCast cast) {
        this.cast = cast;
    }
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
    public void setGrowthSpeed(float growthSpeed) {
        this.growthSpeed = growthSpeed;
    }
    public float getGrowthSpeed() {
        return this.growthSpeed;
    }

    public void setMaxLength(float maxLength) {
        this.dataTracker.set(MAX_LENGTH, maxLength);
    }

    public float getLength() {
        return this.dataTracker.get(LENGTH);
    }

    public float getLength(float tickDelta) {
        if (!this.dataTracker.get(IS_GROWING)) {
            return this.getLength();
        }
        var length = this.getLength();
        return MathHelper.lerp(tickDelta, length-growthSpeed, length);
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5F;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(MAX_LENGTH, nbt.getFloat("maxLength"));
        this.dataTracker.set(LENGTH, nbt.getFloat("length"));
        this.dataTracker.set(IS_GROWING, nbt.getBoolean("isGrowing"));
        this.cast = SpellCast.readFromNbt(nbt.getCompound("cast"), this.world);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("maxLength", this.dataTracker.get(MAX_LENGTH));
        nbt.putFloat("length", this.dataTracker.get(LENGTH));
        nbt.putBoolean("isGrowing", this.dataTracker.get(IS_GROWING));

        var castNBT = new NbtCompound();
        this.cast.writeToNbt(castNBT);
        nbt.put("cast", castNBT);
    }

    protected boolean canHit(Entity entity) {
        if (entity.isSpectator() || !entity.isAlive() || !entity.canHit()) {
            return false;
        }
        Entity entity2 = /*this.getOwner()*/ this;
        return entity2 == null || !entity2.isConnectedThroughVehicle(entity);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        float length = this.dataTracker.get(LENGTH);
        var tip = this.getPos().add(this.getRotationVector().normalize().multiply(length));
        var diagonal = this.getPos().subtract(tip);
        return EntityDimensions.changing((float)diagonal.x, (float)diagonal.y);
    }

    /**
     * Вызывается при попадании луча в блок.
     *
     * @param bhr Информация о попадании в блок, всегда не MISS.
     * @return Должен ли луч продолжить распространяться, проходя сковзь блок.
     */
    protected boolean onBlockHit(BlockHitResult bhr) {
        this.cast.getSpell().onSingleBlockHit(this.cast, bhr.getBlockPos(), bhr.getSide(), this.cast.getEffectReactions());
        return false;
    }

    /**
     * Вызывается при попадании луча в Entity (не обязательно живое).
     * <p>
     * Может вызываться для "владельца" луча (заклинателя) - возможно попасть
     * заклинанием в себя, например, если оно несколько раз отскочет от стен.
     *
     * @param ehr Информация о попадании в Entity, всегда не MISS.
     * @return Должен ли луч продолжить распространяться, проходя сковзь entity.
     */
    protected boolean onEntityHit(EntityHitResult ehr) {
        this.cast.getSpell().onSingleEntityHit(this.cast, ehr.getEntity(), this.cast.getEffectReactions());
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient) {
            return;
        }

        if (this.age >= this.maxAge) {
            this.discard();
        }
        float length = this.dataTracker.get(LENGTH);
        float maxLength = this.dataTracker.get(MAX_LENGTH);
        if (maxLength - length < 0.01f) {
            this.dataTracker.set(IS_GROWING, false);
        }

        if (length <= maxLength && this.dataTracker.get(IS_GROWING)) {
            var raycastStart = this.getPos().add(this.getRotationVector().normalize().multiply(length - 0.1f));
            var raycastEnd = this.getPos().add(this.getRotationVector().normalize().multiply(length + this.growthSpeed));
            var hitResult = this.world.raycast(new RaycastContext(
                    raycastStart, raycastEnd,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                    this));
            if (hitResult.getType() != HitResult.Type.MISS) {
                // TODO: Bounce check here.
                this.dataTracker.set(LENGTH, (float)hitResult.getPos().distanceTo(this.getPos()));
                this.dataTracker.set(IS_GROWING, false);
                raycastEnd = hitResult.getPos();
            }
            var entHitResult = ProjectileUtil.getEntityCollision(world, this, raycastStart, raycastEnd,
                    new Box(raycastStart, raycastEnd), this::canHit);
            if (entHitResult != null && entHitResult.getType() != HitResult.Type.MISS) {
                if (!this.onEntityHit(entHitResult)) {
                    // TODO: Bounce check here.
                    this.dataTracker.set(LENGTH, (float)entHitResult.getPos().distanceTo(this.getPos()));
                    this.dataTracker.set(IS_GROWING, false);
                }
                return;
            } else if (hitResult.getType() != HitResult.Type.MISS) {
                this.onBlockHit(hitResult);
            }

            length += this.growthSpeed;
            this.dataTracker.set(LENGTH, length);
        }
    }
}
