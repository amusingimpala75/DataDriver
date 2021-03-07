package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.Datadriver;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.lang.reflect.Field;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class Registries {

    //Keys
    public static final RegistryKey<Registry<ArmorMaterial>> ARMOR_MATERIALS_KEY = RegistryKey.ofRegistry(Datadriver.id("armor_material"));
    public static final RegistryKey<Registry<ToolMaterial>> TOOL_MATERIALS_KEY = RegistryKey.ofRegistry(Datadriver.id("tool_material"));
    public static final RegistryKey<Registry<ItemGroup>> ITEM_GROUPS_KEY = RegistryKey.ofRegistry(Datadriver.id("item_group"));
    public static final RegistryKey<Registry<Item>> ITEM_KEY = Registry.ITEM_KEY;
    public static final RegistryKey<Registry<Block>> BLOCK_KEY = Registry.BLOCK_KEY;
    public static final RegistryKey<Registry<Fluid>> FLUID_KEY = Registry.FLUID_KEY;
    public static final RegistryKey<Registry<BlockEntity>> BLOCK_ENTITY_KEY = RegistryKey.ofRegistry(Datadriver.id("block_entity"));

    //public static final RegistryKey<Registry<Registry<? extends Codec<?>>>> CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("codec"));
    public static final RegistryKey<Registry<Codec<? extends Block>>> BLOCK_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("block_codec"));
    public static final RegistryKey<Registry<Codec<? extends Item>>> ITEM_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("item_codec"));
    public static final RegistryKey<Registry<Codec<? extends ArmorMaterial>>> ARMOR_MATERIAL_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("armor_material_codec"));
    public static final RegistryKey<Registry<Codec<? extends ToolMaterial>>> TOOL_MATERIAL_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("tool_material_codec"));
    public static final RegistryKey<Registry<Codec<? extends ItemGroup>>> ITEM_GROUP_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("item_group_codec"));
    public static final RegistryKey<Registry<Codec<? extends Pair<Fluid, Fluid>>>> FLUID_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("fluid_codec"));
    public static final RegistryKey<Registry<Codec<? extends Potion>>> POTION_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("potion_codec"));
    public static final RegistryKey<Registry<Codec<? extends PaintingMotive>>> PAINTING_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("painting_codec"));
    public static final RegistryKey<Registry<Codec<? extends BlockEntity>>> BLOCK_ENTITY_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("block_entity_codec"));
    public static final RegistryKey<Registry<Codec<? extends StatusEffect>>> STATUS_EFFECT_CODEC_KEY = RegistryKey.ofRegistry(Datadriver.id("status_effect_codec"));

    //public static Registry<Registry<? extends Codec<?>>> CODEC_REGISTRIES = new SimpleRegistry<>(CODEC_KEY, Lifecycle.experimental());

    public static Registry<Codec<? extends Item>> ITEM_CODECS = new SimpleRegistry<>(ITEM_CODEC_KEY, Lifecycle.experimental());
    public static Registry<Codec<? extends Block>> BLOCK_CODECS = new SimpleRegistry<>(BLOCK_CODEC_KEY, Lifecycle.experimental());
    public static Registry<Codec<? extends ArmorMaterial>> ARMOR_MATERIAL_CODECS = createWithOnlyOneEntry(ARMOR_MATERIAL_CODEC_KEY, CodecsImpl.ARMOR_MATERIAL);
    public static Registry<Codec<? extends ToolMaterial>> TOOL_MATERIAL_CODECS = createWithOnlyOneEntry(TOOL_MATERIAL_CODEC_KEY, CodecsImpl.TOOL_MATERIAL);
    public static Registry<Codec<? extends ItemGroup>> ITEM_GROUP_CODECS = createWithOnlyOneEntry(ITEM_GROUP_CODEC_KEY, CodecsImpl.ITEM_GROUP);
    public static Registry<Codec<? extends Pair<Fluid, Fluid>>> FLUID_CODECS = createWithOnlyOneEntry(FLUID_CODEC_KEY, CodecsImpl.Fluids.FLUIDS);
    public static Registry<Codec<? extends Potion>> POTION_CODECS = createWithOnlyOneEntry(POTION_CODEC_KEY, CodecsImpl.POTION);
    public static Registry<Codec<? extends PaintingMotive>> PAINTING_CODECS = createWithOnlyOneEntry(PAINTING_CODEC_KEY, CodecsImpl.PAINTING);
    public static Registry<Codec<? extends BlockEntity>> BLOCK_ENTITY_CODECS = createWithOnlyOneEntry(BLOCK_ENTITY_CODEC_KEY, CodecsImpl.BlockEntity.INVENTORY_BLOCK_ENTITY);
    public static Registry<Codec<? extends StatusEffect>> STATUS_EFFECT_CODECS = createWithOnlyOneEntry(STATUS_EFFECT_CODEC_KEY, CodecsImpl.EFFECT);

    public static Registry<ArmorMaterial> ARMOR_MATERIALS = new SimpleRegistry<>(ARMOR_MATERIALS_KEY, Lifecycle.experimental());
    public static Registry<ToolMaterial> TOOL_MATERIALS = new SimpleRegistry<>(TOOL_MATERIALS_KEY, Lifecycle.experimental());
    public static Registry<ItemGroup> ITEM_GROUPS = new SimpleRegistry<>(ITEM_GROUPS_KEY, Lifecycle.experimental());
    public static Registry<BlockEntity> BLOCK_ENTITIES = new SimpleRegistry<>(BLOCK_ENTITY_KEY, Lifecycle.experimental());

    static {
        //Populate master Codec registry
        //Registry.register(CODEC_REGISTRIES, ITEM_CODEC_KEY.getValue(), ITEM_CODECS);
        //Registry.register(CODEC_REGISTRIES, BLOCK_CODEC_KEY.getValue(), BLOCK_CODECS);
        //Registry.register(CODEC_REGISTRIES, ARMOR_MATERIAL_CODEC_KEY.getValue(), ARMOR_MATERIAL_CODECS);
        //Registry.register(CODEC_REGISTRIES, TOOL_MATERIAL_CODEC_KEY.getValue(), TOOL_MATERIAL_CODECS);
        //Registry.register(CODEC_REGISTRIES, ITEM_GROUP_CODEC_KEY.getValue(), ITEM_GROUP_CODECS);
        //Registry.register(CODEC_REGISTRIES, FLUID_CODEC_KEY.getValue(), FLUID_CODECS);

        //Populate Block Codec Registry
        for (Field f : CodecsImpl.Blocks.class.getFields()) {
            try {
                Codec<Block> codec = ((Codec<Block>)f.get(null));
                Registry.register(BLOCK_CODECS, f.getName().toLowerCase(Locale.ROOT), codec);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //Populate Item Codec Registry
        for (Field f : CodecsImpl.Items.class.getFields()) {
            try {
                Codec<Item> codec = ((Codec<Item>)f.get(null));
                Registry.register(ITEM_CODECS, f.getName().toLowerCase(Locale.ROOT), codec);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //Populate ItemGroup Registry
        for (ItemGroup g : ItemGroup.GROUPS) {
            Registry.register(ITEM_GROUPS, g.getName(), g);
        }

        //Populate ArmorMaterial Registry
        for (ArmorMaterials mat : ArmorMaterials.values()) {
            Registry.register(ARMOR_MATERIALS, mat.name().toLowerCase(Locale.ROOT), mat);
        }

        //Populate ToolMaterial Registry
        for (ToolMaterials mat : ToolMaterials.values()) {
            Registry.register(TOOL_MATERIALS, mat.name().toLowerCase(Locale.ROOT), mat);
        }


    }

    private static <T> Registry<Codec<? extends T>> createWithOnlyOneEntry(RegistryKey<Registry<Codec<? extends T>>> key, Codec<? extends T> onlyVal) {
        Registry<Codec<? extends T>> reg = new SimpleRegistry<>(key, Lifecycle.experimental());
        Registry.register(reg, Datadriver.id("default"), onlyVal);
        return reg;
    }

}
