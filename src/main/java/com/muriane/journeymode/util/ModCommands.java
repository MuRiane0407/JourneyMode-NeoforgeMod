package com.muriane.journeymode.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.muriane.journeymode.func.CopyManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("journeymode")
                        .then(Commands.literal("research")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("item", ItemArgument.item(context))
                                                        .executes(
                                                                context1 ->
                                                                        addResearch(context1.getSource(), EntityArgument.getPlayers(context1, "targets"), ItemArgument.getItem(context1, "item"), -1)
                                                        )
                                                        .then(Commands.argument("count", IntegerArgumentType.integer())
                                                                .executes(
                                                                        context1 ->
                                                                                addResearch(context1.getSource(), EntityArgument.getPlayers(context1, "targets"), ItemArgument.getItem(context1, "item"), IntegerArgumentType.getInteger(context1, "count"))
                                                                )
                                                        )
                                                )
                                                .then(Commands.literal("all")
                                                        .executes(
                                                                context1 ->
                                                                        addAllResearch(context1.getSource(), EntityArgument.getPlayers(context1, "targets"))
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("item", ItemArgument.item(context))
                                                        .executes(
                                                                context1 ->
                                                                        removeResearch(context1.getSource(), EntityArgument.getPlayers(context1, "targets"), ItemArgument.getItem(context1, "item"), -1)
                                                        )
                                                        .then(Commands.argument("count", IntegerArgumentType.integer())
                                                                .executes(
                                                                        context1 ->
                                                                                removeResearch(context1.getSource(), EntityArgument.getPlayers(context1, "targets"), ItemArgument.getItem(context1, "item"), IntegerArgumentType.getInteger(context1, "count"))
                                                                )
                                                        )
                                                )
                                                .then(Commands.literal("all")
                                                        .executes(
                                                                context1 ->
                                                                        removeAllResearch(context1.getSource(), EntityArgument.getPlayers(context1, "targets"))
                                                        )
                                                )
                                        )
                                )
                        ));
    }

    public static int addResearch(CommandSourceStack source, Collection<ServerPlayer> targets, ItemInput item, int count) {
        for (ServerPlayer player : targets){
            int i = CopyManager.research(item.getItem(), count, player);
            if (i == -1){
                source.sendFailure(Component.translatable("commands.journeymode.research.hasResearch", player.getDisplayName(), item.getItem().getDefaultInstance().getHoverName()));
            }else{
                source.sendSuccess(() -> Component.translatable("commands.journeymode.research.research", player.getDisplayName(), item.getItem().getDefaultInstance().getHoverName(), count == -1 ? "all" : count), true);
            }
        }
        return targets.size();
    }

    public static int addAllResearch(CommandSourceStack source, Collection<ServerPlayer> targets){
        for (ServerPlayer player : targets){
            CopyManager.researchAll(player);
            source.sendSuccess(() -> Component.translatable("commands.journeymode.research.researchAll", player.getDisplayName()), true);
        }
        return targets.size();
    }

    public static int removeResearch(CommandSourceStack source, Collection<ServerPlayer> targets, ItemInput item, int count){
        for (ServerPlayer player : targets){
            int i = CopyManager.forget(item.getItem(), count, player);
            if (i == -1){
                source.sendFailure(Component.translatable("commands.journeymode.research.notResearch", player.getDisplayName(), item.getItem().getDefaultInstance().getHoverName()));
            }else{
                source.sendSuccess(() -> Component.translatable("commands.journeymode.research.forget", player.getDisplayName(), item.getItem().getDefaultInstance().getHoverName(), count == -1 ? "all" : count), true);
            }
        }
        return targets.size();
    }

    public static int removeAllResearch(CommandSourceStack source, Collection<ServerPlayer> targets){
        for (ServerPlayer player : targets){
            CopyManager.forgetAll(player);
            source.sendSuccess(() -> Component.translatable("commands.journeymode.research.forgetAll", player.getDisplayName()), true);
        }
        return targets.size();
    }

    @EventBusSubscriber
    public static class EventHolder {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event){
            ModCommands.register(event.getDispatcher(), event.getBuildContext());
        }
    }
}
