package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SpellChargeEntity extends PersistentProjectileEntity {
    public static final TrackedData<SpellCast> CAST = DataTracker.registerData(SpellChargeEntity.class, SpellCast.TRACKED_HANDLER);

    // Populated only on logical server side.
    public ServerSpellCast cast = null;
    private int maxAge = 3*20;

    public int baseColor = 0x00FFFFFF; // ARGB, calculated on client-side only

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
        this.maxAge = nbt.getInt("MaxAge");
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (CAST.equals(data)) {
            this.baseColor = SpellElement.calculateBaseColor(this.dataTracker.get(CAST).getFullRecipe());
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
        nbt.putInt("MaxAge", this.maxAge);
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

        this.cast.getSpell().onSingleEntityHit(this.cast, ehr.getEntity());
    }

    @Override
    protected void onBlockHit(BlockHitResult bhr) {
        this.calculateDimensions();

        if (!this.world.isClient) {
            this.cast.getSpell().onSingleBlockHit(this.cast, (ServerWorld)this.world, bhr.getBlockPos(), bhr.getSide());
        }

        super.onBlockHit(bhr);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getVelocity().lengthSquared() > 0f) {

        }

        if (this.inGround) {
            this.discard();
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }
}
