package com.muriane.journeymode.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class HoldImageButton extends ImageButton {
    private boolean isHold;

    public HoldImageButton(int x, int y, int width, int height, boolean isHold, WidgetSprites sprites, OnPress onPress) {
        super(x, y, width, height, sprites, onPress);
        this.isHold = isHold;
    }

    @Override
    public void onPress() {
        this.isHold = !this.isHold;
        super.onPress();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation resourcelocation = this.sprites.get(this.isActive(), this.isHold());
        guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
    }

    public boolean isHold() {
        return isHold;
    }
}
