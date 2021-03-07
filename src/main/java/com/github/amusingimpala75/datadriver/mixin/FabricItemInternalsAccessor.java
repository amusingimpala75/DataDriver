package com.github.amusingimpala75.datadriver.mixin;

import net.fabricmc.fabric.impl.item.FabricItemInternals;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.WeakHashMap;

@Mixin(FabricItemInternals.class)
public interface FabricItemInternalsAccessor {
    @Accessor(value = "extraData", remap = false)
    static WeakHashMap<Item.Settings, FabricItemInternals.ExtraData> accessor$getExtraData() {
        throw new IllegalStateException("This should have been autofilled by Mixin!");
    }
}
