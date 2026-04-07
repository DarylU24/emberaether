package com.emberaether.entity;

import com.emberaether.entity.goal.DrakeFireBreathGoal;
import com.emberaether.entity.goal.DrakeRoarAttackGoal;
import com.emberaether.registry.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Infernal Drake — heavy quadruped tank.
 * Biome: Nether Wastes / Basalt Deltas.
 * Attack: short-range fire cone (AoE), applies Fire for 5 seconds.
 * Tamed via "Dominance": reduce to 20% HP then feed Cooked Beef.
 */
public class InfernalDrakeEntity extends TamableAnimal implements GeoEntity {

    // ─── Data parameters ───────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> ROARING =
            SynchedEntityData.defineId(InfernalDrakeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BREATHING_FIRE =
            SynchedEntityData.defineId(InfernalDrakeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(InfernalDrakeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STAMINA =
            SynchedEntityData.defineId(InfernalDrakeEntity.class, EntityDataSerializers.FLOAT);

    // ─── GeckoLib ──────────────────────────────────────────────────────────────
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM     = RawAnimation.begin().thenLoop("animation.infernal_drake.idle");
    private static final RawAnimation WALK_ANIM     = RawAnimation.begin().thenLoop("animation.infernal_drake.walk");
    private static final RawAnimation ROAR_ANIM     = RawAnimation.begin().then("animation.infernal_drake.roar", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FIRE_ANIM     = RawAnimation.begin().then("animation.infernal_drake.fire_breath", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FLY_ANIM      = RawAnimation.begin().thenLoop("animation.infernal_drake.fly");

    // ─── Constructor ───────────────────────────────────────────────────────────
    public InfernalDrakeEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    // ─── Data ──────────────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ROARING, false);
        entityData.define(BREATHING_FIRE, false);
        entityData.define(SADDLED, false);
        entityData.define(STAMINA, 100.0f);
    }

    public boolean isRoaring()      { return entityData.get(ROARING); }
    public boolean isBreathingFire(){ return entityData.get(BREATHING_FIRE); }
    public boolean isSaddled()      { return entityData.get(SADDLED); }
    public float   getStamina()     { return entityData.get(STAMINA); }

    public void setRoaring(boolean v)      { entityData.set(ROARING, v); }
    public void setBreathingFire(boolean v){ entityData.set(BREATHING_FIRE, v); }
    public void setSaddled(boolean v)      { entityData.set(SADDLED, v); }
    public void setStamina(float v)        { entityData.set(STAMINA, Math.max(0, Math.min(100, v))); }

    // ─── NBT ───────────────────────────────────────────────────────────────────
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Saddled", isSaddled());
        tag.putFloat("Stamina", getStamina());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSaddled(tag.getBoolean("Saddled"));
        setStamina(tag.getFloat("Stamina"));
    }

    // ─── Goals ─────────────────────────────────────────────────────────────────
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new DrakeFireBreathGoal(this));
        goalSelector.addGoal(3, new DrakeRoarAttackGoal(this, 1.0));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ─── Taming interaction ────────────────────────────────────────────────────
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!isTame()) {
            // Dominance taming: HP <= 20%, feed Cooked Beef
            if (stack.is(Items.COOKED_BEEF) && getHealth() <= getMaxHealth() * 0.2f) {
                if (!level().isClientSide) {
                    tame(player);
                    stack.shrink(1);
                    level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        } else if (isTame() && isOwnedBy(player)) {
            // Saddle
            if (!isSaddled() && stack.is(ItemRegistry.DRAGON_SADDLE.get())) {
                setSaddled(true);
                stack.shrink(1);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            // Mount
            if (isSaddled() && !player.isShiftKeyDown()) {
                player.startRiding(this);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    // ─── Breeding ──────────────────────────────────────────────────────────────
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BLAZE_POWDER);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        // Spawn a DragonEgg entity instead of a baby mob
        DragonEggEntity egg = new DragonEggEntity(com.emberaether.registry.EntityRegistry.DRAGON_EGG.get(), level);
        egg.setPos(getX(), getY(), getZ());
        level.addFreshEntity(egg);
        return null;
    }

    // ─── Breath attack ─────────────────────────────────────────────────────────
    public void triggerRoar() {
        setRoaring(true);
        level().broadcastEntityEvent(this, (byte) 50);
    }

    public void performFireBreath(LivingEntity target) {
        setBreathingFire(true);
        if (level().isClientSide) return;

        // AoE: ignite all entities in a cone in front of the Drake
        double coneRange = 6.0;
        AABB area = getBoundingBox().inflate(coneRange);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e.isAlive());

        for (LivingEntity entity : nearby) {
            // Simple cone check: entity must be roughly in front
            double dot = getLookAngle().dot(entity.position().subtract(position()).normalize());
            if (dot > 0.5) {
                entity.setSecondsOnFire(5);
                entity.hurt(damageSources().onFire(), 8.0f);
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    @Nullable
    public Player findNearestPlayer(double range) {
        return level().getNearestPlayer(this, range);
    }

    // ─── GeckoLib ──────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, state -> {
            if (isBreathingFire()) return state.setAndContinue(FIRE_ANIM);
            if (isRoaring())       return state.setAndContinue(ROAR_ANIM);
            if (state.isMoving())  return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
