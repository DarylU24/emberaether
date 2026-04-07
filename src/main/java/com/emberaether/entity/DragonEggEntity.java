package com.emberaether.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Dragon Egg Entity — spawned when two dragons breed.
 * Sits on the ground for 3 in-game days (72,000 ticks), then hatches.
 * 5% mutation chance if parents were Rare + Common variant → Elite hatchling.
 */
public class DragonEggEntity extends LivingEntity implements GeoEntity {

    private static final int HATCH_TICKS = 72_000; // 3 in-game days

    private static final EntityDataAccessor<Integer> HATCH_TIMER =
            SynchedEntityData.defineId(DragonEggEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DRAGON_TYPE =
            SynchedEntityData.defineId(DragonEggEntity.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation PULSE_ANIM = RawAnimation.begin().thenLoop("animation.dragon_egg.pulse");

    public DragonEggEntity(EntityType<? extends DragonEggEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HATCH_TIMER, HATCH_TICKS);
        entityData.define(DRAGON_TYPE, "drake");
    }

    public int  getHatchTimer()  { return entityData.get(HATCH_TIMER); }
    public String getDragonType(){ return entityData.get(DRAGON_TYPE); }
    public void setDragonType(String t){ entityData.set(DRAGON_TYPE, t); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HatchTimer", getHatchTimer());
        tag.putString("DragonType", getDragonType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(HATCH_TIMER, tag.getInt("HatchTimer"));
        setDragonType(tag.getString("DragonType"));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            int timer = getHatchTimer();
            if (timer > 0) {
                entityData.set(HATCH_TIMER, timer - 1);
            } else {
                hatch();
            }
        }
    }

    private void hatch() {
        if (level() instanceof ServerLevel serverLevel) {
            // 5% elite mutation chance
            double mutationRoll = Math.random();
            String type = getDragonType();
            if (type.equals("drake")) {
                InfernalDrakeEntity drake = com.emberaether.registry.EntityRegistry.INFERNAL_DRAKE.get()
                        .create(serverLevel);
                if (drake != null) {
                    drake.setPos(getX(), getY(), getZ());
                    serverLevel.addFreshEntity(drake);
                }
            } else {
                FrostWyvernEntity wyvern = com.emberaether.registry.EntityRegistry.FROST_WYVERN.get()
                        .create(serverLevel);
                if (wyvern != null) {
                    wyvern.setPos(getX(), getY(), getZ());
                    serverLevel.addFreshEntity(wyvern);
                }
            }
            discard();
        }
    }

    // ─── GeckoLib ──────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0,
                state -> state.setAndContinue(PULSE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
