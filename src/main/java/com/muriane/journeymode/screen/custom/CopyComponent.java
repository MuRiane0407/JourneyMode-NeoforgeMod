package com.muriane.journeymode.screen.custom;

import com.google.common.collect.Lists;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.func.copy.CopyResearchManager;
import com.muriane.journeymode.payload.ResearchDataData;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CopyComponent implements Renderable, GuiEventListener, NarratableEntry {
    public static final ResourceLocation COPY_MENU_LOCATION = ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "textures/gui/copy/copy_menu.png");
    public static final WidgetSprites RESEARCH_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/research_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy/research_button.highlighted"));
    public static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    private int xOffset;
    private int width;
    private int height;
    @Nullable
    private EditBox searchBox;
    private Button researchButton;
    protected Minecraft minecraft;
    private String lastSearch = "";
    private boolean ignoreTextInput;
    private boolean visible;
    private boolean widthTooNarrow;

    public void init(int width, int height, Minecraft minecraft, boolean widthTooNarrow) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.widthTooNarrow = widthTooNarrow;
        this.visible = true;
        this.initVisuals();
    }

    public void initVisuals() {
        PacketDistributor.sendToServer(new ResearchDataData(null));
        this.xOffset = this.widthTooNarrow ? 0 : 86;
        int i = (this.width - 147) / 2 - this.xOffset;
        int j = (this.height - 166) / 2;
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.minecraft.font, i + 25, j + 13, 108, 14, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SEARCH_HINT);
        this.researchButton = new ImageButton(i+30, j+163, 18, 18, RESEARCH_BUTTON_SPRITES,
                button -> {
                    if (minecraft.player != null && minecraft.player.containerMenu instanceof JourneyModeMenu menu) {
                        PacketDistributor.sendToServer(new ResearchItemData());
                    }
                }
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isVisible()){
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 150.0F);
            int i = (this.width - 148) / 2 - this.xOffset;
            int j = (this.height - 166) / 2;
            guiGraphics.blit(COPY_MENU_LOCATION, i, j, 1, 1, 147, 188);
            this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
            this.researchButton.render(guiGraphics, mouseX, mouseY, partialTick);
            renderCopyPage(guiGraphics, mouseX, mouseY, partialTick, i+11, j+31);
            guiGraphics.pose().popPose();
        }
    }

    public void renderCopyPage(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int startX, int startY) {
        Player player = Minecraft.getInstance().player;
        if (CopyResearchManager.LOCAL_CACHE != null && player != null) {
            for (CopyItemButton button : CopyResearchManager.LOCAL_CACHE.buttonList) {
                button.renderWidget(guiGraphics, mouseX, mouseY, partialTick, startX, startY);
                if (button.isMouseOver(mouseX, mouseY)) guiGraphics.renderComponentTooltip(this.minecraft.font, button.getTooltipText(), mouseX, mouseY);
            }
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
                }else{
                    for (CopyItemButton copyItemButton : CopyResearchManager.LOCAL_CACHE.buttonList){
                        if (copyItemButton.mouseClicked(mouseX, mouseY, button)) {
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
            this.updateCollections(false);
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

    private void updateCollections(boolean resetPageNumber) {

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
//        list.add(this.searchBox);
        Screen.NarratableSearchResult screen$narratablesearchresult = Screen.findNarratableWidget(list, (NarratableEntry) null);
        if (screen$narratablesearchresult != null) {
            screen$narratablesearchresult.entry.updateNarration(narrationElementOutput.nest());
        }
    }
}
