package com.muriane.journeymode.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.checkerframework.checker.index.qual.PolyUpperBound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModUtils {
    public static void playSoundForPlayer(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        // 在玩家自身位置播放
        ClientboundSoundPacket packet = new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                volume,
                pitch,
                player.level().getRandom().nextLong()
        );
        player.connection.send(packet);
    }

    public static List<String> allItem = new ArrayList<>() {};
    public static int researchSorter(Pair<String, Integer> a, Pair<String, Integer> b) {
        boolean bothFinishOrUnfinish = a.getSecond() == -1 && b.getSecond() == -1 || a.getSecond() != -1 && b.getSecond() != -1;
        int indexA = allItem.indexOf(a.getFirst());
        int indexB = allItem.indexOf(b.getFirst());
        boolean bothContainOrNotContain = indexA == -1 && indexB == -1 || indexA != -1 && indexB != -1;
        return !bothFinishOrUnfinish ?
                a.getSecond() == -1 ? -1 : 1 :
                bothContainOrNotContain ?
                        indexA < indexB ? -1 : 1 :
                        indexA != -1 ? -1 : 1;
    }

    @EventBusSubscriber
    public static class EventHolder {
        @SubscribeEvent
        public static void onLoginForRefreshCreativeModeTabs(ClientPlayerNetworkEvent.LoggingIn event) {
            CreativeModeTabs.tryRebuildTabContents(event.getPlayer().connection.enabledFeatures(), true, event.getPlayer().registryAccess());
            allItem = CreativeModeTabs.searchTab().getDisplayItems().stream().map(stack -> stack.getItem().toString()).toList();
        }
    }
}
