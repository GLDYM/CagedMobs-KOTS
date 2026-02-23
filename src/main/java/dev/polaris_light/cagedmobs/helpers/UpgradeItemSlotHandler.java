package dev.polaris_light.cagedmobs.helpers;

import dev.polaris_light.cagedmobs.items.upgrades.UpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UpgradeItemSlotHandler extends SlotItemHandler {

    public UpgradeItemSlotHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack)
    {
        if (stack.isEmpty()){
            return false;
        }else{
            return stack.getItem() instanceof UpgradeItem;
        }
    }
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack){
        return 1;
    }
}
