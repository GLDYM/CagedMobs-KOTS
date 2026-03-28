package dev.polaris_light.cagedmobs.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NetherStarFragmentItem extends Item {

    public NetherStarFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}
