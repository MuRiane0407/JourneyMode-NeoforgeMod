package com.muriane.journeymode.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

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
}
