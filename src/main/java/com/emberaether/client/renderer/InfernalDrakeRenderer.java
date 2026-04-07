package com.emberaether.client.renderer;

import com.emberaether.client.model.InfernalDrakeModel;
import com.emberaether.entity.InfernalDrakeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class InfernalDrakeRenderer extends GeoEntityRenderer<InfernalDrakeEntity> {

    public InfernalDrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new InfernalDrakeModel());
        this.shadowRadius = 1.2f;
    }

    @Override
    public void render(InfernalDrakeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Scale the Drake slightly larger than default
        poseStack.pushPose();
        poseStack.scale(1.4f, 1.4f, 1.4f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
