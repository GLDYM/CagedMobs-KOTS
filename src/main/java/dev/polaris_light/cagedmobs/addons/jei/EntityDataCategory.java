package dev.polaris_light.cagedmobs.addons.jei;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.registers.CagedItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class EntityDataCategory implements IRecipeCategory<EntityDataWrapper> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(CagedMobs.MODID, "entity");

    private static final int BG_PADDING = 5, BG_WIDTH = 166, BG_HEIGHT = 111;
    private static final int WIDTH = BG_WIDTH + BG_PADDING * 2;
    private static final int HEIGHT = BG_HEIGHT + BG_PADDING * 2;
    private final IDrawable icon;
    private final IRecipeType<EntityDataWrapper> type;
    private final Component title;

    public EntityDataCategory(IGuiHelper gui, IRecipeType<EntityDataWrapper> type){
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CagedItems.MOB_CAGE.get()));
        this.type = type;
        this.title = Component.translatable("jei.category.cagedmobs.entity");
    }

    @Override
    public IRecipeType<EntityDataWrapper> getRecipeType() {
        return this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EntityDataWrapper recipe, IFocusGroup focuses) {
        recipe.setRecipe(builder);
    }

    @Override
    public void draw(EntityDataWrapper recipe, IRecipeSlotsView view, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        recipe.draw(graphics);
    }
}
