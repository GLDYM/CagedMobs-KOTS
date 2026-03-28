package dev.polaris_light.cagedmobs.client;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.items.CrystallizedExperienceBlockItem;
import dev.polaris_light.cagedmobs.items.CrystallizedExperienceItem;
import dev.polaris_light.cagedmobs.items.DnaSamplerItem;
import dev.polaris_light.cagedmobs.items.EmptySpawnEggItem;
import dev.polaris_light.cagedmobs.items.MobCageBlockItem;
import dev.polaris_light.cagedmobs.items.StarInfusedNetheriteBlockItem;
import dev.polaris_light.cagedmobs.items.StarInfusedNetheriteIngotItem;
import dev.polaris_light.cagedmobs.items.upgrades.UpgradeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CagedMobs.MODID, value = Dist.CLIENT)
public class ClientTooltipEvents {
    private ClientTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (item instanceof DnaSamplerItem) {
            DnaSamplerItem.appendSamplerTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof CrystallizedExperienceItem) {
            CrystallizedExperienceItem.appendTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof CrystallizedExperienceBlockItem) {
            CrystallizedExperienceBlockItem.appendTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof EmptySpawnEggItem) {
            EmptySpawnEggItem.appendTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof MobCageBlockItem) {
            MobCageBlockItem.appendTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof StarInfusedNetheriteBlockItem) {
            StarInfusedNetheriteBlockItem.appendTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof StarInfusedNetheriteIngotItem) {
            StarInfusedNetheriteIngotItem.appendTooltip(stack, event.getToolTip(), event.getFlags());
        } else if (item instanceof UpgradeItem) {
            ((UpgradeItem) item).appendEventTooltip(stack, event.getToolTip(), event.getFlags());
        }
    }
}
