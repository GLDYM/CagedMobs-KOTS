package dev.polaris_light.cagedmobs.addons.jade;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.impl.ui.ProgressElement;
import snownee.jade.impl.ui.SimpleProgressStyle;
import snownee.jade.impl.ui.TextElement;


import java.util.ArrayList;
import java.util.List;

public class CagedMobsComponentProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig pluginConfig) {
        if(!(blockAccessor.getBlockEntity() instanceof MobCageBlockEntity tile)){
            return;
        }
        SimpleProgressStyle progressStyle = new SimpleProgressStyle();
        // Add growth progress
        if(tile.hasEntity() && tile.hasEnvironment()){
            tooltip.add(new ProgressElement(
                tile.getGrowthPercentage(),
                Component.literal(String.format("%3.0f%%", tile.getGrowthPercentage() * 100)),
                progressStyle.color(0xff44AA44).textColor(0xffffff00),
                BoxStyle.GradientBorder.TRANSPARENT,
                true
            ));
        }
        // Add Environment
        if(tile.hasEnvironment()){
            ItemStack representation = tile.getEnvironmentItemStack();
            if(representation != null){
                tooltip.add(Component.translatable("JADE.tooltip.cagedmobs.cage.environment"));
                tooltip.add(List.of(
                        ItemStackElement.of(representation, 1.0F),
                        new TextElement(representation.getHoverName())));
            }
        }
        // Add Entity
        if(tile.hasEntity()){
            EntityType<?> representation = tile.getEntityType();
            if(representation != null){
                tooltip.add(Component.literal(
                        Component.translatable("JADE.tooltip.cagedmobs.cage.entity").withStyle(ChatFormatting.GRAY).getString() +
                                Component.translatable(representation.getDescriptionId()).withStyle(ChatFormatting.GRAY).getString()));
            }
        }
        // Add Upgrades
        if(tile.hasAnyUpgrades()){
            // Add Upgrade text
            tooltip.add(Component.translatable("TOP.tooltip.cagedmobs.cage.upgrades"));
            // Iterate through upgrades
            List<IElement> upgrades = new ArrayList<>();
            for(ItemStack upgrade : tile.getUpgradesAsItemStacks()){
                if(!upgrade.isEmpty()){
                    upgrades.add(ItemStackElement.of(upgrade));
                }
            }
            // Render a list of upgrades
            tooltip.add(upgrades);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(CagedMobs.MODID, "cagedmobs_jade");
    }
}
