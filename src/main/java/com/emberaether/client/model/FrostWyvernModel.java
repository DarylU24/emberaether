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
        String name = switch (animatable.getVariant()) {
            case FrostWyvernEntity.VARIANT_RARE     -> "frost_wyvern_rare";
            case FrostWyvernEntity.VARIANT_ELITE    -> "frost_wyvern_elite";
            case FrostWyvernEntity.VARIANT_BLIZZARD -> "frost_wyvern_blizzard";
            case FrostWyvernEntity.VARIANT_STORM    -> "frost_wyvern_storm";
            default                                  -> "frost_wyvern_common";
        };
        return new ResourceLocation(EmberAether.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(FrostWyvernEntity animatable) {
        return new ResourceLocation(EmberAether.MOD_ID, "animations/frost_wyvern.animation.json");
    }
}
