package com.github.amusingimpala75.datadriver.mixin.block;

import net.minecraft.block.PressurePlateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PressurePlateBlock.class)
public interface PressurePlateBlockAccessor {
    @Accessor("type")
    PressurePlateBlock.ActivationRule accessor$getRule();
}
