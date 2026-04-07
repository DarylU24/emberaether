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
        String name = switch (animatable.getVariant()) {
            case InfernalDrakeEntity.VARIANT_RARE   -> "infernal_drake_rare";
            case InfernalDrakeEntity.VARIANT_ELITE  -> "infernal_drake_elite";
            case InfernalDrakeEntity.VARIANT_MOLTEN -> "infernal_drake_molten";
            case InfernalDrakeEntity.VARIANT_VOID   -> "infernal_drake_void";
            default                                  -> "infernal_drake_common";
        };
        return new ResourceLocation(EmberAether.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(InfernalDrakeEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "animations/infernal_drake.animation.json");
    }
}
