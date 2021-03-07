package com.github.amusingimpala75.datadriver.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(BuiltinRegistries.class)
public interface BuiltinRegistriesAccessor {

    @Invoker("addRegistry")
    static <T, R extends MutableRegistry<T>> R call$addRegistry(RegistryKey<? extends Registry<T>> registryRef, R registry, Supplier<T> defaultValueSupplier, Lifecycle lifecycle) {
        throw new IllegalStateException("Mixin should have filled this in!");
    }
}
