package com.github.amusingimpala75.datadriver.mixin.item;

import net.minecraft.item.HorseArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseArmorItem.class)
public interface HorseItemAccessor {
    @Accessor("entityTexture")
    String accessor$getName();
}
