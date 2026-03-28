package dev.polaris_light.cagedmobs.serializers.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public final class AdditionalLootDataSerializer {

    public static final MapCodec<AdditionalLootData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("entity").orElse("minecraft:pig").forGetter(AdditionalLootData::getEntityId),
            Codec.list(LootData.CODEC).fieldOf("results").orElse(new ArrayList<>()).forGetter(AdditionalLootData::getResults),
            Codec.BOOL.fieldOf("removeFromEntity").orElse(false).forGetter(AdditionalLootData::isRemoveFromEntity)
        ).apply(instance, AdditionalLootData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AdditionalLootData> STREAM_CODEC =
        StreamCodec.of(
            (buf, recipe) -> {
                buf.writeUtf(recipe.getEntityId());
                buf.writeInt(recipe.getResults().size());
                for (LootData data : recipe.getResults()) {
                    LootData.serializeBuffer(buf, data);
                }
                buf.writeBoolean(recipe.isRemoveFromEntity());
            },
            buf -> {
                String entityId = buf.readUtf();
                int length = buf.readInt();
                List<LootData> results = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    results.add(LootData.deserializeBuffer(buf));
                }
                boolean removeFromEntity = buf.readBoolean();
                return new AdditionalLootData(entityId, results, removeFromEntity);
            }
        );

    private AdditionalLootDataSerializer() {
    }
}
