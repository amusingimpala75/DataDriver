package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.Datadriver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class DataDriverReloadListener<T> extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private boolean loaded = false;
    private final Registry<T> reg;
    private final String dir;
    private final Registry<Codec<? extends T>> codReg;

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private DataDriverReloadListener(String dir, Registry<T> reg, Registry<Codec<? extends T>> codReg) {
        super(GSON, dir);
        this.reg = reg;
        this.dir = dir;
        this.codReg = codReg;
    }

    @Override
    public Identifier getFabricId() {
        return Datadriver.id("reload_listener/"+dir);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        if (this.loaded) {
            Datadriver.LOGGER.error("Data for DataDriver can only be done once per session! /nThe registry is no polluted with json ids and cannot have new ones registered without a relog! %nThis error can be ignored if this is from a modpack, but cannot be ignored if it was a datapack!");
            return;
        }
        this.loaded = true;

        int counter = 0;
        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            try {
                RegistryWrapper<T> wrapper = RegistryWrapper.getFromJson(entry.getValue(), codReg);
                wrapper.register(reg, entry.getKey());
                counter++;
            } catch (Throwable e) {
                Datadriver.LOGGER.error("Error loading data listener "+getFabricId());
                throw e;
            }
        }
        Datadriver.LOGGER.info(("Loaded {} "+dir+ (counter == 1 ? "" : "s")), counter);
    }

    public static <T> DataDriverReloadListener<T> createFromRegistry(Registry<T> reg, Registry<Codec<? extends T>> codReg) {
        return new DataDriverReloadListener<>(reg.getKey().getValue().getPath(), reg, codReg);
    }
}
