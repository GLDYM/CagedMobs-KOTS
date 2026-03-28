package dev.polaris_light.cagedmobs.client;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.client.blocks.mob_cage.MobCageRenderer;
import dev.polaris_light.cagedmobs.client.blocks.mob_cage.MobCageScreen;
import dev.polaris_light.cagedmobs.registers.CagedBlockEntities;
import dev.polaris_light.cagedmobs.registers.CagedContainers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageRenderer::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CagedContainers.CAGE_CONTAINER.get(), MobCageScreen::new);
    }
}
