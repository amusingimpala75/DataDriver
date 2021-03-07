package com.github.amusingimpala75.datadriver.mixin.item;

import net.minecraft.item.DyeItem;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DyeItem.class)
public interface DyeItemAccessor {
    @Accessor("color")
    DyeColor accessor$getColor();
}
