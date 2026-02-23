package dev.polaris_light.cagedmobs.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class StarInfusedNetheriteNuggetItem extends Item {

    public StarInfusedNetheriteNuggetItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack itemStack) {
        return true;
    }
}
