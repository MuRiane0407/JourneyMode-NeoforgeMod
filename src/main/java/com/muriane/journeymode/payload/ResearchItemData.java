package com.muriane.journeymode.payload;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.muriane.journeymode.JourneyMode;
import com.muriane.journeymode.func.CopyManager;
import com.muriane.journeymode.screen.custom.JourneyModeMenu;
import com.muriane.journeymode.util.ModSounds;
import com.muriane.journeymode.util.ModUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

public record ResearchItemData() implements CustomPacketPayload {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<ResearchItemData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JourneyMode.MOD_ID, "research_item"));

    public static final StreamCodec<ByteBuf, ResearchItemData> STREAM_CODEC = StreamCodec.unit(new ResearchItemData());

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
                    ResearchItemData.TYPE,
                    ResearchItemData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final ResearchItemData data, final IPayloadContext context) {
                int slot = JourneyModeMenu.RESEARCH_SLOT;
                if (slot < context.player().containerMenu.slots.size()) {
                    ItemStack stack = context.player().containerMenu.getSlot(slot).getItem();
                    if (CopyManager.needResearch(stack, context.player())) {
                        Pair<ItemStack, Boolean> pair = CopyManager.research(stack, context.player());
                        context.player().containerMenu.setItem(slot, 0, pair.getFirst());

                        ModUtils.playSoundForPlayer((ServerPlayer) context.player(), ModSounds.RESEARCH.get(), 1.0F, 1.0F);
                        if (pair.getSecond()) ModUtils.playSoundForPlayer((ServerPlayer) context.player(), ModSounds.RESEARCH_FINISH.get(), 1.0F, 1.0F);

                        PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ResearchDataData(CopyManager.getPlayerData(context.player()), false));
                    }
                }
            }
        }

        public static class ClientPayloadHandler {
            public static void handleDataOnMain(final ResearchItemData data, final IPayloadContext context) {

            }
        }
    }
}
