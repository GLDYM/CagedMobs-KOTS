package dev.polaris_light.cagedmobs.serializers.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.polaris_light.cagedmobs.serializers.SerializationHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;


import java.util.*;

public class EntityDataSerializer implements RecipeSerializer<EntityData> {

    public static final MapCodec<EntityData> CODEC = RecordCodecBuilder.mapCodec(entityDataInstance ->
        entityDataInstance.group(
            Codec.STRING.fieldOf("entity").forGetter(EntityData::getEntityId),
            Codec.list(Codec.STRING).fieldOf("environments").orElse(List.of()).forGetter(EntityData::getEnvironments),
            Codec.INT.flatXmap(
                ticks -> ticks > 0 ? DataResult.success(ticks) : DataResult.error(() -> "growTicks must be > 0"),
                DataResult::success
            ).fieldOf("growTicks").forGetter(EntityData::getTotalGrowTicks),
            Codec.BOOL.fieldOf("requiresWater").orElse(false).forGetter(EntityData::ifRequiresWater),
            Codec.list(LootData.CODEC).fieldOf("results").forGetter(EntityData::getResults),
            Codec.INT.flatXmap(
                tier -> (tier >= 1 && tier <= 3) ? DataResult.success(tier) : DataResult.error(() -> "tier must be 1-3"),
                DataResult::success
            ).fieldOf("samplerTier").forGetter(EntityData::getSamplerTier)
        ).apply(entityDataInstance, EntityData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityData> STREAM_CODEC =
        StreamCodec.of(
            (buf, recipe) -> {
                buf.writeUtf(recipe.getEntityId());
                SerializationHelper.serializeStringCollection(buf, recipe.getEnvironments());
                buf.writeInt(recipe.getTotalGrowTicks());
                buf.writeBoolean(recipe.ifRequiresWater());
                buf.writeInt(recipe.getResults().size());
                for (LootData data : recipe.getResults()) {
                    LootData.serializeBuffer(buf, data);
                }
                buf.writeInt(recipe.getSamplerTier());
            },
            buf -> {
                String entityId = buf.readUtf();
                List<String> envs = new ArrayList<>();
                SerializationHelper.deserializeStringCollection(buf, envs);
                int growTicks = buf.readInt();
                boolean requiresWater = buf.readBoolean();
                int length = buf.readInt();
                List<LootData> results = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    results.add(LootData.deserializeBuffer(buf));
                }
                int tier = buf.readInt();
                return new EntityData(entityId, envs, growTicks, requiresWater, results, tier);
            }
        );

    @Override
    public MapCodec<EntityData> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EntityData> streamCodec() {
        return STREAM_CODEC;
    }
}
