package dev.polaris_light.cagedmobs.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import javax.annotation.Nonnull;

public abstract class UpgradeItem extends Item {
    public UpgradeItem(Properties properties) {
        super(properties);
    }

    public void appendEventTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.cagedmobs.upgrades.attach").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.cagedmobs.upgrades.stack").withStyle(ChatFormatting.GRAY));
    }
}
