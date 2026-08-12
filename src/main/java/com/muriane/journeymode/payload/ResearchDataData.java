package com.muriane.journeymode.payload;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.func.copy.CopyManager;
import com.muriane.journeymode.util.ModUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

public record ResearchDataData(CopyManager.PlayerResearchData data) implements CustomPacketPayload {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<ResearchDataData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "research_data"));

    public static final StreamCodec<ByteBuf, ResearchDataData> STREAM_CODEC = StreamCodec.of(
            ResearchDataData::encode, ResearchDataData::decode
    );

    public static void encode(ByteBuf buf, ResearchDataData data){
        if (data.data != null) {
            ByteBufCodecs.BOOL.encode(buf, true);
            ByteBufCodecs.INT.encode(buf, data.data.researchMap.size());
            for (String id : data.data.researchMap.keySet()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, id);
                ByteBufCodecs.INT.encode(buf, data.data.researchMap.get(id));
            }
        }else{
            ByteBufCodecs.BOOL.encode(buf, false);
        }
    }

    public static ResearchDataData decode(ByteBuf buf){
        CopyManager.PlayerResearchData data = null;
        if (ByteBufCodecs.BOOL.decode(buf)){
            data = new CopyManager.PlayerResearchData();
            int count = ByteBufCodecs.INT.decode(buf);
            for (int i = 0; i < count; i++) {
                data.researchMap.put(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.INT.decode(buf));
            }
        }
        return new ResearchDataData(data);
    }

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
                    ResearchDataData.TYPE,
                    ResearchDataData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final ResearchDataData data, final IPayloadContext context) {
                PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ResearchDataData(CopyManager.getPlayerData(context.player())));
            }
        }

        public static class ClientPayloadHandler {
            public static void handleDataOnMain(final ResearchDataData data, final IPayloadContext context) {
                CopyManager.LocalPlayerResearchData newData = new CopyManager.LocalPlayerResearchData();
                for (String key : data.data.researchMap.keySet()){
                    if (data.data.researchMap.get(key) >= BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)).getDefaultMaxStackSize()) {
                        newData.researches.add(new Pair<>(key, -1));
                    }else{
                        newData.researches.add(new Pair<>(key, data.data.researchMap.get(key)));
                    }
                }
                newData.researches.sort(ModUtils::researchSorter);
                CopyManager.LOCAL_CACHE = newData;
                CopyManager.LOCAL_CACHE.update();
            }
        }
    }
}
