package dev.polaris_light.cagedmobs.serializers.environment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.serializers.SerializationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import java.util.List;

public final class EnvironmentDataSerializer {

    private static final MapCodec<EnvironmentData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(EnvironmentData::getInputItem),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("render").forGetter(EnvironmentData::getRenderBlock),
            com.mojang.serialization.Codec.FLOAT.fieldOf("growModifier").forGetter(EnvironmentData::getGrowModifier),
            com.mojang.serialization.Codec.list(com.mojang.serialization.Codec.STRING).fieldOf("categories").forGetter(EnvironmentData::getCategories)
        ).apply(instance, EnvironmentData::new)
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, EnvironmentData> STREAM_CODEC =
        StreamCodec.of(
            (buf, recipe) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInputItem());
                SerializationHelper.serializeBlock(buf, recipe.getRenderBlock());
                buf.writeFloat(recipe.getGrowModifier());
                SerializationHelper.serializeStringCollection(buf, recipe.getCategories());
            },
            buf -> {
                Ingredient inputItem = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                Block renderBlock = SerializationHelper.deserializeBlock(buf);
                float growModifier = buf.readFloat();
                List<String> categories = new ArrayList<>();
                SerializationHelper.deserializeStringCollection(buf, categories);
                return new EnvironmentData(inputItem, renderBlock, growModifier, categories);
            }
        );

    public static MapCodec<EnvironmentData> codec() {
        return CODEC;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, EnvironmentData> streamCodec() {
        return STREAM_CODEC;
    }

    private EnvironmentDataSerializer() {
    }
}
