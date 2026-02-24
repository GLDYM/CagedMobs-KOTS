package dev.polaris_light.cagedmobs;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import dev.polaris_light.cagedmobs.configs.ClientConfig;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.items.DnaSamplerDiamondItem;
import dev.polaris_light.cagedmobs.items.DnaSamplerItem;
import dev.polaris_light.cagedmobs.items.DnaSamplerNetheriteItem;
import dev.polaris_light.cagedmobs.registers.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;


@Mod(CagedMobs.MODID)
public class CagedMobs {
    public static final String MODID = "cagedmobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CagedMobs(IEventBus modEventBus, ModContainer modContainer) {
        // Configs
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        CagedBlocks.CAGED_BLOCKS_REGISTER.register(modEventBus);
        CagedItems.CAGED_ITEMS_REGISTER.register(modEventBus);
        CagedBlockEntities.CAGED_BLOCK_ENTITIES_REGISTER.register(modEventBus);
        CagedCreativeTabs.CAGED_CREATIVE_TABS_REGISTER.register(modEventBus);
        CagedRecipeTypes.CAGED_RECIPE_TYPES_REGISTER.register(modEventBus);
        CagedRecipeSerializers.CAGED_RECIPE_SERIALIZERS_REGISTER.register(modEventBus);
        CagedContainers.CAGED_MENU_TYPES_REGISTER.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::addPropertiesToItems);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.ItemHandler.BLOCK, // capability to register for
            (level, pos, state, be, side) -> {
                if (be instanceof MobCageBlockEntity cage) {
                    return side == null ? cage.getInventoryHandler() : cage.getRestrictedHandler();
                }
                return null;
            },
            // blocks to register for
            CagedBlocks.MOB_CAGE.get(),
            CagedBlocks.HOPPING_MOB_CAGE.get()
        );
    }

    private void addPropertiesToItems(final FMLClientSetupEvent event) {
        ItemProperties.register(CagedItems.DNA_SAMPLER.get(), ResourceLocation.parse("cagedmobs:full"), (itemStack, clientWorld, livingEntity, unusedInt) -> DnaSamplerItem.containsEntityType(itemStack) ? 1.0F : 0.0F);
        ItemProperties.register(CagedItems.DIAMOND_DNA_SAMPLER.get(), ResourceLocation.parse("cagedmobs:full"), (itemStack, clientWorld, livingEntity, unusedInt) -> DnaSamplerDiamondItem.containsEntityType(itemStack) ? 1.0F : 0.0F);
        ItemProperties.register(CagedItems.NETHERITE_DNA_SAMPLER.get(), ResourceLocation.parse("cagedmobs:full"), (itemStack, clientWorld, livingEntity, unusedInt) -> DnaSamplerNetheriteItem.containsEntityType(itemStack) ? 1.0F : 0.0F);
    }
}
