package dev.polaris_light.cagedmobs.items.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import javax.annotation.Nonnull;

public class LightningUpgradeItem extends UpgradeItem{
    public LightningUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendEventTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendEventTooltip(stack, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.cagedmobs.lightning_upgrade.info").withStyle(ChatFormatting.GRAY));
    }
}
