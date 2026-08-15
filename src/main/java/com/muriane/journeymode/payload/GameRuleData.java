package com.muriane.journeymode.payload;

import com.mojang.logging.LogUtils;
import com.muriane.journeymode.JourneyMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

public record GameRuleData(String name, int value) implements CustomPacketPayload {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<GameRuleData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "game_rule"));

    public static final StreamCodec<ByteBuf, GameRuleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            GameRuleData::name,
            ByteBufCodecs.INT,
            GameRuleData::value,
            GameRuleData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @EventBusSubscriber
    public static class DataHolder{
        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event){
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(
                    GameRuleData.TYPE,
                    GameRuleData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final GameRuleData data, final IPayloadContext context) {
                Level level = context.player().level();
                switch (data.name){
                    case "daylight":
                        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(data.value != 0, level.getServer());
                        break;
                    case "weather_cycle":
                        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(data.value != 0, level.getServer());
                        break;
                }
            }
        }

        public static class ClientPayloadHandler {
            public static void handleDataOnMain(final GameRuleData data, final IPayloadContext context) {

            }
        }
    }
}
