package com.emberaether.client;

import com.emberaether.client.renderer.FrostWyvernRenderer;
import com.emberaether.client.renderer.InfernalDrakeRenderer;
import com.emberaether.registry.EntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.emberaether.EmberAether.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.INFERNAL_DRAKE.get(), InfernalDrakeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FROST_WYVERN.get(), FrostWyvernRenderer::new);
    }
}
