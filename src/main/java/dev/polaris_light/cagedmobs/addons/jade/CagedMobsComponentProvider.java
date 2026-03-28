package dev.polaris_light.cagedmobs.addons.jade;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.view.ProgressView;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.impl.ui.ProgressElement;
import snownee.jade.impl.ui.SimpleProgressStyle;

import java.util.ArrayList;
import java.util.List;

public class CagedMobsComponentProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig pluginConfig) {
        if (!(blockAccessor.getBlockEntity() instanceof MobCageBlockEntity tile)) {
            return;
        }

        if (tile.hasEntity() && tile.hasEnvironment()) {
            float progress = tile.getGrowthPercentage();
            var progressStyle = new SimpleProgressStyle();
            progressStyle.color = 0xFF44AA44;
            var progressView = new ProgressView(
                ProgressView.Part.of(progress, 0xFF44AA44),
                Component.literal(String.format("%3.0f%%", progress * 100)).withStyle(ChatFormatting.YELLOW),
                progressStyle,
                BoxStyle.transparent()
            );
            tooltip.add(new ProgressElement(progressView));
        }

        if (tile.hasEnvironment()) {
            ItemStack representation = tile.getEnvironmentItemStack();
            if (!representation.isEmpty()) {
                tooltip.add(Component.translatable("JADE.tooltip.cagedmobs.cage.environment"));
                tooltip.add(List.of(ItemStackElement.of(representation, 1.0F)));
                tooltip.add(representation.getHoverName().copy().withStyle(ChatFormatting.GRAY));
            }
        }

        if (tile.hasEntity()) {
            EntityType<?> representation = tile.getEntityType();
            if (representation != null) {
                tooltip.add(Component.translatable("JADE.tooltip.cagedmobs.cage.entity").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(representation.getDescriptionId()).withStyle(ChatFormatting.GRAY));
            }
        }

        if (tile.hasAnyUpgrades()) {
            tooltip.add(Component.translatable("TOP.tooltip.cagedmobs.cage.upgrades"));
            List<ItemStackElement> upgrades = new ArrayList<>();
            for (ItemStack upgrade : tile.getUpgradesAsItemStacks()) {
                if (!upgrade.isEmpty()) {
                    upgrades.add(ItemStackElement.of(upgrade));
                }
            }
            tooltip.add(upgrades);
        }
    }

    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath(CagedMobs.MODID, "cagedmobs_jade");
    }
}
