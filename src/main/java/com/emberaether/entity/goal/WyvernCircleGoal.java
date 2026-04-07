package com.emberaether.entity.goal;

import com.emberaether.entity.FrostWyvernEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Wyvern circles a player at a safe distance before deciding to attack or flee.
 * The wyvern orbits 8–14 blocks away, rising to roughly eye level + 6 blocks.
 */
public class WyvernCircleGoal extends Goal {
    private static final double ORBIT_MIN = 8.0;
    private static final double ORBIT_MAX = 14.0;

    private final FrostWyvernEntity wyvern;
    private LivingEntity target;
    private double orbitAngle;
    private int strafeTick;

    public WyvernCircleGoal(FrostWyvernEntity wyvern) {
        this.wyvern = wyvern;
        this.orbitAngle = Math.random() * Math.PI * 2;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = wyvern.getTarget();
        if (t == null || !t.isAlive()) {
            t = wyvern.findNearestPlayer(24.0);
        }
        if (t == null) return false;
        target = t;
        return wyvern.distanceToSqr(target) <= ORBIT_MAX * ORBIT_MAX * 4;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && wyvern.distanceToSqr(target) <= ORBIT_MAX * ORBIT_MAX * 4;
    }

    @Override
    public void stop() {
        target = null;
        wyvern.getNavigation().stop();
    }

    @Override
    public void tick() {
        strafeTick++;
        orbitAngle += 0.035;

        double radius = ORBIT_MIN + (ORBIT_MAX - ORBIT_MIN) * 0.5;
        double orbitX = target.getX() + Math.cos(orbitAngle) * radius;
        double orbitY = target.getY() + 6.0;
        double orbitZ = target.getZ() + Math.sin(orbitAngle) * radius;

        wyvern.getNavigation().moveTo(orbitX, orbitY, orbitZ, 1.2);
        wyvern.getLookControl().setLookAt(target, 20.0f, 20.0f);
    }
}
