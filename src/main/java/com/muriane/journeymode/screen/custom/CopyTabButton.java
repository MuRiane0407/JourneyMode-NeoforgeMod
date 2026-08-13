package com.muriane.journeymode.screen.custom;

import com.google.common.collect.Lists;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.func.copy.CopyManager;
import com.muriane.journeymode.payload.CopyItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class CopyTabButton extends ImageButton {
    public static final WidgetSprites TAB = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/tab"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/tab"));
    public static final WidgetSprites TAB_SELECTED = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/tab_selected"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/tab_selected"));
    public static final int TAB_SIZE_X = 35;
    public static final int TAB_SIZE_Y = 27;
    private final CreativeModeTab tab;
    private final int i;
    private final boolean selected;

    public CopyTabButton(int i, CreativeModeTab tab, boolean selected) {
        super(0, 0, TAB_SIZE_X, TAB_SIZE_Y,
                selected ? TAB_SELECTED : TAB,
                button -> {}
        );
        this.i = i;
        this.tab = tab;
        this.selected = selected;
    }

    public List<Component> getTooltipText() {
        return List.of(tab.getDisplayName());
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int x, int y){
        this.setX(this.selected ? x-2 : x);
        this.setY(y+i*TAB_SIZE_Y);
        this.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.renderFakeItem(tab.getIconItem(), this.getX()+10, this.getY()+5);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        CopyManager.LOCAL_CACHE.switchTab(this.i);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0;
    }
}
