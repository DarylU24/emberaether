package com.emberaether.client.renderer;

import com.emberaether.client.model.FrostWyvernModel;
import com.emberaether.entity.FrostWyvernEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FrostWyvernRenderer extends GeoEntityRenderer<FrostWyvernEntity> {

    public FrostWyvernRenderer(EntityRendererProvider.Context context) {
        super(context, new FrostWyvernModel());
        this.shadowRadius = 1.0f;
    }

    @Override
    public void render(FrostWyvernEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.2f, 1.2f, 1.2f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
