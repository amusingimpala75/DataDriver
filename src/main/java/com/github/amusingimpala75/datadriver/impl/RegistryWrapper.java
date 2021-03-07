package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.api.Util;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class RegistryWrapper<T> {

    private final T object;

    public RegistryWrapper(T object) {
        this.object = object;
    }

    public void register(Registry<T> reg, Identifier id) {
        Registry.register(reg, id, object);
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public void registerFluid(Identifier id) {
        try {
            ((Pair<Fluid, Fluid>) object).getFirst();
        } catch (ClassCastException e) {
            throw new IllegalStateException("I thought registry object "+id+" of class "+object.getClass()+" was an a Pair<Fluid, Fluid>, but it wasn't!", e);
        }
        Registry.register(Registry.FLUID, new Identifier(id.getNamespace(), "flowing_"+id.getPath()), ((Pair<Fluid, Fluid>) object).getFirst());
        Registry.register(Registry.FLUID, new Identifier(id.getNamespace(), "still_"+id.getPath()), ((Pair<Fluid, Fluid>) object).getSecond());
    }

    public static <T> RegistryWrapper<T> getFromJson(JsonElement json, Registry<Codec<? extends T>> codecRegistry) {
        //Codec<RegistryWrapper<T>> codec = RecordCodecBuilder.create(inst -> inst.group(
        //        Identifier.CODEC.fieldOf("codec").forGetter(r -> Datadriver.id("why_are_we_going_this_way")),
        //        Codec.STRING.fieldOf("type").forGetter(r -> "badbadbadbadbad"),
        //        Identifier.CODEC.fieldOf("registry_name").forGetter(r -> r.name)
        //).apply(inst, (codecId, type, name) -> new RegistryWrapper<>((Util.getOrThrow(Util.getOrThrow(Registries.CODEC_REGISTRIES, new Identifier(type)), codecId).decode(JsonOps.INSTANCE, json).getOrThrow(false, System.err::println).getFirst()), name)));
        Identifier codecId = new Identifier(JsonHelper.getString(JsonHelper.asObject(json, "top_element"), "codec"));
        Codec<? extends T> codec = Util.getOrThrow(codecRegistry, codecId);
        return new RegistryWrapper<>(codec.decode(JsonOps.INSTANCE, json).getOrThrow(false, System.err::println).getFirst());
    }
}
