package com.muriane.journeymode.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public abstract class InventoryMenuMixin {
    @Mixin(InventoryMenu.class)
    public interface InventoryMenuInterface {
        @Accessor("owner")
        Player getOwner();
    }
}
