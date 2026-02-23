package dev.polaris_light.cagedmobs.items;

import dev.polaris_light.cagedmobs.configs.CommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import javax.annotation.Nonnull;

public class EmptySpawnEggItem extends Item {
    public EmptySpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.cagedmobs.dna_sampler.getBackEntity").withStyle(ChatFormatting.GRAY));
        if(CommonConfig.disableSpawnEggs.get()){
            tooltipComponents.add(Component.translatable("item.cagedmobs.dna_sampler.disabled").withStyle(ChatFormatting.RED));
        }
    }
}
