package dev.polaris_light.cagedmobs.items;

import net.minecraft.ChatFormatting;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

import javax.annotation.Nonnull;

public class StarInfusedNetheriteBlockItem extends BlockItem {

    public StarInfusedNetheriteBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    public static void appendTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("block.cagedmobs.star_infused_netherite_block.beacon").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack itemStack) {
        return true;
    }
}
