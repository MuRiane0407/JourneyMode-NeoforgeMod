package com.muriane.journeymode.func.copy;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.muriane.journeymode.Config;
import com.muriane.journeymode.screen.custom.CopyItemButton;
import com.muriane.journeymode.screen.custom.CopyTabButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
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

import static com.muriane.journeymode.util.ModUtils.allItem;

public class CopyManager {
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

    public static Pair<ItemStack, Boolean> research(ItemStack stack, Player player){
        PlayerResearchData data = getPlayerData(player);
        Pair<ItemStack, Boolean> pair = data.gainResearchProgress(stack);
        savePlayerData(player, data);
        return pair;
    }

    public static int research(Item item, int count, Player player){
        PlayerResearchData data = getPlayerData(player);
        int i = data.gainResearchProgress(item.toString(), count);
        savePlayerData(player, data);
        return i;
    }

    public static void researchAll(Player player){
        PlayerResearchData data = getPlayerData(player);
        for (String id : allItem) {
            data.gainResearchProgress(id, -1);
        }
        savePlayerData(player, data);
    }

    public static int forget(Item item, int count, Player player){
        PlayerResearchData data = getPlayerData(player);
        int i = data.loseResearchProgress(item.toString(), count);
        savePlayerData(player, data);
        return i;
    }

    public static void forgetAll(Player player){
        PlayerResearchData data = getPlayerData(player);
        for (String id : allItem) {
            data.loseResearchProgress(id, -1);
        }
        savePlayerData(player, data);
    }

    public static boolean needResearch(ItemStack stack, Player player){
        return canResearch(stack, player) && !hasResearch(stack, player);
    }

    public static boolean canResearch(ItemStack stack, Player player) {
        return !stack.isEmpty();
    }

    public static boolean hasResearch(ItemStack stack, Player player) {
        PlayerResearchData data = getPlayerData(player);
        return data.hasResearch(stack);
    }

    public static class PlayerResearchData {
        public Map<String, Integer> researchMap = new TreeMap<>();

        public PlayerResearchData() {}

        public Pair<ItemStack, Boolean> gainResearchProgress(ItemStack stack){
            int maxProgress = (int) Math.ceil(stack.getItem().getDefaultMaxStackSize() * Config.SERVER.RESEARCH_DEMAND_MULTIPLIER.getAsDouble());
            int progress = researchMap.getOrDefault(stack.getItem().toString(), 0);

            if (progress >= maxProgress) return new Pair<>(stack, true);

            int count = stack.getCount();
            int need = maxProgress - progress;
            if (count < need) {
                researchMap.put(stack.getItem().toString(), progress+count);
                return new Pair<>(ItemStack.EMPTY, false);
            }else{
                researchMap.put(stack.getItem().toString(), maxProgress);
                return new Pair<>(new ItemStack(stack.getItem(), count-need), true);
            }
        }

        public int gainResearchProgress(Item item, int count){
            int maxProgress = (int) Math.ceil(item.getDefaultMaxStackSize() * Config.SERVER.RESEARCH_DEMAND_MULTIPLIER.getAsDouble());
            int progress = researchMap.getOrDefault(item.toString(), 0);

            if (progress >= maxProgress) return -1;

            if (count != -1) {
                int need = maxProgress - progress;
                if (count < need) {
                    researchMap.put(item.toString(), progress + count);
                } else {
                    researchMap.put(item.toString(), maxProgress);
                }
            }else{
                researchMap.put(item.toString(), maxProgress);
            }
            return 1;
        }

        public int gainResearchProgress(String id, int count){
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return this.gainResearchProgress(item, count);
        }

        public int loseResearchProgress(Item item, int count){
            int minProgress = 0;
            int progress = researchMap.getOrDefault(item.toString(), 0);

            if (progress <= minProgress) return -1;

            if (count != -1) {
                if (count < progress) {
                    researchMap.put(item.toString(), progress - count);
                } else {
                    researchMap.remove(item.toString());
                }
            }else{
                researchMap.remove(item.toString());
            }
            return 1;
        }

