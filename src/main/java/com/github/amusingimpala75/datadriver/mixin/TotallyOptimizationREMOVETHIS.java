package com.github.amusingimpala75.datadriver.mixin;

import net.minecraft.client.MinecraftClient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//Just for fun, not in mixins.json, so doesn't load
@SuppressWarnings("ALL")
@Mixin(MinecraftClient.class)
public class TotallyOptimizationREMOVETHIS {
    @Redirect(method = "render", at=@At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentFps:I", opcode = Opcodes.GETSTATIC))
    public int modify$optimize() {
        return 100000000;
    }
}
