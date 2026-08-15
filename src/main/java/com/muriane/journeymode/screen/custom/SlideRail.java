package com.muriane.journeymode.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class SlideRail extends Button {
    protected final ResourceLocation slideRail;
    protected final WidgetSprites slider;
    private int outline;
    private int sliderWidth;
    private int sliderHeight;
    private float percent;

    public SlideRail(int x, int y, float percent, int outline, int width, int height, ResourceLocation sliderRail, int sliderWidth, int sliderHeight, WidgetSprites slider, OnPress onPress) {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.slideRail = sliderRail;
        this.slider = slider;
        this.outline = outline;
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
        this.percent = percent;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(slideRail, this.getX(), this.getY(), this.width, this.height);
        ResourceLocation resourcelocation = this.slider.get(this.isActive(), this.isHoveredOrFocused());
        int sliderX = this.getX()+outline+(int) (percent*(this.width-2*outline))-sliderWidth/2;
        int sliderY = this.getY()+height/2-sliderHeight/2;
        guiGraphics.blitSprite(resourcelocation, sliderX, sliderY, this.sliderWidth, this.sliderHeight);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.percent = Math.min(1.0F, Math.max(0.0F, (float) ((mouseX-(this.getX()+outline))/(1.0*this.width-2*outline))));
        super.onClick(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.onClick(mouseX, mouseY, button);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public float getPercent() {
        return this.percent;
    }
}
