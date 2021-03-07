package com.github.amusingimpala75.datadriver.mixin;

import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DefaultedRegistry.class)
public interface DefaultedRegistryAccessor {
    @Accessor("defaultValue")
    Object accessor$getDefaultValue();
}
