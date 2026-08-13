package com.muriane.journeymode.payload;

import com.mojang.logging.LogUtils;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.func.copy.CopyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

public record CopyItemData(String item, int button, boolean shift) implements CustomPacketPayload {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<CopyItemData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "copy_item"));

    public static final StreamCodec<ByteBuf, CopyItemData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CopyItemData::item,
            ByteBufCodecs.INT,
            CopyItemData::button,
            ByteBufCodecs.BOOL,
            CopyItemData::shift,
            CopyItemData::new
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
                    CopyItemData.TYPE,
                    CopyItemData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final CopyItemData data, final IPayloadContext context) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.item));
                ItemStack stackCarried = context.player().containerMenu.getCarried();
                int count = data.button == 0 ? item.getDefaultMaxStackSize() : 1;
                ItemStack stack = new ItemStack(item, count);

                if (stackCarried.is(item) || stackCarried.isEmpty()) {
                    if (CopyManager.hasResearch(item.getDefaultInstance(), context.player())) {
                        if (data.shift) {
                            context.player().addItem(stack);
                        } else {
                            if (stackCarried.is(item)) {
                                stackCarried.setCount(Math.min(stackCarried.getCount() + count, stackCarried.getMaxStackSize()));
                            } else if (stackCarried.isEmpty()) {
                                context.player().containerMenu.setCarried(stack);
                            }
                        }
                    }
                }else{
                    if (data.shift) {
                        context.player().addItem(stack);
                    }else{
                        context.player().containerMenu.setCarried(ItemStack.EMPTY);
                    }
                }
            }
        }

        public static class ClientPayloadHandler {
            public static void handleDataOnMain(final CopyItemData data, final IPayloadContext context) {

            }
        }
    }
}
