package com.github.amusingimpala75.datadriver.mixin;

import com.github.amusingimpala75.datadriver.duck.BlockDuck;
import com.mojang.serialization.Codec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock implements BlockDuck {

    @Unique
    private Codec<Block> codec;

    public BlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public AbstractBlock.Settings getSettings() {
        return settings;
    }

    public Codec<Block> getCodec() {
        return codec;
    }
}
