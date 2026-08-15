package com.muriane.journeymode.screen.custom;

import com.google.common.collect.Lists;
import com.muriane.journeymode.JourneyMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class JourneyModeFunctionComponent implements Renderable, GuiEventListener, NarratableEntry {
    public static final ResourceLocation FUNCTION_MENU = ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "textures/gui/function_menu.png");
    protected Minecraft minecraft;
    private int x;
    private int y;
    private TimeComponent timeComponent = new TimeComponent();
    private WeatherComponent weatherComponent = new WeatherComponent();
    private boolean ignoreTextInput;
    private boolean visible;
    private boolean init;

    public void init(int x, int y, Minecraft minecraft) {
        this.minecraft = minecraft;
        this.x = x;
        this.y = y;
        this.visible = true;
        this.initVisuals();
        this.init = true;
    }

    public void initVisuals() {
        this.timeComponent.init(this.x+5, this.y+5, minecraft, this);
        this.weatherComponent.init(this.x+31, this.y+5, minecraft, this);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.init && isVisible()){
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
            guiGraphics.blit(FUNCTION_MENU, x, y, 176, 32, 0, 0, 176, 32, 176, 32);
            this.timeComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            this.weatherComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.pose().popPose();
        }
    }

    public void closeOtherFunc() {
        this.timeComponent.setFuncVisible(false);
        this.weatherComponent.setFuncVisible(false);
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTextInput = false;
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            return false;
        } else {
            return false;
        }
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.ignoreTextInput) {
            return false;
        } else if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            return GuiEventListener.super.charTyped(codePoint, modifiers);
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.timeComponent.mouseClicked(mouseX, mouseY, button)){
                return true;
            }else if (this.weatherComponent.mouseClicked(mouseX, mouseY, button)){
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.timeComponent.mouseDragged(mouseX, mouseY, button, dragX, dragY)){
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public boolean hasClickedOutside(double mouseX, double mouseY, int button) {
        boolean main = mouseX < x ||
                mouseY < y ||
                mouseX >= x+176 ||
                mouseY >= y+32;
        return main &&
                this.timeComponent.hasClickedOutside(mouseX, mouseY, button) &&
                this.weatherComponent.hasClickedOutside(mouseX, mouseY, button);
    }

    @Override
    public void setFocused(boolean b) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        List<NarratableEntry> list = Lists.newArrayList();
        Screen.NarratableSearchResult screen$narratablesearchresult = Screen.findNarratableWidget(list, (NarratableEntry) null);
        if (screen$narratablesearchresult != null) {
            screen$narratablesearchresult.entry.updateNarration(narrationElementOutput.nest());
        }
    }
}
