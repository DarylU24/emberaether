package com.darylu24.emberaether.client.renderer;

import com.darylu24.emberaether.EmberAetherMod;
import com.darylu24.emberaether.client.model.EarthscaleDragonModel;
import com.darylu24.emberaether.entity.EarthscaleDragonEntity;
import com.darylu24.emberaether.entity.EarthscaleVariant;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class EarthscaleDragonRenderer extends MobRenderer<EarthscaleDragonEntity, EarthscaleDragonModel> {
    public EarthscaleDragonRenderer(EntityRendererManager manager) {
        super(manager, new EarthscaleDragonModel(), 1.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(EarthscaleDragonEntity entity) {
        EarthscaleVariant variant = (EarthscaleVariant) entity.getVariant();
        return new ResourceLocation(EmberAetherMod.MOD_ID, "textures/entity/earthscale/" + variant.textureName() + ".png");
    }
}
