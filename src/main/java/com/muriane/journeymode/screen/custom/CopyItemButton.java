package com.muriane.journeymode.screen.custom;

import com.google.common.collect.Lists;
import com.muriane.journeymode.Config;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.payload.CopyItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class CopyItemButton extends ImageButton {
    public static final WidgetSprites SLOT_COPYABLE_SPRITE = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/slot_copyable"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/slot_copyable"));
    public static final WidgetSprites SLOT_NONCOPYABLE_SPRITE = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/slot_noncopyable"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/slot_noncopyable"));
    public static final int COPY_SLOT_SIZE = 25;
    private final Item item;
    private final int xi;
    private final int yi;
    private final int progress;

    public CopyItemButton(int xi, int yi, Item item, int progress) {
        super(0, 0, COPY_SLOT_SIZE, COPY_SLOT_SIZE,
                progress == -1 ? SLOT_COPYABLE_SPRITE : SLOT_NONCOPYABLE_SPRITE,
                button -> {}
        );
        this.xi = xi;
        this.yi = yi;
        this.item = item;
        this.progress = progress;
    }

    public List<Component> getTooltipText() {
        List<Component> list = Lists.newArrayList(Screen.getTooltipFromItem(Minecraft.getInstance(), this.item.getDefaultInstance()));
        if (progress != -1) list.add(Component.translatable("gui.journeymode.copy_item_button.tooltip.noncopyable", this.progress, (int) (this.item.getDefaultMaxStackSize() * Config.SERVER.RESEARCH_DEMAND_MULTIPLIER.getAsDouble())).withStyle(ChatFormatting.GRAY));

        return list;
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int x, int y){
        this.setX(x+xi*COPY_SLOT_SIZE);
        this.setY(y+yi*COPY_SLOT_SIZE);
        this.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.renderFakeItem(item.getDefaultInstance(), this.getX()+4, this.getY()+4);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (progress == -1) {
            PacketDistributor.sendToServer(new CopyItemData(item.toString(), button, Screen.hasShiftDown()));
        }
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }
}
