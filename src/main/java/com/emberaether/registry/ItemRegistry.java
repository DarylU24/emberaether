package com.emberaether.registry;

import com.emberaether.EmberAether;
import com.emberaether.item.DragonSaddleItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EmberAether.MOD_ID);

    public static final RegistryObject<Item> DRAGON_SADDLE =
            ITEMS.register("dragon_saddle", () -> new DragonSaddleItem(new Item.Properties().stacksTo(1)));
}
