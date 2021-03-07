package com.github.amusingimpala75.datadriver.duck;

import com.mojang.serialization.Codec;
import net.minecraft.item.Item;

public interface ItemDuck {
    Item.Settings getSettings();
    Codec<Item> getCodec();
    Item setCodec(Codec<Item> codec);
}
