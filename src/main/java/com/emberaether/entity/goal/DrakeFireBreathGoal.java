package com.emberaether.entity.goal;

import com.emberaether.entity.InfernalDrakeEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Short-range fire-cone AoE attack.  The Drake must be within RANGE blocks of
 * its target.  After a cooldown of 8 seconds (160 ticks) it exhales a cone of
 * fire that ignites all entities within the cone for 5 seconds.
 */
public class DrakeFireBreathGoal extends Goal {
    private static final int COOLDOWN_TICKS = 160;
    private static final double RANGE = 6.0;

    private final InfernalDrakeEntity drake;
    private LivingEntity target;
    private int cooldownRemaining;

    public DrakeFireBreathGoal(InfernalDrakeEntity drake) {
        this.drake = drake;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = drake.getTarget();
        if (t == null || !t.isAlive()) return false;
        if (cooldownRemaining > 0) { cooldownRemaining--; return false; }
        target = t;
        return drake.distanceToSqr(target) <= RANGE * RANGE;
    }

    @Override
    public boolean canContinueToUse() { return false; }

    @Override
    public void start() {
        drake.getLookControl().setLookAt(target, 30.0f, 30.0f);
        drake.performFireBreath(target);
        cooldownRemaining = COOLDOWN_TICKS;
    }
}
