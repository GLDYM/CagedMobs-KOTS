package dev.polaris_light.cagedmobs.serializers.environment;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.registers.CagedRecipeSerializers;
import dev.polaris_light.cagedmobs.registers.CagedRecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;


import java.util.List;

public class EnvironmentData implements Recipe<RecipeInput> {

    private Ingredient inputItem;
    private Block renderBlock;
    private float growModifier;
    private List<String> categories;

    public EnvironmentData(Ingredient item, Block renderBlock, float growModifier, List<String> categories){
        this.inputItem = item;
        this.renderBlock = renderBlock;
        this.growModifier = growModifier;
        this.categories = categories;
        // Add the id to the list of loaded recipes
        if (CagedMobs.LOGGER != null && CommonConfig.debug.get()) {
            item.items().findFirst().ifPresent(holder -> CagedMobs.LOGGER.info("Loaded EnvironmentData recipe for input item: " + BuiltInRegistries.ITEM.getKey(holder.value())));
        }
    }

    @Override
    public boolean matches(RecipeInput input, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return CagedRecipeSerializers.ENVIRONMENT_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return CagedRecipeTypes.ENVIRONMENT_RECIPE.get();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public Ingredient getInputItem() {
        return this.inputItem;
    }

    public Block getRenderBlock(){
        return this.renderBlock;
    }

    public float getGrowModifier() {
        return this.growModifier;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setGrowthModifier(float modifier) {
        this.growModifier = modifier;
    }

    public void setRenderBlock(Block state) {
        this.renderBlock = state;
    }

    public void setInputItem(Ingredient item) {
        this.inputItem = item;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
