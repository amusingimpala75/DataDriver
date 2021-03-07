package com.github.amusingimpala75.datadriver.duck;

import com.mojang.serialization.Codec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;

public interface BlockDuck {
    AbstractBlock.Settings getSettings();
    Codec<Block> getCodec();
}
