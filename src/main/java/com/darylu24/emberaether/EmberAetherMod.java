package com.darylu24.emberaether;

import com.darylu24.emberaether.entity.EarthscaleDragonEntity;
import com.darylu24.emberaether.entity.SkyfireDragonEntity;
import com.darylu24.emberaether.registry.ModEntities;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EmberAetherMod.MOD_ID)
public class EmberAetherMod {
    public static final String MOD_ID = "emberaether";

    public EmberAetherMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GlobalEntityTypeAttributes.put(ModEntities.SKYFIRE_DRAGON.get(), SkyfireDragonEntity.createAttributes().build());
            GlobalEntityTypeAttributes.put(ModEntities.EARTHSCALE_DRAGON.get(), EarthscaleDragonEntity.createAttributes().build());
        });
    }
}
