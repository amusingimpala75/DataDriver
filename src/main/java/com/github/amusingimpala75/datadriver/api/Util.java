package com.github.amusingimpala75.datadriver.api;

import com.github.amusingimpala75.datadriver.mixin.DefaultedRegistryAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class Util {
    public static ItemGroup id2Group(Identifier id) {
        Optional<ItemGroup> group = Arrays.stream(ItemGroup.GROUPS).filter(g -> g.getName().contains(id.getNamespace()+"."+id.getPath())).findFirst();
        if (group.isPresent()) {
            return group.get();
        }
        throw new IllegalStateException("Could not find ItemGroup "+id.toString());
    }

    public static Identifier group2Id(ItemGroup itemGroup) {
        String name = itemGroup.getName();
        String namespace = name.substring(0, name.indexOf('.'));
        String path = name.substring(name.indexOf('.')+1);
        return new Identifier(namespace, path);
    }

    @NotNull
    public static <T> T getOrThrow(Registry<T> reg, Identifier id) {
        T t = reg.get(id);
        if (t != null) {
            if (reg instanceof DefaultedRegistry && ((DefaultedRegistryAccessor) reg).accessor$getDefaultValue().equals(t)) {
                throw new IllegalStateException("Could not find entry "+id.toString()+" in registry "+reg.getKey().getValue().toString());
            }
            return t;
        }
        throw new IllegalStateException("Could not find entry "+id.toString()+" in registry "+reg.getKey().getValue().toString());
    }

    public static <T> Identifier getIdOrThrow(Registry<T> reg, T entry) {
        Identifier id = reg.getId(entry);
        if (id != null) {
            if (reg instanceof DefaultedRegistry && ((DefaultedRegistry<T>) reg).getDefaultId().equals(id)) {
                throw new IllegalStateException("Could not find entry "+id.toString()+" in registry "+reg.getKey().getValue().toString());
            }
            return id;
        }
        throw new IllegalStateException("Could not find entry "+entry.getClass()+" in registry "+reg.getKey().getValue().toString());
    }

    public static <T> Optional<T> getOrEmpty(T val) {
        if (val == null) {
            return Optional.empty();
        }
        return Optional.of(val);
    }

    /*public static <T> void removeFromRegistry(Identifier id, SimpleRegistry<T> reg) {
        T entry = reg.get(id);
        //raw id -> entry
        ((SimpleRegistryAccessor)reg).accessor$getRawIdToEntry().remove(entry);
        //entry -> raw id
        int rawId = reg.getRawId(entry);
        ((SimpleRegistryAccessor)reg).accessor$getEntryToRawId().remove(entry, rawId);
        //id -> entry
        ((SimpleRegistryAccessor)reg).accessor$getIdToEntry().remove(entry);
        //key -> entry
        ((SimpleRegistryAccessor)reg).accessor$getKeyToEntry().remove(entry);
        //entry -> lifecycle
        ((SimpleRegistryAccessor)reg).accessor$getEntryToLifeCycle().remove(entry);
    }*/

    public static <T extends Enum<T>> Codec<T> createEnumCodec(Class<T> clazz) {
        return Codec.STRING.comapFlatMap(str -> {
            try {
                return DataResult.success(T.valueOf(clazz, str.toUpperCase(Locale.ROOT)));
            } catch (InvalidIdentifierException var2) {
                return DataResult.error("Not a valid resource location: " + str + " " + var2.getMessage());
            }
        }, t -> t.name().toLowerCase(Locale.ROOT)).stable();
    }

    public static void dumpRegistry(Registry<?> reg) {
        for (Identifier id : reg.getIds()) {
            System.out.println(id);
        }
    }
}
