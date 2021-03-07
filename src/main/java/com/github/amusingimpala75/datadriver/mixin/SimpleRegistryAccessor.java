package com.github.amusingimpala75.datadriver.mixin;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor {
    @Accessor("rawIdToEntry")
    <T> ObjectList<T> accessor$getRawIdToEntry();

    @Accessor("entryToRawId")
    <T> Object2IntMap<T> accessor$getEntryToRawId();

    @Accessor("idToEntry")
    <T> BiMap<Identifier, T> accessor$getIdToEntry();

    @Accessor("keyToEntry")
    <T> BiMap<RegistryKey<T>, T> accessor$getKeyToEntry();

    @Accessor("entryToLifecycle")
    <T> Map<T, Lifecycle> accessor$getEntryToLifeCycle();
}
