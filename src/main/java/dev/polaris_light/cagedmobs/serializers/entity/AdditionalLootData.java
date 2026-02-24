package dev.polaris_light.cagedmobs.serializers.entity;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.registers.CagedRecipeSerializers;
import dev.polaris_light.cagedmobs.registers.CagedRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeConfig.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdditionalLootData implements Recipe<RecipeInput> {

    private String entityId;
    private EntityType<?> entityType;
    private ArrayList<LootData> results;
    private boolean removeFromEntity;

    public AdditionalLootData(String entityId, List<LootData> results, boolean removeFromEntity){
        this.entityId = entityId;
        this.entityType = this.getEntityType();
        this.results = new ArrayList<>(results);
        this.removeFromEntity = removeFromEntity;
        // Add the id to the list of loaded recipes
        if(!entityId.isEmpty() && CagedMobs.LOGGER != null && CommonConfig.debug.get()){
            CagedMobs.LOGGER.info("Loaded AdditionalLootData recipe for entity: " + entityId);
        }
    }

    @Override
    public boolean matches(RecipeInput input, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public void setEntityId(String entityId){
        this.entityId = entityId;
        // Change entity type
        Optional<EntityType<?>> entityType = EntityType.byString(this.entityId);
        entityType.ifPresent(type -> this.entityType = type);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CagedRecipeSerializers.ADDITIONAL_LOOT_RECIPE_SERIALIZER.get();
    }

    public EntityType<?> getEntityType(){
        // Try to find again entity type if it's null
        if(this.entityType == null){
            Optional<EntityType<?>> entityType = EntityType.byString(this.entityId);
            if(entityType.isPresent()) {
                this.entityType = entityType.get();
                return entityType.get();
            }
        }
        return this.entityType;
    }

    @Override
    public RecipeType<?> getType() {
        return CagedRecipeTypes.ADDITIONAL_LOOT_RECIPE.get();
    }

    public ArrayList<LootData> getResults() {
        return this.results;
    }

    public void setResults(ArrayList<LootData> results) {
        this.results = results;
    }

    public boolean isRemoveFromEntity(){
        return this.removeFromEntity;
    }

    public void setRemoveFromEntity(boolean removeFromEntity){
        this.removeFromEntity = removeFromEntity;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

}
