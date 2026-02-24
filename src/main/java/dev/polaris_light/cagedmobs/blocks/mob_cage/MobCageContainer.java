package dev.polaris_light.cagedmobs.blocks.mob_cage;

import dev.polaris_light.cagedmobs.helpers.EnvironmentItemSlotHandler;
import dev.polaris_light.cagedmobs.helpers.UpgradeItemSlotHandler;
import dev.polaris_light.cagedmobs.items.upgrades.UpgradeItem;
import dev.polaris_light.cagedmobs.registers.CagedBlocks;
import dev.polaris_light.cagedmobs.registers.CagedContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;

import static dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity.*;

public class MobCageContainer extends AbstractContainerMenu {

    public final ContainerLevelAccess access;
    public final Player player;
    private final ContainerData data;
    private Slot environmentSlot = null;
    private final ArrayList<Slot> upgradeSlots = new ArrayList<>();

    public MobCageContainer(int windowId, Inventory inv, ContainerLevelAccess access, ItemStackHandler handler, ContainerData data) {
        super(CagedContainers.CAGE_CONTAINER.get(), windowId);
        this.access = access;
        this.player = inv.player;
        this.data = data;
        this.addDataSlots(data);
        initSlots(handler);
    }

    public MobCageContainer(int windowId, Inventory inv) {
        this(windowId, inv, ContainerLevelAccess.NULL, new ItemStackHandler(SLOT_COUNT), new SimpleContainerData(2));
    }

    private void initSlots(ItemStackHandler handler) {
        if(handler != null) {
            this.environmentSlot = addSlot(new EnvironmentItemSlotHandler(handler, ENVIRONMENT_SLOT, 26, 44));
            upgradeSlots.add(addSlot(new UpgradeItemSlotHandler(handler, ENVIRONMENT_SLOT + 1, 134, 23)));
            upgradeSlots.add(addSlot(new UpgradeItemSlotHandler(handler, ENVIRONMENT_SLOT + 2, 134, 44)));
            upgradeSlots.add(addSlot(new UpgradeItemSlotHandler(handler, ENVIRONMENT_SLOT + 3, 134, 65)));
        }
        // } else {
        //     // Why?
        //     SimpleContainer dummy = new SimpleContainer(SLOT_COUNT);
        //     this.environmentSlot = addSlot(new Slot(dummy, ENVIRONMENT_SLOT, 26, 44));
        //     upgradeSlots.add(addSlot(new Slot(dummy, ENVIRONMENT_SLOT + 1, 134, 23)));
        //     upgradeSlots.add(addSlot(new Slot(dummy, ENVIRONMENT_SLOT + 2, 134, 44)));
        //     upgradeSlots.add(addSlot(new Slot(dummy, ENVIRONMENT_SLOT + 3, 134, 65)));
        // }

        layoutPlayerInventorySlots(player.getInventory(), 8, 101);
    }


    private void layoutPlayerInventorySlots(Inventory inventory, int leftCol, int topRow) {
        // Player inventory
        addSlotBox(inventory, 9, leftCol, topRow, 9, 18, 3, 18);
        // Player Hot-bar
        topRow += 58;
        addSlotRange(inventory, 0, leftCol, topRow, 9, 18);
    }

    /**
     * Adds a box of inventory slots.
     * Written by McJty (https://www.mcjty.eu/docs/1.20/).
     */
    private int addSlotBox(Container playerInventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(playerInventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    /**
     * Adds a range of inventory slots.
     * Written by McJty (https://www.mcjty.eu/docs/1.20/).
     */
    private int addSlotRange(Container playerInventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new Slot(playerInventory, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    /**
     * Used for logic of shift-clicking items.
     * @param pPlayer player accessing the block
     * @param pIndex slot index
     * @return item stack
     */
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();
            // Get items from block back to the player inventory
            if (pIndex < SLOT_COUNT) {
                if (!this.moveItemStackTo(slotItem, SLOT_COUNT, Inventory.INVENTORY_SIZE + SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotItem.getItem() instanceof UpgradeItem) {
                for(int i = ENVIRONMENT_SLOT+1; i < SLOT_COUNT; i++){
                    if (!this.slots.get(i).hasItem() && this.slots.get(i).mayPlace(slotItem)) {
                        ItemStack itemstack2 = slotItem.copyWithCount(1);
                        slotItem.shrink(1);
                        this.slots.get(i).setByPlayer(itemstack2);
                    }
                }
            } else if(existsEnvironmentFromItemStack(slotItem)){
                if (!this.slots.get(ENVIRONMENT_SLOT).hasItem() && this.slots.get(ENVIRONMENT_SLOT).mayPlace(slotItem)) {
                    ItemStack itemstack2 = slotItem.copyWithCount(1);
                    slotItem.shrink(1);
                    this.slots.get(ENVIRONMENT_SLOT).setByPlayer(itemstack2);
                }
            }
            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (slotItem.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(pPlayer, slotItem);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return AbstractContainerMenu.stillValid(this.access, pPlayer, CagedBlocks.MOB_CAGE.get()) 
            || AbstractContainerMenu.stillValid(this.access, pPlayer, CagedBlocks.HOPPING_MOB_CAGE.get());
    }

    public Slot getEnvironmentSlot() {
        return environmentSlot;
    }

    public ArrayList<Slot> getUpgradeSlots() {
        return upgradeSlots;
    }

    public int getEntityTypeId() {
        return data.get(0);
    }

    public EntityType<?> getEntityType() {
        int id = getEntityTypeId();
        return id >= 0 ? BuiltInRegistries.ENTITY_TYPE.byId(id) : null;
    }

    public int getColor() {
        return data.get(1);
    }
}
