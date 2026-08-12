package com.muriane.journeymode.sound;

import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.screen.custom.JourneyModeMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, JourneyMode.MOD_ID);

    public static final Supplier<SoundEvent> RESEARCH = registerSoundEvent("research");
    public static final Supplier<SoundEvent> RESEARCH_FINISH = registerSoundEvent("research_finish");

    private static Supplier<SoundEvent> registerSoundEvent(String name){
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
