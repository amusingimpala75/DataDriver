package com.github.amusingimpala75.datadriver.mixin;

import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
    @Accessor("entries")
    Ingredient.Entry[] accessor$getEntries();
}
