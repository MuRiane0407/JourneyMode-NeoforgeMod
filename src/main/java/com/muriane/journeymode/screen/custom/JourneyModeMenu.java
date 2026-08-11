package com.muriane.journeymode.screen.custom;

import com.mojang.datafixers.util.Pair;
import com.muriane.journeymode.screen.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Map;

public class JourneyModeMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    public static final int CRAFT_SLOT_START = 1;
    public static final int CRAFT_SLOT_COUNT = 4;
    public static final int CRAFT_SLOT_END = 5;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_COUNT = 4;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final ResourceLocation BLOCK_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_shield");
    private static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS;
    private static final EquipmentSlot[] SLOT_IDS;
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer resultSlots = new ResultContainer();
    public final boolean active;
    private final Player owner;

    public static final int RESEARCH_SLOT = 46;
    private final ItemStackHandler container = new ItemStackHandler(1);

    public JourneyModeMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(inventory, true, inventory.player, containerId);
    }

    public JourneyModeMenu(Inventory playerInventory, boolean active, final Player owner, int containerId) {
        super(ModMenuTypes.JOURNEY_MODE_MENU.get(), containerId);
        this.active = active;
        this.owner = owner;
        this.addSlot(new ResultSlot(playerInventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));

        for(int i = 0; i < 2; ++i) {
            for(int j = 0; j < 2; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for(int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentslot = SLOT_IDS[k];
            ResourceLocation resourcelocation = (ResourceLocation)TEXTURE_EMPTY_SLOTS.get(equipmentslot);
            this.addSlot(createArmorSlot(playerInventory, owner, equipmentslot, 39 - k, 8, 8 + k * 18, resourcelocation));
        }

        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        this.addSlot(new Slot(playerInventory, 40, 77, 62) {
            public void setByPlayer(ItemStack p_270969_, ItemStack p_299918_) {
                owner.onEquipItem(EquipmentSlot.OFFHAND, p_299918_, p_270969_);
                super.setByPlayer(p_270969_, p_299918_);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(new SlotItemHandler(container, 0, -141, 164));
    }

    public static Slot createArmorSlot(Container container, LivingEntity owner, EquipmentSlot slot, int slotIndex, int x, int y, @Nullable ResourceLocation emptyIcon) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.inventory.ArmorSlot");
            Constructor<?> ctor = clazz.getDeclaredConstructor(
                    Container.class, LivingEntity.class, EquipmentSlot.class, int.class, int.class, int.class, ResourceLocation.class
            );
            ctor.setAccessible(true);
            return (Slot) ctor.newInstance(container, owner, slot, slotIndex, x, y, emptyIcon);
        } catch (Exception e) {
            return new Slot(container, slotIndex, x, y);  // 降级方案
        }
    }

    public static boolean isHotbarSlot(int index) {
        return index >= 36 && index < 45 || index == 45;
    }

    public void fillCraftSlotsStackedContents(StackedContents itemHelper) {
        this.craftSlots.fillStackedContents(itemHelper);
    }

    public void clearCraftingContent() {
        this.resultSlots.clearContent();
        this.craftSlots.clearContent();
    }

    public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe) {
        return ((CraftingRecipe)recipe.value()).matches(this.craftSlots.asCraftInput(), this.owner.level());
    }

    public void slotsChanged(Container inventory) {
        ExtendCraftingMenu.extendSlotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots, (RecipeHolder) null);
    }

    public void removed(Player player) {
        super.removed(player);
        this.resultSlots.clearContent();
        if (!player.level().isClientSide) {
            this.clearContainer(player, this.craftSlots);
            this.clearItemStackHandler(player, this.container);
        }
    }

    protected void clearItemStackHandler(Player player, ItemStackHandler container) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for (int j = 0; j < container.getSlots(); ++j) {
                player.drop(container.getStackInSlot(j), false);
            }
        } else {
            for (int i = 0; i < container.getSlots(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.getStackInSlot(i));
                }
            }
        }

    }

    public boolean stillValid(Player player) {
        return true;
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(itemstack);
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= 1 && index < 5) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 5 && index < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !((Slot)this.slots.get(8 - equipmentslot.getIndex())).hasItem()) {
                int i = 8 - equipmentslot.getIndex();
                if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 9 && index < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 36 && index < 45) {
                if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, itemstack);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
            if (index == 0) {
                player.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    public int getResultSlotIndex() {
        return 0;
    }

    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    public int getSize() {
        return 5;
    }

    public CraftingContainer getCraftSlots() {
        return this.craftSlots;
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int slotIndex) {
        return slotIndex != this.getResultSlotIndex();
    }

    static {
        TEXTURE_EMPTY_SLOTS = Map.of(EquipmentSlot.FEET, EMPTY_ARMOR_SLOT_BOOTS, EquipmentSlot.LEGS, EMPTY_ARMOR_SLOT_LEGGINGS, EquipmentSlot.CHEST, EMPTY_ARMOR_SLOT_CHESTPLATE, EquipmentSlot.HEAD, EMPTY_ARMOR_SLOT_HELMET);
        SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    }
}
