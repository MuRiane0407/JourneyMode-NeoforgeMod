package com.muriane.journeymode.screen.custom;

import com.google.common.collect.Lists;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.func.copy.CopyManager;
import com.muriane.journeymode.payload.ResearchItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

public class CopyComponent implements Renderable, GuiEventListener, NarratableEntry {
    public static final ResourceLocation COPY_MENU = ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "textures/gui/copy/copy_menu.png");
    public static final WidgetSprites RESEARCH_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/research_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/research_button.highlighted"));
    public static final WidgetSprites RIGHT_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/right_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/right_button_highlighted"));
    public static final WidgetSprites LEFT_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/left_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/left_button_highlighted"));
    public static final WidgetSprites DOWN_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/down_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/down_button_highlighted"));
    public static final WidgetSprites UP_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/up_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/up_button_highlighted"));
    public static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    private int x;
    private int y;
    private EditBox searchBox;
    private Button researchButton;
    private Button copyPageForwardButton;
    private Button copyPageBackwardButton;
    private Button copyTabForwardButton;
    private Button copyTabBackwardButton;
    protected Minecraft minecraft;
    private String lastSearch = "";
    private boolean ignoreTextInput;
    private boolean visible;
    private boolean init;

    public void init(int x, int y, Minecraft minecraft) {
        this.minecraft = minecraft;
        this.x = x;
        this.y = y;
        this.visible = true;
        this.initVisuals();
        this.update();
        this.init = true;
    }

    public void initVisuals() {
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.minecraft.font, x + 25, y + 13, 108, 14, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SEARCH_HINT);
        if (CopyManager.LOCAL_CACHE != null) {
            CopyManager.LOCAL_CACHE.setSearch(s);
        }

        this.researchButton = new ImageButton(x+28, y+165, 22, 22, RESEARCH_BUTTON_SPRITES,
                button -> {
                    if (minecraft.player != null && minecraft.player.containerMenu instanceof JourneyModeMenu) {
                        PacketDistributor.sendToServer(new ResearchItemData());
                    }
                }
        );

        this.copyPageForwardButton = new ImageButton(x+93, y+137, 12, 17, RIGHT_BUTTON_SPRITES,
                button -> {
                    CopyManager.LOCAL_CACHE.forwardItemPage();
                    this.update();
                }
        );
        this.copyPageBackwardButton = new ImageButton(x+38, y+137, 12, 17, LEFT_BUTTON_SPRITES,
                button -> {
                    CopyManager.LOCAL_CACHE.backwardItemPage();
                    this.update();
                }
        );

        this.copyTabForwardButton = new ImageButton(x-20, y+155, 17, 12, DOWN_BUTTON_SPRITES,
                button -> {
                    CopyManager.LOCAL_CACHE.forwardTabPage();
                    this.update();
                }
        );
        this.copyTabBackwardButton = new ImageButton(x-20, y+3, 17, 12, UP_BUTTON_SPRITES,
                button -> {
                    CopyManager.LOCAL_CACHE.backwardTabPage();
                    this.update();
                }
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.init && isVisible()){
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
            guiGraphics.blit(COPY_MENU, x, y, 147, 192, 0, 0, 147, 192, 147, 192);
            this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
            this.researchButton.render(guiGraphics, mouseX, mouseY, partialTick);
            renderCopyPage(guiGraphics, mouseX, mouseY, partialTick, x, y);
            guiGraphics.pose().popPose();
        }
    }

    public void renderCopyPage(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int x, int y) {
        Player player = Minecraft.getInstance().player;
        if (CopyManager.LOCAL_CACHE != null && player != null) {
            int itemX = x+11;
            int itemY = y+31;
            for (CopyItemButton button : CopyManager.LOCAL_CACHE.itemButtonList) {
                button.renderWidget(guiGraphics, mouseX, mouseY, partialTick, itemX, itemY);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                if (button.isMouseOver(mouseX, mouseY)) guiGraphics.renderComponentTooltip(this.minecraft.font, button.getTooltipText(), mouseX, mouseY);
                guiGraphics.pose().popPose();
            }

            int tabX = x-30;
            int tabY = y+18;
            for (CopyTabButton button : CopyManager.LOCAL_CACHE.tabButtonList) {
                button.renderWidget(guiGraphics, mouseX, mouseY, partialTick, tabX, tabY);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                if (button.isMouseOver(mouseX, mouseY)) guiGraphics.renderComponentTooltip(this.minecraft.font, button.getTooltipText(), mouseX, mouseY);
                guiGraphics.pose().popPose();
            }

            updatePageButton();

            this.copyPageForwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.copyPageBackwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
            if (CopyManager.LOCAL_CACHE.maxItemPage > 1){
                Component component = Component.translatable("gui.journeymode.copy_page.page", CopyManager.LOCAL_CACHE.itemPage, CopyManager.LOCAL_CACHE.maxItemPage);
                int length = this.minecraft.font.width(component);
                guiGraphics.drawString(this.minecraft.font, component, x - length / 2 + 73, y + 141, -1, false);
            }

            this.copyTabForwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.copyTabBackwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public void update() {
        updatePageButton();
    }

    public void updatePageButton() {
        if (CopyManager.LOCAL_CACHE != null){
            this.copyPageForwardButton.visible = CopyManager.LOCAL_CACHE.maxItemPage > 1 && CopyManager.LOCAL_CACHE.itemPage < CopyManager.LOCAL_CACHE.maxItemPage;
            this.copyPageBackwardButton.visible = CopyManager.LOCAL_CACHE.maxItemPage > 1 && CopyManager.LOCAL_CACHE.itemPage > 1;
            this.copyTabForwardButton.visible = CopyManager.LOCAL_CACHE.maxTabPage > 1 && CopyManager.LOCAL_CACHE.tabPage < CopyManager.LOCAL_CACHE.maxTabPage;
            this.copyTabBackwardButton.visible = CopyManager.LOCAL_CACHE.maxTabPage > 1 && CopyManager.LOCAL_CACHE.tabPage > 1;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTextInput = false;
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                this.checkSearchStringUpdate();
                CopyManager.LOCAL_CACHE.setSearch(this.searchBox.getValue());
                CopyManager.LOCAL_CACHE.update();
                return true;
            } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256) {
                return true;
            } else if (this.minecraft.options.keyChat.matches(keyCode, scanCode) && !this.searchBox.isFocused()) {
                this.ignoreTextInput = true;
                this.searchBox.setFocused(true);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.ignoreTextInput) {
            return false;
        } else if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.searchBox.charTyped(codePoint, modifiers)) {
                this.checkSearchStringUpdate();
                CopyManager.LOCAL_CACHE.setSearch(this.searchBox.getValue());
                CopyManager.LOCAL_CACHE.update();
                return true;
            } else {
                return GuiEventListener.super.charTyped(codePoint, modifiers);
            }
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
                this.searchBox.setFocused(true);
                return true;
            } else {
                this.searchBox.setFocused(false);
                if (this.researchButton.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }else if (this.copyPageForwardButton.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }else if (this.copyPageBackwardButton.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }else if (this.copyTabForwardButton.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }else if (this.copyTabBackwardButton.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }else{
                    for (CopyItemButton copyItemButton : CopyManager.LOCAL_CACHE.itemButtonList){
                        if (copyItemButton.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                    for (CopyTabButton copyTabButton : CopyManager.LOCAL_CACHE.tabButtonList){
                        if (copyTabButton.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }

    private void checkSearchStringUpdate() {
        String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
        this.pirateSpeechForThePeople(s);
        if (!s.equals(this.lastSearch)) {
            this.lastSearch = s;
        }
    }

    private void pirateSpeechForThePeople(String text) {
        if ("excitedze".equals(text)) {
            LanguageManager languagemanager = this.minecraft.getLanguageManager();
            String s = "en_pt";
            LanguageInfo languageinfo = languagemanager.getLanguage("en_pt");
            if (languageinfo == null || languagemanager.getSelected().equals("en_pt")) {
                return;
            }

            languagemanager.setSelected("en_pt");
            this.minecraft.options.languageCode = "en_pt";
            this.minecraft.reloadResourcePacks();
            this.minecraft.options.save();
        }
    }

    public boolean hasClickedOutside(double mouseX, double mouseY, int mouseButton) {
        boolean main = mouseX < x ||
                mouseY < y ||
                mouseX >= x+147 ||
                mouseY >= y+166;
        boolean research = mouseX < x ||
                mouseY < y+166 ||
                mouseX >= x+56 ||
                mouseY >= y+166+26;
        boolean tab = mouseX < x-35 ||
                mouseY < y ||
                mouseX >= x ||
                mouseY >= y+166+26;
        return main && research && tab;
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
