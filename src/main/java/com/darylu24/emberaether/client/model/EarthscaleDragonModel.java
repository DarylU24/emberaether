package com.darylu24.emberaether.client.model;

import com.darylu24.emberaether.entity.EarthscaleDragonEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class EarthscaleDragonModel extends EntityModel<EarthscaleDragonEntity> {
    private final ModelRenderer root;
    private final ModelRenderer body;
    private final ModelRenderer neck1;
    private final ModelRenderer neck2;
    private final ModelRenderer head;
    private final ModelRenderer jaw;
    private final ModelRenderer frontLeftLeg;
    private final ModelRenderer frontRightLeg;
    private final ModelRenderer backLeftLeg;
    private final ModelRenderer backRightLeg;
    private final ModelRenderer tail1;
    private final ModelRenderer tail2;
    private final ModelRenderer tail3;

    public EarthscaleDragonModel() {
        texWidth = 128;
        texHeight = 128;

        root = new ModelRenderer(this);
        root.setPos(0.0F, 24.0F, 0.0F);

        body = new ModelRenderer(this, 0, 0);
        body.setPos(0.0F, -10.0F, 0.0F);
        body.addBox(-6.0F, -8.0F, -9.0F, 12.0F, 8.0F, 18.0F);
        root.addChild(body);

        for (int i = 0; i < 6; i++) {
            ModelRenderer spike = new ModelRenderer(this, 60, 0);
            spike.setPos(0.0F, -8.0F, -7.0F + i * 3.0F);
            spike.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F);
            body.addChild(spike);
        }

        neck1 = new ModelRenderer(this, 0, 26);
        neck1.setPos(0.0F, -5.0F, -9.0F);
        neck1.addBox(-1.5F, -1.5F, -4.0F, 3.0F, 3.0F, 4.0F);
        body.addChild(neck1);

        neck2 = new ModelRenderer(this, 14, 26);
        neck2.setPos(0.0F, 0.0F, -4.0F);
        neck2.addBox(-1.5F, -1.5F, -4.0F, 3.0F, 3.0F, 4.0F);
        neck1.addChild(neck2);

        head = new ModelRenderer(this, 0, 34);
        head.setPos(0.0F, 0.0F, -4.0F);
        head.addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F);
        neck2.addChild(head);

        jaw = new ModelRenderer(this, 28, 34);
        jaw.setPos(0.0F, 4.0F, -6.0F);
        jaw.addBox(-4.0F, 0.0F, 0.0F, 8.0F, 2.0F, 6.0F);
        head.addChild(jaw);

        frontLeftLeg = createLeg(4.0F, -3.0F, -6.0F);
        frontRightLeg = createLeg(-4.0F, -3.0F, -6.0F);
        backLeftLeg = createLeg(4.0F, -3.0F, 6.0F);
        backRightLeg = createLeg(-4.0F, -3.0F, 6.0F);

        tail1 = new ModelRenderer(this, 0, 50);
        tail1.setPos(0.0F, -5.0F, 9.0F);
        tail1.addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 8.0F);
        body.addChild(tail1);

        tail2 = new ModelRenderer(this, 22, 50);
        tail2.setPos(0.0F, 0.0F, 8.0F);
        tail2.addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 8.0F);
        tail1.addChild(tail2);

        tail3 = new ModelRenderer(this, 44, 50);
        tail3.setPos(0.0F, 0.0F, 8.0F);
        tail3.addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 8.0F);
        tail2.addChild(tail3);
    }

    private ModelRenderer createLeg(float x, float y, float z) {
        ModelRenderer upper = new ModelRenderer(this, 80, 0);
        upper.setPos(x, y, z);
        upper.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F);
        body.addChild(upper);

        ModelRenderer mid = new ModelRenderer(this, 92, 0);
        mid.setPos(0.0F, 5.0F, 0.0F);
        mid.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F);
        upper.addChild(mid);

        ModelRenderer foot = new ModelRenderer(this, 104, 0);
        foot.setPos(0.0F, 5.0F, 0.0F);
        foot.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F);
        mid.addChild(foot);

        return upper;
    }

    @Override
    public void setupAnim(EarthscaleDragonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float breathing = MathHelper.sin(ageInTicks * 0.08F) * 0.05F;
        float walk = MathHelper.cos(limbSwing * 0.75F) * 0.7F * limbSwingAmount;
        float stomp = MathHelper.sin(ageInTicks * 0.4F) * 0.15F * limbSwingAmount;
        float tailSwing = MathHelper.sin(ageInTicks * 0.12F) * 0.2F;

        body.y = entity.isBurrowing() ? -8.5F : -10.0F;
        body.xRot = breathing;

        neck1.xRot = headPitch * ((float) Math.PI / 180F) * 0.35F;
        neck2.xRot = headPitch * ((float) Math.PI / 180F) * 0.4F;
        head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.75F;

        frontLeftLeg.xRot = walk + stomp;
        frontRightLeg.xRot = -walk + stomp;
        backLeftLeg.xRot = -walk - stomp;
        backRightLeg.xRot = walk - stomp;

        tail1.yRot = tailSwing;
        tail2.yRot = tailSwing * 1.25F;
        tail3.yRot = tailSwing * 1.55F;

        jaw.xRot = entity.getJawOpen() * 0.5F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack matrixStack, com.mojang.blaze3d.vertex.IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
