package com.darylu24.emberaether.client;

import com.darylu24.emberaether.EmberAetherMod;
import com.darylu24.emberaether.client.renderer.EarthscaleDragonRenderer;
import com.darylu24.emberaether.client.renderer.SkyfireDragonRenderer;
import com.darylu24.emberaether.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EmberAetherMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SKYFIRE_DRAGON.get(), SkyfireDragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.EARTHSCALE_DRAGON.get(), EarthscaleDragonRenderer::new);
    }
}
