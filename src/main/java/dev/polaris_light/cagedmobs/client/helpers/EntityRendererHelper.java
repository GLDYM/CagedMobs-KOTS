package dev.polaris_light.cagedmobs.client.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

import static dev.polaris_light.cagedmobs.CagedMobs.LOGGER;

public class EntityRendererHelper {

    public static Optional<Entity> createEntity(Level level, EntityType<?> entityType, CompoundTag startingNbt){
        Optional<Entity> entity = Optional.empty();
        try {
            if (level != null && level.isClientSide()) {
                Entity created = entityType.create(level, EntitySpawnReason.LOAD);
                entity = Optional.ofNullable(created);
            }
        } catch (Exception e) {
            LOGGER.error("[CagedMobs] Rendering entity in GUI failed!", e);
        }
        return entity;
    }

    public static void renderEntity(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float rotation, Entity entity) {
        try {
            float scale = EntityRendererHelper.getScaleForEntityType(entity);
            float extraYOffset = (EntityRendererHelper.getOffsetForEntityType(entity) - 50.0F) / scale;
            Quaternionf bodyRotation = new Quaternionf().rotateZ((float) Math.PI).rotateY((float) Math.toRadians(rotation));
            EntityRenderState renderState = extractRenderState(entity);
            if (renderState instanceof LivingEntityRenderState livingRenderState) {
                livingRenderState.bodyRot = 180.0F + rotation;
                livingRenderState.yRot = rotation;
                if (livingRenderState.pose != Pose.FALL_FLYING) {
                    livingRenderState.xRot = 0.0F;
                }

                float currentScale = livingRenderState.scale;
                if (currentScale != 0.0F) {
                    livingRenderState.boundingBoxWidth = livingRenderState.boundingBoxWidth / currentScale;
                    livingRenderState.boundingBoxHeight = livingRenderState.boundingBoxHeight / currentScale;
                }

                livingRenderState.scale = 1.0F;
            }

            Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + extraYOffset, 0.0F);
            graphics.entity(renderState, scale, translation, bodyRotation, null, x0, y0, x1, y1);
        } catch (Exception e) {
            LOGGER.error("[CagedMobs] Rendering entity in the GUI failed!", e);
        }
    }

    private static EntityRenderState extractRenderState(Entity entity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super Entity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }

    public static float getScaleForEntityType(Entity entity){
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        float maxEntityDimension = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if(entity.getType().toString().contains("twilightforest.ur_ghast")){return 2.0F;}
        if(entity.getType().toString().contains("greekfantasy.charybdis")){return 3.0F;}
        if(entity.getType().toString().contains("twilightforest.hydra")){return 3.2F;}
        if(entity.getType().toString().contains("twilightforest.yeti_alpha")){return 6.0F;}
        if(entity.getType().toString().contains("minecraft.ender_dragon")) {return 5.0F;}
        if(entity.getType().toString().contains("twilightforest.armored_giant")) {return 6.0F;}
        if(entity.getType().toString().contains("twilightforest.giant_miner")) {return 6.0F;}
        if(entity.getType().toString().contains("twilightforest.mini_ghast")) {return 10.0F;}
        if(entity.getType().toString().contains("outvoted:kraken")) {return 2.0F;}
        if(entity.getType().toString().contains("mowziesmobs:frostmaw")) {return 10.0F;}
        if(entity.getType().toString().contains("alexsmobs:cachalot_whale")) {return 0.2F;}
        if(entity.getType().toString().contains("greekfantasy:giant_boar")) {return 10.0F;}
        if(entity.getType().toString().contains("alexsmobs:crocodile")) {return 6.0F;}
        if(entity.getType().toString().contains("alexsmobs:hammerhead_shark")) {return 6.0F;}
        if(entity.getType().toString().contains("upgrade_aquatic:great_thrasher")) {return 2.0F;}
        if(entity.getType().toString().contains("fireandice:cyclops")) {return 2.0F;}
        if(entity.getType().toString().contains("alexsmobs:cachalot_whale")) {return 0.5F;}
        if(entity.getType().toString().contains("alexsmobs:laviathan")) {return 0.5F;}
        if(entity.getType().toString().contains("alexsmobs:void_worm")) {return 1.0F;}
        if(entity instanceof ElderGuardian) {return 10.0F;}
        if(entity instanceof AbstractFish) {return 25.0F;}
        if(entity instanceof Ghast) {return 5.2F;}
        if(entity instanceof Sniffer) {return 16F;}
        if(maxEntityDimension >= 5.0F) { return 100F / maxEntityDimension; }
        if(width <= height){
            if(height >= 3){
                return 10.0f;
            }else if(height >= 2.5){
                return 15.0f;
            } else if(height >= 1.9){
                return 18.0F;
            }
        }
        return 20.0F;
    }

    public static int getOffsetForEntityType(Entity entity){
        if(entity instanceof Phantom ||
            entity instanceof AbstractFish ||
            entity instanceof EnderDragon ||
            entity instanceof Dolphin ||
            entity instanceof Guardian ||
            entity instanceof Turtle
        ){
            return 60;
        } else if(entity instanceof Ghast || entity instanceof Squid){
            return 65;
        }
        return 50;
    }

}
