package dev.polaris_light.cagedmobs.client.blocks.mob_cage;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import dev.polaris_light.cagedmobs.configs.ClientConfig;
import dev.polaris_light.cagedmobs.registers.CagedItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MobCageRenderer implements BlockEntityRenderer<MobCageBlockEntity, MobCageRenderer.MobCageRenderState> {

    private final EntityRenderDispatcher entityRenderer;

    public static class MobCageRenderState extends BlockEntityRenderState {
        public @Nullable EntityRenderState displayEntity;
        public float scale = 1.0F;
        public double zOffset = 0.0D;
    }

    public MobCageRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public MobCageRenderState createRenderState() {
        return new MobCageRenderState();
    }

    @Override
    public void extractRenderState(
        MobCageBlockEntity blockEntity,
        MobCageRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.displayEntity = null;

        if (blockEntity.getLevel() == null || !blockEntity.hasEntity() || ClientConfig.disableEntitiesRender.get()) {
            return;
        }

        Entity entity = blockEntity.getCachedEntity(blockEntity.getLevel());
        if (entity == null) {
            return;
        }

        if (entity instanceof Sheep sheep) {
            sheep.setColor(DyeColor.byId(blockEntity.getColor()));
        }

        EntityRenderState extracted = this.entityRenderer.extractEntity(entity, partialTicks);
        extracted.lightCoords = state.lightCoords;
        state.displayEntity = extracted;

        float maxSize = getEntitySize(entity);
        if (entity instanceof Sniffer) {
            maxSize *= 0.6F;
        } else {
            maxSize *= 0.8F;
        }
        float maxEntityDimension = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (maxEntityDimension > 1.0F) {
            maxSize /= maxEntityDimension;
        }

        if (!ClientConfig.disableGrowthRender.get()) {
            float growthPercentage = blockEntity.getGrowthPercentage() * maxSize;
            if (blockEntity.hasUpgrades(CagedItems.CREATIVE_UPGRADE.get(), 1)) {
                growthPercentage = maxSize;
            }
            state.scale = growthPercentage;
        } else {
            state.scale = maxSize;
        }

        state.zOffset = getEntityZ(entity);
    }

    @Override
    public void submit(
        MobCageRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera
    ) {
        if (state.displayEntity == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D + state.zOffset);
        poseStack.translate(0.0D, 0.1D, 0.0D);
        poseStack.scale(state.scale, state.scale, state.scale);
        this.entityRenderer.submit(state.displayEntity, camera, 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    private static double getEntityZ(Entity entity) {
        if (entity instanceof Dolphin) {
            return 0.25D;
        }
        if (entity instanceof ElderGuardian) {
            return 1.2D;
        }
        if (entity instanceof Guardian) {
            return 0.7D;
        }
        return 0.0D;
    }

    private static float getEntitySize(Entity entity) {
        if (
            entity instanceof Dolphin
                || entity instanceof Squid
                || entity instanceof Turtle
        ) {
            return 0.32F;
        }
        if (entity instanceof Guardian || entity instanceof Phantom) {
            return 0.25F;
        }
        if (entity instanceof EnderDragon) {
            return 0.8F;
        }
        return 0.5F;
    }
}
