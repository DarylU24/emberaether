package com.darylu24.emberaether.client.renderer;

import com.darylu24.emberaether.EmberAetherMod;
import com.darylu24.emberaether.client.model.SkyfireDragonModel;
import com.darylu24.emberaether.entity.SkyfireDragonEntity;
import com.darylu24.emberaether.entity.SkyfireVariant;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class SkyfireDragonRenderer extends MobRenderer<SkyfireDragonEntity, SkyfireDragonModel<SkyfireDragonEntity>> {
    public SkyfireDragonRenderer(EntityRendererManager manager) {
        super(manager, new SkyfireDragonModel<>(), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SkyfireDragonEntity entity) {
        SkyfireVariant variant = (SkyfireVariant) entity.getVariant();
        return new ResourceLocation(EmberAetherMod.MOD_ID, "textures/entity/skyfire/" + variant.textureName() + ".png");
    }
}
