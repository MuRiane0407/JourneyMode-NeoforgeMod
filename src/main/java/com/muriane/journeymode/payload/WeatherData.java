package com.muriane.journeymode.payload;

import com.mojang.logging.LogUtils;
import com.muriane.journeymode.JourneyMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

public record WeatherData(String weather) implements CustomPacketPayload {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<WeatherData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "weather"));

    public static final StreamCodec<ByteBuf, WeatherData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            WeatherData::weather,
            WeatherData::new
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
                    WeatherData.TYPE,
                    WeatherData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final WeatherData data, final IPayloadContext context) {
                MinecraftServer server = context.player().level().getServer();
                if (server != null) {
                    switch (data.weather) {
                        case "clear":
                            server.overworld().setWeatherParameters(ServerLevel.RAIN_DELAY.sample(server.overworld().random), 0, false, false);
                            break;
                        case "rain":
                            server.overworld().setWeatherParameters(0, ServerLevel.RAIN_DURATION.sample(server.overworld().random), true, false);
                            break;
                        case "thunder":
                            server.overworld().setWeatherParameters(0, ServerLevel.THUNDER_DURATION.sample(server.overworld().random), true, true);
                    }
                }
            }
        }

        public static class ClientPayloadHandler {
            public static void handleDataOnMain(final WeatherData data, final IPayloadContext context) {

            }
        }
    }
}
