package dev.polaris_light.cagedmobs.addons.jei;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.items.DnaSamplerItem;
import dev.polaris_light.cagedmobs.registers.CagedItems;
import dev.polaris_light.cagedmobs.serializers.RecipesHelper;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

@JeiPlugin
public class CagedMobsPlugin implements IModPlugin {

    public static final RecipeType<EntityDataWrapper> ENTITY_RECIPE = RecipeType.create(CagedMobs.MODID, "entity", EntityDataWrapper.class);

    @Override
    public ResourceLocation getPluginUid () {
        return ResourceLocation.fromNamespaceAndPath(CagedMobs.MODID, "jei");
    }


    @Override
    public void registerRecipeCatalysts (IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(CagedItems.MOB_CAGE.get()), ENTITY_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(CagedItems.HOPPING_MOB_CAGE.get()), ENTITY_RECIPE);
    }

    @Override
    public void registerRecipes (IRecipeRegistration registration) {
        final List<EntityData> entities = new ArrayList<>(RecipesHelper.getEntitiesRecipesList());
        // Subtract the blacklisted entities
        List<EntityType<?>> blacklistedEntities = RecipesHelper.getEntityTypesFromConfigList();
        if(!CommonConfig.entitiesListInWhitelistMode.get()) {
            // Remove blacklisted entities
            entities.removeIf(data -> blacklistedEntities.contains(data.getEntityType()));
        }else{
            // Remove all except whitelisted entities
            entities.removeIf(data -> !blacklistedEntities.contains(data.getEntityType()));
        }
        // Create JEI recipes
        registration.addRecipes(ENTITY_RECIPE, entities.stream().map(EntityDataWrapper::new).collect(Collectors.toList()));
    }

    @Override
    public void registerCategories (IRecipeCategoryRegistration registration) {
        final IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new EntityDataCategory(guiHelper, ENTITY_RECIPE));
    }

    // Subtype interpreter for samplers

    private static class DnaSamplerSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
        @Override
        @Nullable
        public Object getSubtypeData(ItemStack ingredient, UidContext context) {
            if (ingredient.getItem() instanceof DnaSamplerItem sampler) {
                EntityType<?> entityType = sampler.getEntityType(ingredient);
                if (entityType != null) {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                }
            }
            return null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            if (ingredient.getItem() instanceof DnaSamplerItem sampler) {
                EntityType<?> entityType = sampler.getEntityType(ingredient);
                if (entityType != null) {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
                }
            }
            return "";
        }
    }


    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        DnaSamplerSubtypeInterpreter interpreter = new DnaSamplerSubtypeInterpreter();
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, CagedItems.NETHERITE_DNA_SAMPLER.get(), interpreter);
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, CagedItems.DIAMOND_DNA_SAMPLER.get(), interpreter);
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, CagedItems.DNA_SAMPLER.get(), interpreter);
    }

}