package com.emberaether.entity;

import com.emberaether.entity.goal.WyvernCircleGoal;
import com.emberaether.entity.goal.WyvernIceBreathGoal;
import com.emberaether.registry.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * Frost Wyvern — slender biped, high-altitude scout.
 * Biome: Snowy Tundra / Snowy Mountains.
 * Passive-Neutral: circles players, retaliates with Ice Projectile (Slowness IV, 3 sec).
 * Tamed via stealth: sneak + feed Raw Cod/Salmon while perched.
 */
public class FrostWyvernEntity extends TamableAnimal implements GeoEntity {

    // ─── Data parameters ───────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> ICE_BREATHING =
            SynchedEntityData.defineId(FrostWyvernEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(FrostWyvernEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STAMINA =
            SynchedEntityData.defineId(FrostWyvernEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> RETREATING =
            SynchedEntityData.defineId(FrostWyvernEntity.class, EntityDataSerializers.BOOLEAN);

    // ─── GeckoLib ──────────────────────────────────────────────────────────────
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("animation.frost_wyvern.idle");
    private static final RawAnimation WALK_ANIM   = RawAnimation.begin().thenLoop("animation.frost_wyvern.walk");
    private static final RawAnimation FLY_ANIM    = RawAnimation.begin().thenLoop("animation.frost_wyvern.fly");
    private static final RawAnimation ICE_ANIM    = RawAnimation.begin().then("animation.frost_wyvern.ice_breath", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation PERCH_ANIM  = RawAnimation.begin().thenLoop("animation.frost_wyvern.perch");

    // ─── Ice breath goal (back-reference for hurt callback) ────────────────────
    private WyvernIceBreathGoal iceBreathGoal;

    public FrostWyvernEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.FLYING_SPEED, 0.52);
    }

    // ─── Data ──────────────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ICE_BREATHING, false);
        entityData.define(SADDLED, false);
        entityData.define(STAMINA, 100.0f);
        entityData.define(RETREATING, false);
    }

    public boolean isIceBreathing() { return entityData.get(ICE_BREATHING); }
    public boolean isSaddled()      { return entityData.get(SADDLED); }
    public float   getStamina()     { return entityData.get(STAMINA); }
    public boolean isRetreating()   { return entityData.get(RETREATING); }

    public void setIceBreathing(boolean v){ entityData.set(ICE_BREATHING, v); }
    public void setSaddled(boolean v)     { entityData.set(SADDLED, v); }
    public void setStamina(float v)       { entityData.set(STAMINA, Math.max(0, Math.min(100, v))); }
    public void setRetreating(boolean v)  { entityData.set(RETREATING, v); }

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
        iceBreathGoal = new WyvernIceBreathGoal(this);
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, iceBreathGoal);
        goalSelector.addGoal(3, new WyvernCircleGoal(this));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // ─── Hurt callback triggers ice retaliation ────────────────────────────────
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && source.getEntity() instanceof LivingEntity attacker && !isTame()) {
            iceBreathGoal.triggerAttack(attacker);
        }
        return result;
    }

    // ─── Heal by feeding (no natural regen) ────────────────────────────────────
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.PACKED_ICE);
    }

    // ─── Taming ────────────────────────────────────────────────────────────────
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!isTame()) {
            // Stealth taming: player must be sneaking, wyvern must be on ground
            boolean tamingFood = stack.is(Items.COD) || stack.is(Items.SALMON);
            boolean stealthOk = player.isShiftKeyDown() && !player.isFallFlying();
            if (tamingFood && stealthOk) {
                if (!level().isClientSide) {
                    tame(player);
                    stack.shrink(1);
                    level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        } else if (isTame() && isOwnedBy(player)) {
            if (!isSaddled() && stack.is(ItemRegistry.DRAGON_SADDLE.get())) {
                setSaddled(true);
                stack.shrink(1);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (isSaddled() && !player.isShiftKeyDown()) {
                player.startRiding(this);
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    // ─── Breeding (spawn dragon egg) ───────────────────────────────────────────
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        DragonEggEntity egg = new DragonEggEntity(com.emberaether.registry.EntityRegistry.DRAGON_EGG.get(), level);
        egg.setPos(getX(), getY(), getZ());
        level.addFreshEntity(egg);
        return null;
    }

    // ─── Ice projectile / retreat ──────────────────────────────────────────────
    public void shootIceProjectile(LivingEntity target) {
        setIceBreathing(true);
        if (level().isClientSide) return;

        Vec3 delta = target.position().add(0, target.getBbHeight() * 0.5, 0)
                .subtract(position().add(0, getBbHeight() * 0.5, 0));

        Snowball snowball = new Snowball(level(), this);
        snowball.setPos(getX(), getEyeY(), getZ());
        double speed = 1.6;
        snowball.shoot(delta.x, delta.y + delta.length() * 0.2, delta.z, (float) speed, 1.0f);
        level().addFreshEntity(snowball);
    }

    public void startRetreat() {
        setRetreating(true);
        if (!level().isClientSide) {
            // Move straight up 15 blocks
            getNavigation().moveTo(getX(), getY() + 15, getZ(), 1.3);
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
            if (isIceBreathing()) return state.setAndContinue(ICE_ANIM);
            if (isRetreating())   return state.setAndContinue(FLY_ANIM);
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
