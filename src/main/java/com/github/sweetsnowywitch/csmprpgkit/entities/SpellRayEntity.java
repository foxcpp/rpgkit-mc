package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpellRayEntity extends Entity {
    // Populated only on logical server side.
    private @Nullable SpellRayEntity parent = null;

    // Populated only on logical server side.
    public ServerSpellCast cast = null;
    private int maxAge = 10*20;
    private float growthSpeed = 2;
    private int totalBounces = 0;
    private int remainingBounces = 0;
    public static final TrackedData<Float> MAX_LENGTH = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> LENGTH = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Boolean> IS_GROWING = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<SpellCast> CAST = DataTracker.registerData(SpellRayEntity.class, SpellCast.TRACKED_HANDLER);

    public int rayBaseColor = 0x88FFFFFF;

    public SpellRayEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
    }

    public static SpellRayEntity empty(EntityType<?> type, World world) {
        return new SpellRayEntity(type, world);
    }

    private static SpellRayEntity nextSegment(SpellRayEntity previous, Vec3d origin, Vec3d dir) {
        var seg = new SpellRayEntity(previous.getType(), previous.world);
        seg.setCast(previous.cast);
        seg.growthSpeed = previous.growthSpeed;
        seg.parent = previous.parent;
        seg.setMaxLength(previous.getMaxLength() - previous.getLength());
        seg.setPosition(origin);
        seg.remainingBounces = previous.remainingBounces - 1;
        seg.totalBounces = previous.totalBounces;

        seg.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, origin.add(dir));

        return seg;
    }

    @Override
    public boolean isPartOf(Entity entity) {
        if (this.parent == null) {
            return false;
        }
        return entity.equals(this.parent);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(MAX_LENGTH, 100f);
        this.dataTracker.startTracking(LENGTH, 1f);
        this.dataTracker.startTracking(IS_GROWING, true);
        this.dataTracker.startTracking(CAST, SpellCast.EMPTY);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (CAST.equals(data)) {
            this.rayBaseColor = 0xFFFFFFFF;
            for (var element : cast.getFullRecipe()) {
                this.rayBaseColor = ColorHelper.Argb.mixColor(0, element.getColor());
            }
        }
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
    public float getMaxLength() {
        return this.dataTracker.get(MAX_LENGTH);
    }

    public void setTotalBounces(int count) {
        this.remainingBounces = count;
        this.totalBounces = count;
    }
    public int getRemainingBounces() {
        return this.remainingBounces;
    }
    public int getTotalBounces() {
        return this.totalBounces;
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

    public @Nullable SpellRayEntity getParent() {
        return this.parent;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5F;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(MAX_LENGTH, nbt.getFloat("MaxLength"));
        this.dataTracker.set(LENGTH, nbt.getFloat("Length"));
        this.dataTracker.set(IS_GROWING, nbt.getBoolean("IsGrowing"));
        this.cast = ServerSpellCast.readFromNbt(nbt.getCompound("Cast")); // full data for server
        this.dataTracker.set(CAST, this.cast); // sync some spell data to client
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("MaxLength", this.dataTracker.get(MAX_LENGTH));
        nbt.putFloat("Length", this.dataTracker.get(LENGTH));
        nbt.putBoolean("IsGrowing", this.dataTracker.get(IS_GROWING));

        var castNBT = new NbtCompound();
        if (this.cast != null) {
            // Server with full data.
            this.cast.writeToNbt(castNBT);
        } else {
            // Client with partial data.
            this.dataTracker.get(CAST).writeToNbt(castNBT);
        }
        nbt.put("Cast", castNBT);
    }

    protected boolean canHit(Entity entity) {
        if (entity.isSpectator() || !entity.isAlive() || !entity.canHit()) {
            return false;
        }

        // Bounced rays can hit caster.
        if (this.parent != null) {
            return true;
        }

        // Prevent first ray segment from accidentally colliding with caster.
        Entity owner = ((ServerWorld)this.world).getEntity(this.cast.getCasterUuid());
        return owner == null || !owner.isConnectedThroughVehicle(entity);
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
        if (this.cast == null) {
            return false;
        }
        this.cast.getSpell().onSingleBlockHit(this.cast, (ServerWorld)this.world, bhr.getBlockPos(), bhr.getSide());
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
        this.cast.getSpell().onSingleEntityHit(this.cast, ehr.getEntity());
        return false;
    }

    private Vec3d bounceDirection(BlockHitResult bhr) {
        var side = bhr.getSide();
        var rotation = this.getRotationVector();

        var factor = this.cast.getCost(SpellElement.COST_INTERITIO) * 0.3f + 0.1f;
        var degreesDelta = RPGKitMod.RANDOM.nextFloat(factor) - factor/2;

        var bounce = switch (side.getAxis()) {
            case X -> rotation.rotateX(180+degreesDelta);
            case Y -> rotation.rotateY(180+degreesDelta);
            case Z -> rotation.rotateZ(180+degreesDelta);
        };
        return bounce.multiply(-1);
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
            var raycastStart = this.getPos().add(this.getRotationVector().multiply(length - 0.1f));
            var raycastEnd = this.getPos().add(this.getRotationVector().multiply(length + this.growthSpeed));
            BlockHitResult hitResult = this.world.raycast(new RaycastContext(
                    raycastStart, raycastEnd,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                    this));
            if (hitResult.getType() != HitResult.Type.MISS) {
                raycastEnd = hitResult.getPos();
            }
            var entHitResult = ProjectileUtil.getEntityCollision(world, this, raycastStart, raycastEnd,
                    new Box(raycastStart, raycastEnd), this::canHit);
            if (entHitResult != null && entHitResult.getType() != HitResult.Type.MISS) {
                if (!this.onEntityHit(entHitResult)) {
                    this.dataTracker.set(LENGTH, (float)entHitResult.getPos().distanceTo(this.getPos()));
                    this.dataTracker.set(IS_GROWING, false);
                }
                return;
            } else if (hitResult.getType() != HitResult.Type.MISS) {
                var block = this.world.getBlockState(hitResult.getBlockPos());
                var glassBlocksTag = TagKey.of(RegistryKeys.BLOCK, Identifier.of("c", "glass_blocks"));
                var glassPanesTag = TagKey.of(RegistryKeys.BLOCK, Identifier.of("c", "glass_panes"));
                if (!block.isIn(glassBlocksTag) && !block.isIn(glassPanesTag)) {
                    if (!this.onBlockHit(hitResult)) {
                        if (this.remainingBounces > 0 && (maxLength - length) > 1f) {
                            var dir = bounceDirection(hitResult);
                            var ent = nextSegment(this, hitResult.getPos(), dir);
                            this.world.spawnEntity(ent);
                        }
                        this.dataTracker.set(LENGTH, (float)hitResult.getPos().distanceTo(this.getPos()));
                        this.dataTracker.set(IS_GROWING, false);
                    }
                    return;

                }
            }

            length += this.growthSpeed;
            this.dataTracker.set(LENGTH, length);
        }
    }
}
