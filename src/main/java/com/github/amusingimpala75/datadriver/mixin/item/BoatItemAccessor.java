package com.github.amusingimpala75.datadriver.mixin.item;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BoatItem.class)
public interface BoatItemAccessor {
    @Accessor("type")
    BoatEntity.Type accessor$getType();
}
