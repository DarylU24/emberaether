package com.darylu24.emberaether.entity;

public enum EarthscaleVariant implements DragonVariant {
    MOSS("moss", 0.9F),
    STONE("stone", 0.85F),
    SAND("sand", 0.95F);

    private final String textureName;
    private final float speedMultiplier;

    EarthscaleVariant(String textureName, float speedMultiplier) {
        this.textureName = textureName;
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public String textureName() {
        return textureName;
    }

    @Override
    public float speedMultiplier() {
        return speedMultiplier;
    }
}
