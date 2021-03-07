package com.github.amusingimpala75.datadriver.mixin;

import com.github.amusingimpala75.datadriver.duck.ItemDuck;
import com.mojang.serialization.Codec;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements ItemDuck {

    @Unique
    private Item.Settings settings;
    @Unique
    private Codec<Item> codec;

    @Inject(method = "<init>", at=@At("TAIL"))
    public void inject$saveSettings(Item.Settings settings, CallbackInfo ci) {
        this.settings = settings;
    }

    @Override
    public Item.Settings getSettings() {
        return settings;
    }

    public Codec<Item> getCodec() {
        return codec;
    }

    public Item setCodec(Codec<Item> codec) {
        this.codec = codec;
        return (Item)(Object)this;
    }
}
