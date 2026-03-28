package dev.polaris_light.cagedmobs.helpers;

import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class EnvironmentItemSlotHandler extends ResourceHandlerSlot {

    public EnvironmentItemSlotHandler(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, itemHandler::set, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        if (stack.isEmpty()){
            return false;
        }else{
            return MobCageBlockEntity.existsEnvironmentFromItemStack(stack);
        }
    }
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack){
        return 1;
    }
}
