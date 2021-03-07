package com.github.amusingimpala75.datadriver;

import com.github.amusingimpala75.datadriver.impl.DataDriverReloadListener;
import com.github.amusingimpala75.datadriver.impl.FluidDataReloadListener;
import com.github.amusingimpala75.datadriver.impl.Registries;
import com.github.amusingimpala75.datadriver.impl.RenderLayersReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//TODO Non types: fix Tags (init data things before tags are registered), Config option to hide "loaded datapacks twice since load" error (useful on servers and modpacks where datapacks should not change), damage if max-health boost when already above
//TODO: Entities, BlockEntities, DFU identifier upgrades, Recipes (?),
public class Datadriver implements ModInitializer {
    public static final String MODID = "datadriver";
    public static Logger LOGGER = LogManager.getLogger();
    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registries.ARMOR_MATERIALS, Registries.ARMOR_MATERIAL_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registries.TOOL_MATERIALS, Registries.TOOL_MATERIAL_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registries.ITEM_GROUPS, Registries.ITEM_GROUP_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registry.STATUS_EFFECT, Registries.STATUS_EFFECT_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registry.POTION, Registries.POTION_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FluidDataReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registry.BLOCK, Registries.BLOCK_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registry.ITEM, Registries.ITEM_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new RenderLayersReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registry.PAINTING_MOTIVE, Registries.PAINTING_CODECS));
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(DataDriverReloadListener.createFromRegistry(Registries.BLOCK_ENTITIES, Registries.BLOCK_ENTITY_CODECS));
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
