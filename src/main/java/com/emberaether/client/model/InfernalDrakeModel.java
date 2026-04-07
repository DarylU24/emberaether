package com.emberaether.client.model;

import com.emberaether.EmberAether;
import com.emberaether.entity.InfernalDrakeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class InfernalDrakeModel extends GeoModel<InfernalDrakeEntity> {

    @Override
    public ResourceLocation getModelResource(InfernalDrakeEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "geo/infernal_drake.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(InfernalDrakeEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "textures/entity/infernal_drake.png");
    }

    @Override
    public ResourceLocation getAnimationResource(InfernalDrakeEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "animations/infernal_drake.animation.json");
    }
}
