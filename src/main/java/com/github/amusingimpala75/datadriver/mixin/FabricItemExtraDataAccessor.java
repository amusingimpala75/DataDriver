package com.github.amusingimpala75.datadriver.mixin;

import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.impl.item.FabricItemInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FabricItemInternals.ExtraData.class)
public interface FabricItemExtraDataAccessor {
    @Accessor(value = "equipmentSlotProvider", remap = false)
    EquipmentSlotProvider accessor$getEquipmentSlot();
}
