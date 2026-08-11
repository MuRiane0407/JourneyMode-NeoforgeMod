package com.muriane.journeymode.screen;

import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.screen.custom.JourneyModeMenu;
import com.muriane.journeymode.screen.custom.JourneyModeScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, JourneyMode.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<JourneyModeMenu>> JOURNEY_MODE_MENU =
            registerMenuType("journey_mode", (IContainerFactory<JourneyModeMenu>) JourneyModeMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory){
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }

    @EventBusSubscriber
    public static class EventHolder{
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.JOURNEY_MODE_MENU.get(), JourneyModeScreen::new);
        }
    }
}
