package com.emberaether;

import com.emberaether.entity.DragonEggEntity;
import com.emberaether.entity.FrostWyvernEntity;
import com.emberaether.entity.InfernalDrakeEntity;
import com.emberaether.registry.EntityRegistry;
import com.emberaether.registry.ItemRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(EmberAether.MOD_ID)
@Mod.EventBusSubscriber(modid = EmberAether.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EmberAether {
    public static final String MOD_ID = "emberaether";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public EmberAether() {
        GeckoLib.initialize();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EntityRegistry.ENTITIES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.INFERNAL_DRAKE.get(), InfernalDrakeEntity.createAttributes().build());
        event.put(EntityRegistry.FROST_WYVERN.get(),   FrostWyvernEntity.createAttributes().build());
        event.put(EntityRegistry.DRAGON_EGG.get(),     DragonEggEntity.createAttributes().build());
    }
}
