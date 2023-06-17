package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.github.sweetsnowywitch.csmprpgkit.particle.GenericSpellParticleEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SpellRayEntity extends Entity {
    // Populated only on logical server side.
    public ServerSpellCast cast = null;
    private int maxAge;
    public static final TrackedData<Float> LENGTH = DataTracker.registerData(SpellRayEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<SpellCast> CAST = DataTracker.registerData(SpellRayEntity.class, SpellCast.TRACKED_HANDLER);
    public Vec3d aimOrigin;

    public int rayBaseColor = 0x00FFFFFF; // ARGB, calculated on client-side only
    protected ParticleEffect particleEffect; // calcualted on client-side only
    private Vec3d previousBlockHit;
    private Entity previousEntityHit;

    public SpellRayEntity(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
        this.maxAge = 3 * 20;

        this.previousBlockHit = null;
        this.previousEntityHit = null;
    }

    public static SpellRayEntity empty(EntityType<?> type, World world) {
        return new SpellRayEntity(type, world);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(LENGTH, 1f);
        this.dataTracker.startTracking(CAST, SpellCast.EMPTY);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(LENGTH, nbt.getFloat("Length"));
        this.cast = ServerSpellCast.readFromNbt(nbt.getCompound("Cast")); // full data for server
        this.dataTracker.set(CAST, this.cast); // sync some spell data to client

        if (nbt.contains("AimOrigin")) {
            var pos = nbt.getCompound("AimOrigin");
            this.aimOrigin = new Vec3d(pos.getDouble("X"), pos.getDouble("Y"), pos.getDouble("Z"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("Length", this.dataTracker.get(LENGTH));

        var castNBT = new NbtCompound();
        if (this.cast != null) {
            // Server with full data.
            this.cast.writeToNbt(castNBT);
        } else {
            // Client with partial data.
            this.dataTracker.get(CAST).writeToNbt(castNBT);
        }
        nbt.put("Cast", castNBT);

        if (this.aimOrigin != null) {
            var pos = new NbtCompound();
            pos.putDouble("X", this.aimOrigin.getX());
            pos.putDouble("Y", this.aimOrigin.getY());
            pos.putDouble("Z", this.aimOrigin.getZ());
            nbt.put("AimOrigin", pos);
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (CAST.equals(data)) {
            this.rayBaseColor = SpellElement.calculateBaseColor(this.dataTracker.get(CAST).getFullRecipe());
            this.particleEffect = new GenericSpellParticleEffect(this.rayBaseColor, 2);
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

    public float getLength() {
        return this.dataTracker.get(LENGTH);
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5F;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    protected boolean canHit(Entity entity) {
        if (entity.isSpectator() || !entity.isAlive() || !entity.canHit()) {
            return false;
        }

        // Prevent first ray segment from accidentally colliding with caster.
        Entity owner = ((ServerWorld) this.world).getEntity(this.cast.getCasterUuid());
        return owner == null || !owner.isConnectedThroughVehicle(entity);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        float length = this.dataTracker.get(LENGTH);
        var tip = this.getPos().add(this.getRotationVector().normalize().multiply(length));
        var diagonal = this.getPos().subtract(tip);
        return EntityDimensions.changing((float) diagonal.x, (float) diagonal.y);
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
        return this.cast.getSpell().onSingleBlockHit(this.cast, (ServerWorld) this.world, bhr.getBlockPos(), bhr.getSide());
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

    public void setAimOrigin(Vec3d pos) {
        this.aimOrigin = pos;
    }

    public Vec3d getAimOrigin() {
        if (this.aimOrigin == null) {
            return this.getPos();
        }
        return this.aimOrigin;
    }

    protected void spawnParticles() {
        if (this.age % 20 < 10) {
            return;
        }

        var len = this.getLength();

        if (len <= 1) {
            return;
        }

        double x = this.getX(), y = this.getY(), z = this.getZ();
        var i = this.random.nextInt((int) len);
        var rot = this.getRotationVector();
        x += i * rot.x;
        y += i * rot.y;
        z += i * rot.z;
        this.world.addParticle(this.particleEffect,
                x, y, z, 0, 0, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient) {
            this.spawnParticles();
            return;
        }

        if (this.age >= this.maxAge) {
            this.discard();
        }

        // Raycast to aim.
        var raycastStart = this.getAimOrigin();
        var raycastEnd = this.getAimOrigin().add(this.getRotationVector().multiply(50f));
        BlockHitResult hitResult = this.world.raycast(new RaycastContext(
                raycastStart, raycastEnd,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                this));
        if (hitResult.getType() != HitResult.Type.MISS) {
            raycastEnd = hitResult.getPos().add(this.getRotationVector());
        }
        var entHitResult = ProjectileUtil.getEntityCollision(world, this, raycastStart, raycastEnd,
                new Box(raycastStart, raycastEnd), this::canHit);
        if (entHitResult != null && entHitResult.getType() != HitResult.Type.MISS) {
            raycastEnd = entHitResult.getPos().add(this.getRotationVector());
        }

        // Raycast to hit.
        raycastStart = this.getPos();
        hitResult = this.world.raycast(new RaycastContext(
                raycastStart, raycastEnd,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                this));
        if (hitResult.getType() != HitResult.Type.MISS) {
            raycastEnd = hitResult.getPos();
        }
        entHitResult = ProjectileUtil.getEntityCollision(world, this, raycastStart, raycastEnd,
                new Box(raycastStart, raycastEnd), this::canHit);
        if (entHitResult != null && entHitResult.getType() != HitResult.Type.MISS) {
            // Skip hits to make effect spam less significant.
            if (this.previousEntityHit != null && entHitResult.getEntity().equals(this.previousEntityHit)) {
                this.cast.getSpell().onSingleEntityHold(this.cast, entHitResult.getEntity());
                return;
            }

            if (!this.onEntityHit(entHitResult)) {
                this.dataTracker.set(LENGTH, (float) entHitResult.getPos().distanceTo(this.getPos()));
            }

            this.previousBlockHit = null;
            this.previousEntityHit = entHitResult.getEntity();
        } else if (hitResult.getType() != HitResult.Type.MISS) {
            // Skip hits to make effect spam less significant.
            if (this.previousBlockHit != null && hitResult.getPos().squaredDistanceTo(this.previousBlockHit) <= 1) {
                return;
            }

            if (!this.onBlockHit(hitResult)) {
                this.dataTracker.set(LENGTH, (float) hitResult.getPos().distanceTo(this.getPos()));
            }

            this.previousBlockHit = hitResult.getPos();
            this.previousEntityHit = null;
        } else {
            this.dataTracker.set(LENGTH, 50f);
        }
    }
}