        public int loseResearchProgress(String id, int count){
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            return this.loseResearchProgress(item, count);
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
        public static final int TAB_PER_COL = 5;
        public List<Pair<String, Integer>> researches = new ArrayList<>();
        public String search = "";
        public List<CopyItemButton> itemButtonList = new ArrayList<>();
        public int maxItemPage = 0;
        public int itemPage = 1;
        public List<CreativeModeTab> tabs = List.of(CreativeModeTabs.searchTab());
        public List<String> tabItems = CreativeModeTabs.searchTab().getDisplayItems().stream().map(stack -> stack.getItem().toString()).toList();
        public List<CopyTabButton> tabButtonList = new ArrayList<>();
        public int selectedTab = 0;
        public int maxTabPage = 0;
        public int tabPage = 1;

        public LocalPlayerResearchData() {}

        public void update() {
            this.tabs = getNeedShowTabs();
            this.maxTabPage = (int) Math.ceil(tabs.size()/(1.0*TAB_PER_COL));
            this.tabButtonList = new ArrayList<>();
            for (int i = 0; i+(tabPage-1)*TAB_PER_COL < tabs.size() && i < TAB_PER_COL ; i++) {
                tabButtonList.add(new CopyTabButton(i, tabs.get(i+(tabPage-1)*TAB_PER_COL), i+(tabPage-1)*TAB_PER_COL == selectedTab));
            }

            List<Pair<String, Integer>> tabResearches = researches.stream()
                    .filter(pair -> tabItems.contains(pair.getFirst()))
                    .toList();
            List<Pair<String, Integer>> searchResearches = tabResearches.stream()
                    .filter(pair -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(pair.getFirst())).getDefaultInstance().getHoverName().getString().contains(search))
                    .toList();
            this.maxItemPage = (int) Math.ceil(searchResearches.size()/(1.0*ITEM_PER_COPY_PAGE));
            if (this.itemPage > this.maxItemPage) this.itemPage = 1;
            this.itemButtonList = new ArrayList<>();
            for (int i = 0; i+(itemPage-1)*ITEM_PER_COPY_PAGE < searchResearches.size() && i < ITEM_PER_COPY_PAGE ; i++) {
                int xi = i % ITEM_PER_COPY_PAGE_ROW;
                int yi = i / ITEM_PER_COPY_PAGE_ROW;
                int index = i+(itemPage-1)*ITEM_PER_COPY_PAGE;
                itemButtonList.add(new CopyItemButton(xi, yi, BuiltInRegistries.ITEM.get(ResourceLocation.parse(searchResearches.get(index).getFirst())), searchResearches.get(index).getSecond()));
            }
        }

        public List<CreativeModeTab> getNeedShowTabs() {
            List<CreativeModeTab> newList = new ArrayList<>(List.of(CreativeModeTabs.searchTab()));
            List<CreativeModeTab> allTabs = CreativeModeTabs.allTabs().stream().filter(tab -> tab.getDisplayName() != CreativeModeTabs.searchTab().getDisplayName()).toList();
            newList.addAll(
                    allTabs.stream()
                            .filter(tab -> tab.getDisplayItems().stream().map(stack -> stack.getItem().toString()).anyMatch(id -> researches.stream().anyMatch(pair -> pair.getFirst().equals(id))))
                            .toList()
            );
            return newList;
        }

        public void switchTab(int i) {
            this.selectedTab = i+(this.tabPage-1)*TAB_PER_COL;
            this.tabItems = this.tabs.get(this.selectedTab).getDisplayItems().stream().map(stack -> stack.getItem().toString()).toList();
            this.itemPage = 1;
            update();
        }

        public void forwardItemPage(){
            itemPage = Math.min(itemPage+1, maxItemPage);
            update();
        }

        public void backwardItemPage(){
            itemPage = Math.max(itemPage-1, 1);
            update();
        }

        public void forwardTabPage(){
            tabPage = Math.min(tabPage+1, maxTabPage);
            update();
        }

        public void backwardTabPage(){
            tabPage = Math.max(tabPage-1, 1);
            update();
        }

        public void setSearch(String search){
            this.search = search;
        }
    }
}
