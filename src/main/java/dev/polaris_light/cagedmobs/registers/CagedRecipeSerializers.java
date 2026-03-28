package dev.polaris_light.cagedmobs.registers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.serializers.environment.EnvironmentData;
import dev.polaris_light.cagedmobs.serializers.environment.EnvironmentDataSerializer;
import dev.polaris_light.cagedmobs.serializers.entity.AdditionalLootData;
import dev.polaris_light.cagedmobs.serializers.entity.AdditionalLootDataSerializer;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import dev.polaris_light.cagedmobs.serializers.entity.EntityDataSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> CAGED_RECIPE_SERIALIZERS_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CagedMobs.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EntityData>> ENTITY_RECIPE_SERIALIZER = CAGED_RECIPE_SERIALIZERS_REGISTER.register("entity_data", () -> new RecipeSerializer<>(EntityDataSerializer.CODEC, EntityDataSerializer.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnvironmentData>> ENVIRONMENT_RECIPE_SERIALIZER = CAGED_RECIPE_SERIALIZERS_REGISTER.register("environment_data", () -> new RecipeSerializer<>(EnvironmentDataSerializer.codec(), EnvironmentDataSerializer.streamCodec()));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AdditionalLootData>> ADDITIONAL_LOOT_RECIPE_SERIALIZER = CAGED_RECIPE_SERIALIZERS_REGISTER.register("additional_loot_data", () -> new RecipeSerializer<>(AdditionalLootDataSerializer.CODEC, AdditionalLootDataSerializer.STREAM_CODEC));

}
