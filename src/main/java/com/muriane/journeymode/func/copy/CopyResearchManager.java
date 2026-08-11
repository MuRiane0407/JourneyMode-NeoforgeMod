package com.muriane.journeymode.func.copy;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.muriane.journeymode.Config;
import com.muriane.journeymode.screen.custom.CopyItemButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CopyResearchManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<UUID, PlayerResearchData> CACHE = new HashMap<>();
    public static LocalPlayerResearchData LOCAL_CACHE;

    public static Path getPlayerDataPath(Player player) {
        File root = player.getServer().getWorldPath(LevelResource.ROOT).toFile();
        File modDir = new File(root, "journeymode");
        modDir.mkdir();
        File researchDir = new File(modDir, "research");
        researchDir.mkdir();
        File playerJson = new File(researchDir, player.getStringUUID()+".json");
        return playerJson.toPath();
    }

    public static PlayerResearchData loadPlayerData(Player player) {
        Path playerJsonPath = getPlayerDataPath(player);

        if (!Files.exists(playerJsonPath)){
            return new PlayerResearchData();
        }

        try (Reader reader = Files.newBufferedReader(playerJsonPath)) {
            Type type = new TypeToken<PlayerResearchData>(){}.getType();
            PlayerResearchData data = GSON.fromJson(reader, type);
            return data != null ? data : new PlayerResearchData();
        } catch (IOException e) {
            LOGGER.error("{}'s data can't be read: {}", player.getName().getString(), e.getMessage());
            return new PlayerResearchData();
        }
    }

    public static PlayerResearchData getPlayerData(Player player) {
        return CACHE.getOrDefault(player.getUUID(), loadPlayerData(player));
    }

    public static void savePlayerData(Player player, PlayerResearchData data) {
        Path playerjsonPath = getPlayerDataPath(player);

        try (Writer writer = Files.newBufferedWriter(playerjsonPath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("{}'s data can't be saved: {}", player.getName().getString(), e.getMessage());
        }
    }

    public static ItemStack research(ItemStack stack, Player player){
        PlayerResearchData data = getPlayerData(player);
        ItemStack newStack = data.gainResearchProgress(stack);
        savePlayerData(player, data);
        return newStack;
    }

    public static boolean needResearch(ItemStack stack, Player player){
        return canResearch(stack, player) && !hasResearch(stack, player);
    }

    public static boolean canResearch(ItemStack stack, Player player) {
        return true;
    }

    public static boolean hasResearch(ItemStack stack, Player player) {
        PlayerResearchData data = getPlayerData(player);
        return data.hasResearch(stack);
    }

    public static class PlayerResearchData {
        public Map<String, Integer> researchMap = new TreeMap<>();

        public PlayerResearchData() {}

        public ItemStack gainResearchProgress(ItemStack stack){
            int maxProgress = (int) Math.ceil(stack.getItem().getDefaultMaxStackSize() * Config.SERVER.RESEARCH_DEMAND_MULTIPLIER.getAsDouble());
            int progress = researchMap.getOrDefault(stack.getItem().toString(), 0);

            if (progress >= maxProgress) return stack;

            int count = stack.getCount();
            int need = maxProgress - progress;
            if (count < need) {
                researchMap.put(stack.getItem().toString(), progress+count);
                return ItemStack.EMPTY;
            }else{
                researchMap.put(stack.getItem().toString(), maxProgress);
                return new ItemStack(stack.getItem(), count-need);
            }
        }

        public boolean hasResearch(ItemStack stack){
            int maxProgress = (int) Math.ceil(stack.getItem().getDefaultMaxStackSize() * Config.SERVER.RESEARCH_DEMAND_MULTIPLIER.getAsDouble());
            int progress = researchMap.getOrDefault(stack.getItem().toString(), 0);
            return progress >= maxProgress;
        }
    }

    public static class LocalPlayerResearchData {
        public static final int ITEM_PER_COPY_PAGE = 20;
        public static final int ITEM_PER_COPY_PAGE_ROW = 5;

        public Map<String, Integer> researchMap = new TreeMap<>();
        public List<CopyItemButton> buttonList = new ArrayList<>();
        public int maxPage = 0;
        public int page = 0;

        public LocalPlayerResearchData() {}

        public void update() {
            this.maxPage = researchMap.size()/ITEM_PER_COPY_PAGE;
            this.buttonList = new ArrayList<>();
            for (int i = 0 ; i+page*ITEM_PER_COPY_PAGE < CopyResearchManager.LOCAL_CACHE.researchMap.size() && i < ITEM_PER_COPY_PAGE ; i++) {
                int xi = i % ITEM_PER_COPY_PAGE_ROW;
                int yi = i / ITEM_PER_COPY_PAGE_ROW;
                int index = i+page*ITEM_PER_COPY_PAGE;
                String key = researchMap.keySet().stream().toList().get(index);
                buttonList.add(new CopyItemButton(xi, yi, BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)), researchMap.get(key)));
            }
        }
    }
}
