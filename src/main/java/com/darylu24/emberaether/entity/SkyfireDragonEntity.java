package com.darylu24.emberaether.entity;

import com.darylu24.emberaether.registry.ModEntities;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class SkyfireDragonEntity extends AbstractDragonEntity {
    public SkyfireDragonEntity(EntityType<? extends SkyfireDragonEntity> entityType, World world) {
        super(entityType, world);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 42.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public DragonBreed getBreed() {
        return DragonBreed.SKYFIRE;
    }

    @Override
    public DragonVariant[] getVariants() {
        return SkyfireVariant.values();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide && this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 49.0D && this.random.nextInt(40) == 0) {
            this.attackAnimationTicks = 12;
            this.getTarget().setSecondsOnFire(3);
            ((ServerWorld) this.level).sendParticles(
                    ParticleTypes.FLAME,
                    this.getX(),
                    this.getY(0.8D),
                    this.getZ(),
                    20,
                    0.4D,
                    0.2D,
                    0.4D,
                    0.01D
            );
        }
    }

    @Nullable
    @Override
    public AgeableEntity getBreedOffspring(ServerWorld serverWorld, AgeableEntity partner) {
        SkyfireDragonEntity baby = ModEntities.SKYFIRE_DRAGON.get().create(serverWorld);
        if (baby == null) {
            return null;
        }

        int index = this.random.nextBoolean()
                ? this.getVariantIndex()
                : ((SkyfireDragonEntity) partner).getVariantIndex();
        if (this.random.nextInt(4) == 0) {
            index = this.random.nextInt(getVariants().length);
        }
        baby.setVariantIndex(index);
        return baby;
    }
}
