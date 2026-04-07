package com.darylu24.emberaether.entity;

import com.darylu24.emberaether.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class EarthscaleDragonEntity extends AbstractDragonEntity {
    private static final DataParameter<Boolean> BURROWING = EntityDataManager.defineId(EarthscaleDragonEntity.class, DataSerializers.BOOLEAN);
    private int burrowTicks;

    public EarthscaleDragonEntity(EntityType<? extends EarthscaleDragonEntity> entityType, World world) {
        super(entityType, world);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BURROWING, false);
    }

    @Override
    public DragonBreed getBreed() {
        return DragonBreed.EARTHSCALE;
    }

    @Override
    public DragonVariant[] getVariants() {
        return EarthscaleVariant.values();
    }

    public boolean isBurrowing() {
        return this.entityData.get(BURROWING);
    }

    public void setBurrowing(boolean burrowing) {
        this.entityData.set(BURROWING, burrowing);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.getTarget() == null && this.onGround && this.getDeltaMovement().horizontalDistanceSqr() < 0.003D && this.random.nextInt(220) == 0) {
                setBurrowing(true);
                this.burrowTicks = 60;
            }

            if (isBurrowing()) {
                this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
                if (this.tickCount % 5 == 0) {
                    BlockPos pos = this.blockPosition().below();
                    BlockState state = this.level.getBlockState(pos);
                    if (!state.isAir()) {
                        ((ServerWorld) this.level).sendParticles(
                                new BlockParticleData(ParticleTypes.BLOCK, state),
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                10,
                                0.6D,
                                0.1D,
                                0.6D,
                                0.01D
                        );
                    }
                }

                if (--this.burrowTicks <= 0 || this.getTarget() != null) {
                    setBurrowing(false);
                }
            }
        }
    }

    @Override
    public boolean isPushable() {
        return !isBurrowing() && super.isPushable();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isBurrowing() && source != DamageSource.OUT_OF_WORLD) {
            amount *= 0.5F;
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public AgeableEntity getBreedOffspring(ServerWorld serverWorld, AgeableEntity partner) {
        EarthscaleDragonEntity baby = ModEntities.EARTHSCALE_DRAGON.get().create(serverWorld);
        if (baby == null) {
            return null;
        }

        int index = this.random.nextBoolean()
                ? this.getVariantIndex()
                : ((EarthscaleDragonEntity) partner).getVariantIndex();
        if (this.random.nextInt(4) == 0) {
            index = this.random.nextInt(getVariants().length);
        }
        baby.setVariantIndex(index);
        return baby;
    }
}
