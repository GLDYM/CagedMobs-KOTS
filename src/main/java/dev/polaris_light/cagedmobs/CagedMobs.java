package dev.polaris_light.cagedmobs;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import dev.polaris_light.cagedmobs.configs.ClientConfig;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.registers.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
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
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.Item.BLOCK, // capability to register for
            (level, pos, state, be, side) -> {
                if (be instanceof MobCageBlockEntity cage) {
                    return side == null ? cage.getInventoryResourceHandler() : cage.getRestrictedResourceHandler();
                }
                return null;
            },
            // blocks to register for
            CagedBlocks.MOB_CAGE.get(),
            CagedBlocks.HOPPING_MOB_CAGE.get()
        );
    }
}
