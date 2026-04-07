package com.emberaether.registry;

import com.emberaether.EmberAether;
import com.emberaether.entity.DragonEggEntity;
import com.emberaether.entity.FrostWyvernEntity;
import com.emberaether.entity.InfernalDrakeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EmberAether.MOD_ID);

    public static final RegistryObject<EntityType<InfernalDrakeEntity>> INFERNAL_DRAKE =
            ENTITIES.register("infernal_drake", () -> EntityType.Builder
                    .<InfernalDrakeEntity>of(InfernalDrakeEntity::new, MobCategory.MONSTER)
                    .sized(2.0f, 2.5f)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build(new ResourceLocation(EmberAether.MOD_ID, "infernal_drake").toString()));

    public static final RegistryObject<EntityType<FrostWyvernEntity>> FROST_WYVERN =
            ENTITIES.register("frost_wyvern", () -> EntityType.Builder
                    .<FrostWyvernEntity>of(FrostWyvernEntity::new, MobCategory.CREATURE)
                    .sized(1.8f, 2.2f)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build(new ResourceLocation(EmberAether.MOD_ID, "frost_wyvern").toString()));

    public static final RegistryObject<EntityType<DragonEggEntity>> DRAGON_EGG =
            ENTITIES.register("dragon_egg_entity", () -> EntityType.Builder
                    .<DragonEggEntity>of(DragonEggEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .clientTrackingRange(8)
                    .build(new ResourceLocation(EmberAether.MOD_ID, "dragon_egg_entity").toString()));
}
