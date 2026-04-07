package com.darylu24.emberaether.entity;

public enum SkyfireVariant implements DragonVariant {
    EMBER("ember", 1.0F),
    AZURE("azure", 1.1F),
    OBSIDIAN("obsidian", 0.95F);

    private final String textureName;
    private final float speedMultiplier;

    SkyfireVariant(String textureName, float speedMultiplier) {
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
