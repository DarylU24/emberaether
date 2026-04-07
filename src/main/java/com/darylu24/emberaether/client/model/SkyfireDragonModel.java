package com.darylu24.emberaether.client.model;

import com.darylu24.emberaether.entity.AbstractDragonEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class SkyfireDragonModel<T extends AbstractDragonEntity> extends EntityModel<T> {
    private final ModelRenderer root;
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer jaw;
    private final ModelRenderer neck1;
    private final ModelRenderer neck2;
    private final ModelRenderer neck3;
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;
    private final ModelRenderer leftWingTip;
    private final ModelRenderer rightWingTip;
    private final ModelRenderer frontLeftLeg;
    private final ModelRenderer frontRightLeg;
    private final ModelRenderer backLeftLeg;
    private final ModelRenderer backRightLeg;
    private final ModelRenderer tail1;
    private final ModelRenderer tail2;
    private final ModelRenderer tail3;
    private final ModelRenderer tail4;

    public SkyfireDragonModel() {
        texWidth = 128;
        texHeight = 128;

        root = new ModelRenderer(this);
        root.setPos(0.0F, 24.0F, 0.0F);

        body = new ModelRenderer(this, 0, 0);
        body.setPos(0.0F, -12.0F, 0.0F);
        body.addBox(-5.0F, -6.0F, -10.0F, 10.0F, 6.0F, 20.0F);
        root.addChild(body);

        neck1 = new ModelRenderer(this, 0, 26);
        neck1.setPos(0.0F, -4.0F, -10.0F);
        neck1.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F);
        body.addChild(neck1);

        neck2 = new ModelRenderer(this, 12, 26);
        neck2.setPos(0.0F, 0.0F, -4.0F);
        neck2.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F);
        neck1.addChild(neck2);

        neck3 = new ModelRenderer(this, 24, 26);
        neck3.setPos(0.0F, 0.0F, -4.0F);
        neck3.addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F);
        neck2.addChild(neck3);

        head = new ModelRenderer(this, 0, 32);
        head.setPos(0.0F, 0.0F, -4.0F);
        head.addBox(-3.0F, -3.0F, -8.0F, 6.0F, 6.0F, 8.0F);
        neck3.addChild(head);

        ModelRenderer leftHorn = new ModelRenderer(this, 28, 32);
        leftHorn.setPos(2.0F, -3.0F, -2.0F);
        leftHorn.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F);
        leftHorn.zRot = (float) Math.toRadians(-30.0D);
        head.addChild(leftHorn);

        ModelRenderer rightHorn = new ModelRenderer(this, 28, 32);
        rightHorn.setPos(-2.0F, -3.0F, -2.0F);
        rightHorn.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F);
        rightHorn.zRot = (float) Math.toRadians(30.0D);
        head.addChild(rightHorn);

        jaw = new ModelRenderer(this, 0, 46);
        jaw.setPos(0.0F, 3.0F, -8.0F);
        jaw.addBox(-3.0F, 0.0F, 0.0F, 6.0F, 2.0F, 4.0F);
        head.addChild(jaw);

        leftWing = new ModelRenderer(this, 40, 0);
        leftWing.setPos(6.0F, -8.0F, 2.0F);
        leftWing.addBox(0.0F, 0.0F, -4.0F, 2.0F, 16.0F, 8.0F);
        body.addChild(leftWing);

        leftWingTip = new ModelRenderer(this, 60, 0);
        leftWingTip.setPos(2.0F, 12.0F, 0.0F);
        leftWingTip.addBox(0.0F, 0.0F, -3.0F, 2.0F, 12.0F, 6.0F);
        leftWing.addChild(leftWingTip);

        rightWing = new ModelRenderer(this, 40, 0);
        rightWing.mirror = true;
        rightWing.setPos(-6.0F, -8.0F, 2.0F);
        rightWing.addBox(-2.0F, 0.0F, -4.0F, 2.0F, 16.0F, 8.0F);
        body.addChild(rightWing);

        rightWingTip = new ModelRenderer(this, 60, 0);
        rightWingTip.mirror = true;
        rightWingTip.setPos(-2.0F, 12.0F, 0.0F);
        rightWingTip.addBox(-2.0F, 0.0F, -3.0F, 2.0F, 12.0F, 6.0F);
        rightWing.addChild(rightWingTip);

        frontLeftLeg = createLeg(3.0F, -2.0F, -7.0F);
        frontRightLeg = createLeg(-3.0F, -2.0F, -7.0F);
        backLeftLeg = createLeg(3.0F, -2.0F, 6.0F);
        backRightLeg = createLeg(-3.0F, -2.0F, 6.0F);

        tail1 = new ModelRenderer(this, 0, 52);
        tail1.setPos(0.0F, -3.0F, 10.0F);
        tail1.addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        body.addChild(tail1);

        tail2 = new ModelRenderer(this, 16, 52);
        tail2.setPos(0.0F, 0.0F, 6.0F);
        tail2.addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        tail1.addChild(tail2);

        tail3 = new ModelRenderer(this, 32, 52);
        tail3.setPos(0.0F, 0.0F, 6.0F);
        tail3.addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        tail2.addChild(tail3);

        tail4 = new ModelRenderer(this, 48, 52);
        tail4.setPos(0.0F, 0.0F, 6.0F);
        tail4.addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        tail3.addChild(tail4);
    }

    private ModelRenderer createLeg(float x, float y, float z) {
        ModelRenderer upper = new ModelRenderer(this, 80, 0);
        upper.setPos(x, y, z);
        upper.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        body.addChild(upper);

        ModelRenderer mid = new ModelRenderer(this, 88, 0);
        mid.setPos(0.0F, 4.0F, 0.0F);
        mid.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        upper.addChild(mid);

        ModelRenderer foot = new ModelRenderer(this, 96, 0);
        foot.setPos(0.0F, 4.0F, 0.0F);
        foot.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F);
        mid.addChild(foot);

        return upper;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float idle = MathHelper.sin(ageInTicks * 0.08F) * 0.08F;
        float walk = MathHelper.cos(limbSwing * 0.9F) * 0.8F * limbSwingAmount;
        float tailSwing = MathHelper.sin(ageInTicks * 0.2F) * 0.18F;
        float attack = entity.getAttackAnimationScale(0.0F);

        neck1.xRot = headPitch * ((float) Math.PI / 180F) * 0.25F;
        neck2.xRot = headPitch * ((float) Math.PI / 180F) * 0.25F;
        neck3.xRot = headPitch * ((float) Math.PI / 180F) * 0.25F;
        head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.35F + idle;

        boolean flying = entity.isDragonFlying();
        float wingBase = flying ? 0.9F : 0.25F;
        float flap = MathHelper.sin(ageInTicks * (flying ? 0.9F : 0.25F)) * wingBase;
        leftWing.zRot = flap;
        rightWing.zRot = -flap;
        leftWingTip.zRot = flap * 0.65F;
        rightWingTip.zRot = -flap * 0.65F;

        frontLeftLeg.xRot = walk - (attack * 0.6F);
        frontRightLeg.xRot = -walk - (attack * 0.6F);
        backLeftLeg.xRot = -walk;
        backRightLeg.xRot = walk;

        tail1.yRot = tailSwing;
        tail2.yRot = tailSwing * 1.2F;
        tail3.yRot = tailSwing * 1.5F;
        tail4.yRot = tailSwing * 1.8F;

        jaw.xRot = entity.getJawOpen() * 0.6F;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.matrix.MatrixStack matrixStack, com.mojang.blaze3d.vertex.IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
