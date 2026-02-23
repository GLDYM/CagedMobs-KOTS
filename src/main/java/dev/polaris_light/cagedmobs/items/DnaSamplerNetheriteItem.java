package dev.polaris_light.cagedmobs.items;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class DnaSamplerNetheriteItem extends DnaSamplerItem{
    public DnaSamplerNetheriteItem(Properties properties) {
        super(properties);
    }

    public boolean isFoil(@Nonnull ItemStack itemStack) {
        return true;
    }
}
