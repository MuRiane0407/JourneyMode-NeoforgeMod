package com.muriane.journeymode.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.muriane.journeymode.screen.custom.JourneyModeButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @Shadow
    protected Minecraft minecraft;

    @Inject(
            method = "updateScreenPosition",
            at = @At(
                    value = "RETURN"
            )
    )
    private void onUndateScreenPosition(int width, int imageWidth, CallbackInfoReturnable<Integer> cir, @Local(ordinal = 2) int i){
        if (minecraft.screen instanceof InventoryScreen screen) {
            for (GuiEventListener listener : screen.children()) {
                if (listener instanceof JourneyModeButton button){
                    button.setPosition(i + 104 + 26, screen.height / 2 - 22);
                }
            }
        }
    }
}
