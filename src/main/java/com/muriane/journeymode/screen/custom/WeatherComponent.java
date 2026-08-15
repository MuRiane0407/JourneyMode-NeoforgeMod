package com.muriane.journeymode.screen.custom;

import com.google.common.collect.Lists;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.payload.GameRuleData;
import com.muriane.journeymode.payload.WeatherData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class WeatherComponent implements Renderable, GuiEventListener, NarratableEntry {
    public static final ResourceLocation TIME_MENU = ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "textures/gui/weather_menu.png");
    public static final WidgetSprites WEATHER_BUTTON = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather/weather_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather/weather_button_highlighted"));
    public static final WidgetSprites FREEZE_WEATHER_BUTTON = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather/freeze_weather_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather/freeze_weather_button_highlighted"));
    public static final WidgetSprites SWITCH_WEATHER_BUTTON = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather/switch_weather_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather/switch_weather_button_highlighted"));
    protected Minecraft minecraft;
    private JourneyModeFunctionComponent parent;
    private int x;
    private int y;
    private int menuWidth;
    private int menuHeight;
    private int menuX;
    private int menuY;
    private Button funcActiveButton;
    private Button freezeWeatherButton;
    private Button switchWeatherButton;
    private boolean funcVisible;
    private boolean visible;
    private boolean init;

    public void init(int x, int y, Minecraft minecraft, JourneyModeFunctionComponent parent) {
        this.minecraft = minecraft;
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.menuWidth = 58;
        this.menuHeight = 32;
        this.menuX = x+(22-this.menuWidth)/2;
        boolean tooNarrow = this.minecraft.screen != null && (y+22+6+this.menuHeight > this.minecraft.screen.height);
        this.menuY = !tooNarrow ? y+22+6 : y-6-this.menuHeight;
        this.visible = true;
        this.funcVisible = false;
        this.initVisuals();
        this.init = true;
    }

    public void initVisuals() {
        this.funcActiveButton = new ImageButton(this.x, this.y, 22, 22, WEATHER_BUTTON,
                button -> {
                    if (!this.funcVisible) {
                        this.parent.closeOtherFunc();
                        this.funcVisible = true;
                    }else{
                        this.funcVisible = false;
                    }
                });
        this.freezeWeatherButton = new HoldImageButton(this.menuX+5, this.menuY+5, 22, 22, this.minecraft.level != null && !this.minecraft.level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).get(),
                FREEZE_WEATHER_BUTTON,
                button -> {
                    if (this.minecraft.level != null && button instanceof HoldImageButton holdImageButton) {
                        PacketDistributor.sendToServer(new GameRuleData("daylight", holdImageButton.isHold() ? 0 : 1));
                    }
                });
        this.switchWeatherButton = new ImageButton(this.menuX+31, this.menuY+5, 22, 22, SWITCH_WEATHER_BUTTON,
                button -> {
                    if (this.minecraft.level.isThundering()){
                        PacketDistributor.sendToServer(new WeatherData("clear"));
                    }else if (this.minecraft.level.isRaining()){
                        PacketDistributor.sendToServer(new WeatherData("thunder"));
                    }else{
                        PacketDistributor.sendToServer(new WeatherData("rain"));
                    }
                });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.init && isVisible()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
            this.funcActiveButton.render(guiGraphics, mouseX, mouseY, partialTick);
            renderFunc(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.pose().popPose();
        }
    }

    public void renderFunc(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.funcVisible) {
            guiGraphics.blit(TIME_MENU, menuX, menuY, this.menuWidth, this.menuHeight, 0, 0, this.menuWidth, this.menuHeight, this.menuWidth, this.menuHeight);
            this.freezeWeatherButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.switchWeatherButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setFuncVisible(boolean funcVisible){
        this.funcVisible = funcVisible;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.funcActiveButton.mouseClicked(mouseX, mouseY, button)){
                return true;
            } else if (this.funcVisible) {
                if (this.freezeWeatherButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }else if (this.switchWeatherButton.mouseClicked(mouseX, mouseY, button)){
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public boolean hasClickedOutside(double mouseX, double mouseY, int button) {
        boolean menu = !funcVisible ||
                mouseX < menuX ||
                mouseY < menuY ||
                mouseX >= menuX + menuWidth ||
                mouseY >= menuY + menuHeight;
        return menu;
    }

    @Override
    public void setFocused(boolean b) {
    }

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
