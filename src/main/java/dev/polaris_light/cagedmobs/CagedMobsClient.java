package dev.polaris_light.cagedmobs;

import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageRenderer;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageScreen;
import dev.polaris_light.cagedmobs.registers.CagedBlockEntities;
import dev.polaris_light.cagedmobs.registers.CagedBlocks;
import dev.polaris_light.cagedmobs.registers.CagedContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CagedMobs.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CagedMobs.MODID, value = Dist.CLIENT)
public class CagedMobsClient {
    public CagedMobsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageRenderer::new);
            // ItemBlockRenderTypes.setRenderLayer(CagedBlocks.MOB_CAGE.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new);
    }
}
