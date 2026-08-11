package com.muriane.journeymode.payload;

import com.mojang.logging.LogUtils;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.screen.custom.JourneyModeMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

public record DeleteContainerItemData(int slot) implements CustomPacketPayload {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<DeleteContainerItemData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "delete_container_item"));

    public static final StreamCodec<ByteBuf, DeleteContainerItemData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DeleteContainerItemData::slot,
            DeleteContainerItemData::new
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
                    DeleteContainerItemData.TYPE,
                    DeleteContainerItemData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final DeleteContainerItemData data, final IPayloadContext context) {
                if (data.slot < context.player().containerMenu.slots.size()) {
                    context.player().containerMenu.setItem(data.slot, 0, ItemStack.EMPTY);
                }
            }
        }

        public static class ClientPayloadHandler {
            public static void handleDataOnMain(final DeleteContainerItemData data, final IPayloadContext context) {

            }
        }
    }
}
