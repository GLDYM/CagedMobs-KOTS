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

public class MobCageBlockItem extends BlockItem {
    public MobCageBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    public static void appendTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("block.cagedmobs.mob_cage.mainInfo").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("block.cagedmobs.mob_cage.rightClickHarvest").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("block.cagedmobs.mob_cage.envInfo").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("block.cagedmobs.mob_cage.upgrading").withStyle(ChatFormatting.GRAY));
    }
}
