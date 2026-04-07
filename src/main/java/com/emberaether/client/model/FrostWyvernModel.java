package com.emberaether.client.model;

import com.emberaether.EmberAether;
import com.emberaether.entity.FrostWyvernEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FrostWyvernModel extends GeoModel<FrostWyvernEntity> {

    @Override
    public ResourceLocation getModelResource(FrostWyvernEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "geo/frost_wyvern.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FrostWyvernEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "textures/entity/frost_wyvern.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FrostWyvernEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "animations/frost_wyvern.animation.json");
    }
}
