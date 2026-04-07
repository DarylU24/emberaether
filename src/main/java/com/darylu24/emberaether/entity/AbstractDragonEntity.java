package com.darylu24.emberaether.entity;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.FloatGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public abstract class AbstractDragonEntity extends AnimalEntity {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.defineId(AbstractDragonEntity.class, DataSerializers.INT);

    protected int attackAnimationTicks;
    protected float jawOpen;

    protected AbstractDragonEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
    }

    public abstract DragonBreed getBreed();

    public abstract DragonVariant[] getVariants();

    public DragonVariant getVariant() {
        DragonVariant[] variants = getVariants();
        int index = MathHelper.clamp(this.entityData.get(VARIANT), 0, variants.length - 1);
        return variants[index];
    }

    public void setVariantIndex(int index) {
        this.entityData.set(VARIANT, MathHelper.clamp(index, 0, getVariants().length - 1));
    }

    public int getVariantIndex() {
        return this.entityData.get(VARIANT);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", getVariantIndex());
        compound.putInt("AttackAnim", this.attackAnimationTicks);
        compound.putFloat("JawOpen", this.jawOpen);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.setVariantIndex(compound.getInt("Variant"));
        this.attackAnimationTicks = compound.getInt("AttackAnim");
        this.jawOpen = compound.getFloat("JawOpen");
    }

    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData spawnData, @Nullable CompoundNBT dataTag) {
        ILivingEntityData livingData = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        this.setVariantIndex(this.random.nextInt(getVariants().length));
        return livingData;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
            this.jawOpen = Math.min(1.0F, this.jawOpen + 0.15F);
        } else {
            this.jawOpen = Math.max(0.0F, this.jawOpen - 0.1F);
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        this.attackAnimationTicks = 12;
        return super.doHurtTarget(entity);
    }

    public float getJawOpen() {
        return this.jawOpen;
    }

    public float getAttackAnimationScale(float partialTick) {
        return MathHelper.clamp((this.attackAnimationTicks - partialTick) / 12.0F, 0.0F, 1.0F);
    }

    public boolean isDragonFlying() {
        return !this.onGround && this.getDeltaMovement().lengthSqr() > 0.03D;
    }

    @Override
    public boolean canMate(AnimalEntity otherAnimal) {
        return otherAnimal != this && this.getClass() == otherAnimal.getClass() && super.canMate(otherAnimal);
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!this.isBaby() && !player.isSecondaryUseActive() && held.isEmpty()) {
            if (!this.level.isClientSide) {
                player.startRiding(this);
            }
            return ActionResultType.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(net.minecraft.util.math.vector.Vector3d travelVector) {
        if (this.isAlive() && this.isVehicle() && this.canBeControlledByRider()) {
            LivingEntity rider = (LivingEntity) this.getControllingPassenger();
            if (rider != null) {
                this.yRot = rider.yRot;
                this.yRotO = this.yRot;
                this.xRot = rider.xRot * 0.5F;
                this.setRot(this.yRot, this.xRot);
                this.yBodyRot = this.yRot;
                this.yHeadRot = this.yBodyRot;
                float forward = rider.zza;
                float strafe = rider.xxa * 0.4F;
                this.setSpeed((float) this.getAttribute(net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getValue() * getVariant().speedMultiplier());
                super.travel(new net.minecraft.util.math.vector.Vector3d(strafe, travelVector.y, forward));
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    protected boolean canBeControlledByRider() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
    }

    @Override
    public Pose getPose() {
        return Pose.STANDING;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
