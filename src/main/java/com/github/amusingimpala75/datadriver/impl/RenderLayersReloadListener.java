package com.github.amusingimpala75.datadriver.impl;

import com.github.amusingimpala75.datadriver.Datadriver;
import com.github.amusingimpala75.datadriver.api.Util;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;

import java.util.*;

public class RenderLayersReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Map<String, RenderLayer> RENDER_LAYER_MAP = new HashMap<>();

    public RenderLayersReloadListener() {
        super((new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create(), "render_layer");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        if (!FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            return;
        }
        int counter = 0;
        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            JsonObject root = entry.getValue().getAsJsonObject();
            JsonObject values = root.getAsJsonObject("values");
            String type = root.get("type").getAsString();
            Map<Identifier, String> renderMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> val : values.entrySet()) {
                renderMap.put(new Identifier(val.getKey()), val.getValue().getAsString());
            }

            switch (type) {
                case "block":
                    for (Map.Entry<Identifier, String> e : renderMap.entrySet()) {
                        BlockRenderLayerMap.INSTANCE.putBlock(Util.getOrThrow(Registry.BLOCK, e.getKey()), RENDER_LAYER_MAP.get(e.getValue()));
                        counter++;
                    }
                    break;
                case "item":
                    for (Map.Entry<Identifier, String> e : renderMap.entrySet()) {
                        BlockRenderLayerMap.INSTANCE.putItem(Util.getOrThrow(Registry.ITEM, e.getKey()), RENDER_LAYER_MAP.get(e.getValue()));
                        counter++;
                    }
                    break;
                case "fluid":
                    for (Map.Entry<Identifier, String> e : renderMap.entrySet()) {
                        BlockRenderLayerMap.INSTANCE.putFluids(RENDER_LAYER_MAP.get(e.getValue()), Util.getOrThrow(Registry.FLUID, new Identifier(e.getKey().getNamespace(), "still_"+e.getKey().getPath())), Util.getOrThrow(Registry.FLUID, new Identifier(e.getKey().getNamespace(), "flowing_"+e.getKey().getPath())));
                        counter++;
                    }
                    break;
                default:
                    throw new IllegalStateException("Could not decipher render layer target type " + type);
            }
        }
        LogManager.getLogger().info("Loaded {} render layer"+(counter == 1 ? "" : "s"), counter);
    }

    //TODO: More?
    static {
        RENDER_LAYER_MAP.put("solid", RenderLayer.getSolid());
        RENDER_LAYER_MAP.put("armor_glint", RenderLayer.getArmorGlint());
        RENDER_LAYER_MAP.put("cutout", RenderLayer.getCutout());
        RENDER_LAYER_MAP.put("cutout_mipped", RenderLayer.getCutoutMipped());
        RENDER_LAYER_MAP.put("direct_glint", RenderLayer.getDirectGlint());
        RENDER_LAYER_MAP.put("lines", RenderLayer.getLines());
        RENDER_LAYER_MAP.put("translucent", RenderLayer.getTranslucent());
    }

    @Override
    public Identifier getFabricId() {
        return Datadriver.id("reload_listener/render_layers");
    }
}
