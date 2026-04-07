package com.emberaether.entity.goal;

import com.emberaether.entity.FrostWyvernEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * When the Wyvern has been hit, it fires a single ice projectile at its attacker
 * (Slowness IV, 3 seconds), then retreats upward.  Cooldown: 8 seconds (160 ticks).
 */
public class WyvernIceBreathGoal extends Goal {
    private static final int COOLDOWN_TICKS = 160;
    private static final double RANGE = 16.0;

    private final FrostWyvernEntity wyvern;
    private LivingEntity target;
    private int cooldownRemaining;

    public WyvernIceBreathGoal(FrostWyvernEntity wyvern) {
        this.wyvern = wyvern;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    public void triggerAttack(LivingEntity attacker) {
        if (cooldownRemaining > 0) return;
        this.target = attacker;
    }

    @Override
    public boolean canUse() {
        if (cooldownRemaining > 0) { cooldownRemaining--; return false; }
        return target != null && target.isAlive() && wyvern.distanceToSqr(target) <= RANGE * RANGE;
    }

    @Override
    public boolean canContinueToUse() { return false; }

    @Override
    public void start() {
        wyvern.getLookControl().setLookAt(target, 30.0f, 30.0f);
        wyvern.shootIceProjectile(target);
        // Apply slowness directly in case the projectile misses at close range
        if (wyvern.distanceToSqr(target) < 4.0) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        }
        cooldownRemaining = COOLDOWN_TICKS;
        wyvern.startRetreat();
        target = null;
    }
}
