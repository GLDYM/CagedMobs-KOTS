package dev.polaris_light.cagedmobs.client.blocks.mob_cage;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageContainer;
import dev.polaris_light.cagedmobs.client.helpers.EntityRendererHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Optional;

public class MobCageScreen extends AbstractContainerScreen<MobCageContainer> {

    private final Identifier GUI = Identifier.fromNamespaceAndPath(CagedMobs.MODID, "textures/gui/mob_cage.png");
    private final Identifier UPGRADE_SLOT_OUTLINE = Identifier.fromNamespaceAndPath(CagedMobs.MODID, "textures/gui/upgrade_slot.png");
    private final Identifier ENVIRONMENT_SLOT_OUTLINE = Identifier.fromNamespaceAndPath(CagedMobs.MODID, "textures/gui/environment_slot.png");
    private float rotation;

    public MobCageScreen(MobCageContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 183);
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos, topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        Slot envSlot = this.menu.getEnvironmentSlot();
        if (envSlot != null && !envSlot.hasItem()) {
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENVIRONMENT_SLOT_OUTLINE, leftPos + envSlot.x, topPos + envSlot.y, 0.0F, 0.0F, 16, 16, 16, 16);
        }

        int i = 1;
        for (Slot slot : this.menu.getUpgradeSlots()) {
            if (!slot.hasItem()) {
                pGuiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    UPGRADE_SLOT_OUTLINE,
                    leftPos + slot.x,
                    topPos + slot.y,
                    (float) (this.imageWidth + (16 * (i - 1))),
                    0.0F,
                    16,
                    16,
                    16,
                    16
                );
                i++;
            }
        }

        EntityType<?> type = menu.getEntityType();
        int color = menu.getColor();
        if (type != null) {
            Optional<Entity> entity;
            if (color != -1) {
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("Color", color);
                entity = EntityRendererHelper.createEntity(this.menu.player.level(), type, nbt);
            } else {
                entity = EntityRendererHelper.createEntity(this.menu.player.level(), type, null);
            }

            if (entity.isPresent()) {
                rotation = (rotation + 0.5f) % 360;
                int x0 = this.leftPos + 62;
                int y0 = this.topPos + 17;
                int x1 = this.leftPos + 114;
                int y1 = this.topPos + 87;
                pGuiGraphics.enableScissor(x0, y0, x1, y1);
                EntityRendererHelper.renderEntity(pGuiGraphics, x0, y0, x1, y1, rotation, entity.get());
                pGuiGraphics.disableScissor();
            }
        }
    }
}
