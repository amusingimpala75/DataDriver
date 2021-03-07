package com.github.amusingimpala75.datadriver.api;

import com.github.amusingimpala75.datadriver.impl.BlockSettings1;
import com.github.amusingimpala75.datadriver.impl.BlockSettings2;
import com.github.amusingimpala75.datadriver.impl.CodecsImpl;
import com.github.amusingimpala75.datadriver.impl.Registries;
import com.github.amusingimpala75.datadriver.mixin.FabricItemExtraDataAccessor;
import com.github.amusingimpala75.datadriver.mixin.FabricItemInternalsAccessor;
import com.github.amusingimpala75.datadriver.mixin.ItemSettingsAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class Codecs {

    public static final Codec<Item.Settings> ITEM_SETTINGS = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("fireproof").orElse(false).forGetter(s -> ((ItemSettingsAccessor)s).accessor$isFireproof()),
            CodecsImpl.RARITY.optionalFieldOf("rarity").forGetter(s -> Util.getOrEmpty(((ItemSettingsAccessor)s).accessor$getRarity())),
            Identifier.CODEC.optionalFieldOf("group").forGetter(s -> Util.getOrEmpty(Util.group2Id(((ItemSettingsAccessor)s).accessor$getGroup()))),
            Identifier.CODEC.optionalFieldOf("remainder").forGetter(s -> Util.getOrEmpty(Util.getIdOrThrow(Registry.ITEM, ((ItemSettingsAccessor)s).accessor$getRemainder()))),
            Codec.INT.optionalFieldOf("max_uses").forGetter(s -> Util.getOrEmpty(((ItemSettingsAccessor)s).accessor$getMaxDamage())),
            Codec.INT.optionalFieldOf("max_uses_if_absent").forGetter(s -> Util.getOrEmpty(((ItemSettingsAccessor)s).accessor$getMaxDamage())),
            Codec.INT.optionalFieldOf("max_count").forGetter(s -> Util.getOrEmpty(((ItemSettingsAccessor)s).accessor$maxCount())),
            CodecsImpl.FOOD_COMPONENT.optionalFieldOf("food").forGetter(s -> Util.getOrEmpty(((ItemSettingsAccessor)s).accessor$getFoodComponent())),
            CodecsImpl.EQUIPMENT_SLOT.optionalFieldOf("slot").forGetter(s -> Util.getOrEmpty(((FabricItemExtraDataAccessor)(Object)(FabricItemInternalsAccessor.accessor$getExtraData().get(s))).accessor$getEquipmentSlot().getPreferredEquipmentSlot(new ItemStack(net.minecraft.item.Items.AIR))))
    ).apply(inst, (fireproof, rarity, group, remainder, maxDamage, maxDamageAbsent, maxCount, food, slot) -> {
        FabricItemSettings settings = new FabricItemSettings();
        if (fireproof) {
            settings.fireproof();
        }
        rarity.ifPresent(settings::rarity);
        group.ifPresent(id -> settings.group(Util.getOrThrow(Registries.ITEM_GROUPS, id)));
        remainder.ifPresent(r -> settings.recipeRemainder(Util.getOrThrow(Registry.ITEM, r)));
        maxDamage.ifPresent(settings::maxDamage);
        maxDamageAbsent.ifPresent(settings::maxDamageIfAbsent);
        maxCount.ifPresent(settings::maxCount);
        food.ifPresent(settings::food);
        slot.ifPresent(e -> settings.equipmentSlot(stack -> e));
        return settings;
    }));

    //TODO: Fill in Optional.emtpy() s and defaults
    public static final Codec<AbstractBlock.Settings> BLOCK_SETTINGS = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(a -> Optional.empty()),
            BlockSettings1.CODEC.optionalFieldOf("settings_part_1").forGetter(a -> Optional.empty()),
            CodecsImpl.DYE_COLOR.optionalFieldOf("dye_color").forGetter(a -> Optional.empty()),
            CodecsImpl.MATERIAL_COLOR.optionalFieldOf("material_color").forGetter(a -> Optional.empty()),
            CodecsImpl.MATERIAL.optionalFieldOf("material").forGetter(a -> Optional.empty()),
            BlockSettings2.CODEC.optionalFieldOf("settings_part_2").forGetter(a -> Optional.empty())
    ).apply(inst, (parent, settings1, dye, matColor, mat, settings2) -> {
        if (parent.isPresent()) {
            return FabricBlockSettings.copyOf(Util.getOrThrow(Registry.BLOCK, parent.get()));
        }
        FabricBlockSettings settings;
        //Thanks IntelliJ ;)
        if (!mat.isPresent()) {
            throw new IllegalStateException("If not copying a block, then a material entry is required!");
        }
        settings = matColor.map(materialColor -> FabricBlockSettings.of(mat.get(), materialColor)).orElseGet(() -> dye.map(dyeColor -> FabricBlockSettings.of(mat.get(), dyeColor)).orElseGet(() -> FabricBlockSettings.of(mat.get())));

        if (settings1.isPresent()) {
            BlockSettings1 s1 = settings1.get();
            if (s1.breaksInstantly) {
                settings.breakInstantly();
            }
            if (s1.dropsNothing) {
                settings.dropsNothing();
            }
            if (s1.isAir) {
                settings.air();
            }
            if (s1.noCollision) {
                settings.noCollision();
            }
            if (s1.nonOpaque) {
                settings.nonOpaque();
            }
            if (s1.ticksRandomly) {
                settings.ticksRandomly();
            }
            if (s1.dynamicBounds) {
                settings.dynamicBounds();
            }
            s1.resistance.ifPresent(settings::resistance);
            s1.hardness.ifPresent(settings::hardness);
            s1.luminance.ifPresent(settings::luminance);
            s1.jumpMult.ifPresent(settings::jumpVelocityMultiplier);
            s1.velMult.ifPresent(settings::velocityMultiplier);
            s1.slipperiness.ifPresent(settings::slipperiness);
            s1.lootTable.ifPresent(settings::drops);
            s1.allowsSpawning.ifPresent(sp -> settings.allowsSpawning((state, world, pos, type) -> {
                if (sp.equals(BlockSettings1.SpawningPossibility.ALL)) {
                    return true;
                }
                if (sp.equals(BlockSettings1.SpawningPossibility.NONE)) {
                    return false;
                }
                if (sp.equals(BlockSettings1.SpawningPossibility.HOSTILE) && !type.getSpawnGroup().isPeaceful()) {
                    return true;
                }
                return sp.equals(BlockSettings1.SpawningPossibility.PASSIVE) && type.getSpawnGroup().isPeaceful();
            }));
        }

        if (settings2.isPresent()) {
            BlockSettings2 s2 = settings2.get();

            s2.bsg.ifPresent(settings::sounds);
            s2.dropBlockId.ifPresent(id -> settings.dropsLike(Util.getOrThrow(Registry.BLOCK, id)));

            settings.solidBlock((s, w, p) -> s2.solidBlock);
            settings.suffocates((s,w,p) -> s2.suffocates);
            settings.blockVision((s,w,p) -> s2.blocksVision);
            settings.postProcess((s,w,p) -> s2.postProcess);
            settings.emissiveLighting((s,w,p) -> s2.emissiveLighting);

            s2.matColor.ifPresent(settings::materialColor);
            s2.dyeColor.ifPresent(settings::materialColor);
            settings.collidable(s2.collidable);
            settings.breakByHand(s2.breakByHand);

            s2.breakTag.ifPresent(id -> {
                if (s2.requiredMiningLevel.isPresent()) {
                    settings.breakByTool(TagRegistry.item(id), s2.requiredMiningLevel.get());
                } else {
                    settings.breakByTool(TagRegistry.item(id));
                }
            });

        }

        return settings;
    }));


}
