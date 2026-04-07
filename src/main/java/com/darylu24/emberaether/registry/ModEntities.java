package com.darylu24.emberaether.registry;

import com.darylu24.emberaether.EmberAetherMod;
import com.darylu24.emberaether.entity.EarthscaleDragonEntity;
import com.darylu24.emberaether.entity.SkyfireDragonEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModEntities {
    private ModEntities() {
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, EmberAetherMod.MOD_ID);

    public static final RegistryObject<EntityType<SkyfireDragonEntity>> SKYFIRE_DRAGON = ENTITY_TYPES.register(
            "skyfire_dragon",
            () -> EntityType.Builder.of(SkyfireDragonEntity::new, EntityClassification.CREATURE)
                    .sized(1.8F, 1.6F)
                    .build("skyfire_dragon")
    );

    public static final RegistryObject<EntityType<EarthscaleDragonEntity>> EARTHSCALE_DRAGON = ENTITY_TYPES.register(
            "earthscale_dragon",
            () -> EntityType.Builder.of(EarthscaleDragonEntity::new, EntityClassification.CREATURE)
                    .sized(2.0F, 1.8F)
                    .build("earthscale_dragon")
    );
}
