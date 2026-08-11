package com.muriane.journeymode.screen.custom;

import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.payload.OpenMenuData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class JourneyModeButton extends ImageButton {
    public static final WidgetSprites MENU_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "main/menu_button"), ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "main/menu_button_highlighted"));

    public JourneyModeButton(int x, int y) {
        super(x, y, 20, 18, MENU_BUTTON_SPRITES, JourneyModeButton::onPress);
    }

    public static void onPress(Button button) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            if (!(minecraft.screen instanceof JourneyModeScreen)) {
                PacketDistributor.sendToServer(new OpenMenuData(0));
            }else{
                minecraft.screen.onClose();
            }
        }
    }

    @EventBusSubscriber
    public static class EventHolder{
        @SubscribeEvent
        public static void onOpenMenu(ScreenEvent.Init.Post event){
            if (event.getScreen() instanceof InventoryScreen screen) {
                event.addListener(new JourneyModeButton(screen.getGuiLeft() + 104 + 26, screen.height / 2 - 22));
            }
        }
    }
}
