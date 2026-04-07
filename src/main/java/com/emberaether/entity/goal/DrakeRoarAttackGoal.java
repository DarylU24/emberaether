package com.emberaether.entity.goal;

import com.emberaether.entity.InfernalDrakeEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * The Drake roars before charging: it locks its target, plays the roar animation,
 * then launches a melee rush toward the target.
 */
public class DrakeRoarAttackGoal extends Goal {
    private static final int ROAR_DURATION_TICKS = 40;

    private final InfernalDrakeEntity drake;
    private final double speedModifier;
    private LivingEntity target;
    private int ticksUntilCharge;

    public DrakeRoarAttackGoal(InfernalDrakeEntity drake, double speedModifier) {
        this.drake = drake;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = drake.getTarget();
        if (t == null || !t.isAlive()) return false;
        target = t;
        ticksUntilCharge = ROAR_DURATION_TICKS;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && drake.getTarget() == target;
    }

    @Override
    public void stop() {
        target = null;
        drake.getNavigation().stop();
    }

    @Override
    public void tick() {
        drake.getLookControl().setLookAt(target, 30.0f, 30.0f);

        if (ticksUntilCharge > 0) {
            // Roar phase — stand still and roar
            ticksUntilCharge--;
            if (ticksUntilCharge == ROAR_DURATION_TICKS - 1) {
                drake.triggerRoar();
            }
        } else {
            // Charge phase
            drake.getNavigation().moveTo(target, speedModifier);
            if (drake.distanceToSqr(target) < 9.0) {
                drake.doHurtTarget(target);
                ticksUntilCharge = ROAR_DURATION_TICKS * 2;
            }
        }
    }
}
