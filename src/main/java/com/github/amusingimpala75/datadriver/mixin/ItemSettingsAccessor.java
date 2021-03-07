package com.github.amusingimpala75.datadriver.mixin;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Settings.class)
public interface ItemSettingsAccessor {
    @Accessor("fireproof")
    boolean accessor$isFireproof();

    @Accessor("rarity")
    Rarity accessor$getRarity();

    @Accessor("group")
    ItemGroup accessor$getGroup();

    @Accessor("recipeRemainder")
    Item accessor$getRemainder();

    @Accessor("maxDamage")
    int accessor$getMaxDamage();

    @Accessor("maxCount")
    int accessor$maxCount();

    @Accessor("foodComponent")
    FoodComponent accessor$getFoodComponent();
}
