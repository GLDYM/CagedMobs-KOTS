package dev.polaris_light.cagedmobs.serializers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.registers.CagedRecipeTypes;
import dev.polaris_light.cagedmobs.serializers.environment.EnvironmentData;
import dev.polaris_light.cagedmobs.serializers.entity.AdditionalLootData;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class RecipesHelper {

    // Some helper functions
    public static List<EntityData> getEntitiesRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.recipeMap().byType(CagedRecipeTypes.ENTITY_RECIPE.get()).stream().map(holder -> holder.value()).toList();
        }
        return Collections.emptyList();
    }

    public static List<EnvironmentData> getEnvironmentRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.recipeMap().byType(CagedRecipeTypes.ENVIRONMENT_RECIPE.get()).stream().map(holder -> holder.value()).toList();
        }
        return Collections.emptyList();
    }

    public static List<AdditionalLootData> getAdditionalLootRecipesList() {
        RecipeManager manager = getRecipeManager();
        if (manager != null) {
            return manager.recipeMap().byType(CagedRecipeTypes.ADDITIONAL_LOOT_RECIPE.get()).stream().map(holder -> holder.value()).toList();
        }
        return Collections.emptyList();
    }

    public static RecipeManager getRecipeManager(){
        try{
            return RecipesHelper.getRecipeManagerServer();
        }catch(final Exception e){
            throw new RuntimeException(e);
        }
    }

    private static RecipeManager getRecipeManagerServer() {
        return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
    }

    public static boolean isEnvValidForEntity(EntityData entity, EnvironmentData env) {
        for(String s : entity.getEnvironments()){
            for(String s2 : env.getCategories()){
                if(s.matches(s2)){
                    return true;
                }
            }
        }
        return false;
    }

    public static List<EntityType<?>> getEntityTypesFromConfigList(){
        List<EntityType<?>> blacklisted = new java.util.ArrayList<>(Collections.emptyList());
        List<? extends String> blacklistedEntities = CommonConfig.entitiesList.get();
        for(String s : blacklistedEntities){
            Optional<EntityType<?>> entityType = EntityType.byString(s);
            entityType.ifPresent(blacklisted::add);
        }
        return blacklisted;
    }

    public static boolean isEntityTypeBlacklisted(EntityType<?> type){
        List<EntityType<?>> list = getEntityTypesFromConfigList();
        if(CommonConfig.entitiesListInWhitelistMode.get()){
            return !list.contains(type);
        }else{
            return list.contains(type);
        }
    }

    public static List<Item> getItemsFromConfigList(){
        List<Item> blacklisted = new java.util.ArrayList<>(Collections.emptyList());
        List<? extends String> blacklistedItems = CommonConfig.itemsList.get();
        for(String s : blacklistedItems){
            Item i = BuiltInRegistries.ITEM.getValue(Identifier.parse(s));
            if(i != Items.AIR && i != null){
                blacklisted.add(i);
            }
        }
        return blacklisted;
    }
}
