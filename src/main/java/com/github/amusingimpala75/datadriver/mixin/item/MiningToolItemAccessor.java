package com.github.amusingimpala75.datadriver.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(MiningToolItem.class)
public interface MiningToolItemAccessor {
    @Accessor("effectiveBlocks")
    Set<Block> accessor$getEffectiveBlocks();
}
