package dev.polaris_light.cagedmobs.registers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.serializers.environment.EnvironmentData;
import dev.polaris_light.cagedmobs.serializers.environment.RecipeTypeEnvironmentData;
import dev.polaris_light.cagedmobs.serializers.entity.AdditionalLootData;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import dev.polaris_light.cagedmobs.serializers.entity.RecipeAdditionalLoot;
import dev.polaris_light.cagedmobs.serializers.entity.RecipeTypeEntityData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> CAGED_RECIPE_TYPES_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, CagedMobs.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EntityData>> ENTITY_RECIPE = CAGED_RECIPE_TYPES_REGISTER.register("entity_data", RecipeTypeEntityData::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<EnvironmentData>> ENVIRONMENT_RECIPE = CAGED_RECIPE_TYPES_REGISTER.register("environment_data", RecipeTypeEnvironmentData::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<AdditionalLootData>> ADDITIONAL_LOOT_RECIPE = CAGED_RECIPE_TYPES_REGISTER.register("additional_loot_data", RecipeAdditionalLoot::new);
}
