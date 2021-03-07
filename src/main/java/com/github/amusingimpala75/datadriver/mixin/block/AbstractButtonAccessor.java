package com.github.amusingimpala75.datadriver.mixin.block;

import net.minecraft.block.AbstractButtonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractButtonBlock.class)
public interface AbstractButtonAccessor {
    @Accessor("wooden")
    void accessor$setIsWooden(boolean isWooden);
}
