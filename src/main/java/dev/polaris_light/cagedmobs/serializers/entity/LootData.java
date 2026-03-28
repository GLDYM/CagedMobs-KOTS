package dev.polaris_light.cagedmobs.serializers.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.polaris_light.cagedmobs.CagedMobs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class LootData {

    public static final Codec<LootData> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Ingredient.CODEC.fieldOf("output").forGetter(LootData::getItem),
            Ingredient.CODEC.optionalFieldOf("output_cooked").forGetter(LootData::getOptionalCookedItem),
            Codec.FLOAT.fieldOf("chance").forGetter(LootData::getChance),
            Codec.INT.fieldOf("minAmount").forGetter(LootData::getMinAmount),
            Codec.INT.fieldOf("maxAmount").forGetter(LootData::getMaxAmount),
            Codec.BOOL.fieldOf("lighting").orElse(false).forGetter(LootData::isLighting),
            Codec.BOOL.fieldOf("needsArrow").orElse(false).forGetter(LootData::isArrow),
            Codec.INT.fieldOf("color").orElse(-1).forGetter(LootData::getColor),
            Codec.BOOL.fieldOf("randomDurability").orElse(false).forGetter(LootData::ifRandomDurability),
            Codec.STRING.fieldOf("components").orElse("").forGetter(LootData::getComponents)
    ).apply(builder, LootData::new));

    private final float chance;
    private Ingredient item;
    private Ingredient cookedItem;
    private final int minAmount;
    private final int maxAmount;
    private final boolean lighting;
    private final boolean arrow;
    private final int color;
    private final boolean randomDurability;
    private final String components;

    public LootData(Ingredient item, Optional<Ingredient> cookedItem, float chance, int min, int max, boolean lighting, boolean arrow, int color, boolean randomDurability, String components) {
        this.chance = chance;
        this.item = item;
        this.cookedItem = cookedItem.orElse(null);
        this.minAmount = min;
        this.maxAmount = max;
        this.lighting = lighting;
        this.arrow = arrow;
        this.color = color;
        this.randomDurability = randomDurability;
        this.components = components;
        // Check for errors
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Amounts must not be negative!");
        }
        if (min > max) {
            throw new IllegalArgumentException("Min amount must not be greater than max amount!");
        }
    }

    public static void serializeBuffer(RegistryFriendlyByteBuf buffer, LootData lootData) {
        buffer.writeFloat(lootData.getChance());

        ItemStack item = lootData.getItemStack();
        if (!item.isEmpty()) {
            buffer.writeBoolean(true);
            ItemStack.STREAM_CODEC.encode(buffer, item);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeInt(lootData.getMinAmount());
        buffer.writeInt(lootData.getMaxAmount());
        buffer.writeBoolean(lootData.isLighting());
        buffer.writeBoolean(lootData.isArrow());

        ItemStack cooked = lootData.getCookedItemStack();
        if (!cooked.isEmpty()) {
            buffer.writeBoolean(true);
            ItemStack.STREAM_CODEC.encode(buffer, cooked);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeInt(lootData.getColor());
        buffer.writeBoolean(lootData.ifRandomDurability());
        buffer.writeUtf(lootData.getComponents());
    }


    public static LootData deserializeBuffer(RegistryFriendlyByteBuf buffer) {
        final float chance = buffer.readFloat();

        Ingredient item;
        if (buffer.readBoolean()) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            item = DataComponentIngredient.of(false, stack);
        } else {
            throw new IllegalStateException("LootData primary output ingredient is missing from network payload");
        }

        final int min = buffer.readInt();
        final int max = buffer.readInt();
        final boolean isLightning = buffer.readBoolean();
        final boolean isArrow = buffer.readBoolean();

        Ingredient cookedItem;
        if (buffer.readBoolean()) {
            ItemStack cookedStack = ItemStack.STREAM_CODEC.decode(buffer);
            cookedItem = DataComponentIngredient.of(false, cookedStack);
        } else {
            cookedItem = null;
        }

        final int color = buffer.readInt();
        final boolean randomDurability = buffer.readBoolean();
        final String components = buffer.readUtf();

        return new LootData(item, Optional.ofNullable(cookedItem), chance, min, max, isLightning, isArrow, color, randomDurability, components);
    }


    /**
     * Writes Components data to an item stack
     * @param components the data for the Components variable
     * @param stack the item stack to write to
     * @return the updated item stack reference
     */
    public static ItemStack writeComponents(String components, ItemStack stack) {
        ItemStack copy = stack.copy();
        RegistryAccess registryAccess = getComponentRegistryAccess();
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

        JsonObject json = JsonParser.parseString(components).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            try {
                Identifier id = Identifier.parse(entry.getKey());
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id);

                if (type == null) {
                    throw new IllegalArgumentException("Unknown component key: " + entry.getKey());
                }

                // Use the component's codec to parse the JSON value
                Object value = type.codec().parse(registryOps, entry.getValue())
                    .getOrThrow(msg -> { throw new RuntimeException(msg); });

                // Safe cast because the codec guarantees the type
                copy.set((DataComponentType<Object>) type, value);
                
            } catch (Exception e) {
                CagedMobs.LOGGER.error("Failed to parse component {}: {}", entry.getKey(), e.getMessage()); 
            }
        }
        return copy;
    }

    private static RegistryAccess getComponentRegistryAccess() {
        try {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                return ServerLifecycleHooks.getCurrentServer().registryAccess();
            }
        } catch (Exception ignored) {
            // Fall back to built-in registries when server context is unavailable.
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    private static ItemStack firstStack(Ingredient ingredient) {
        if (ingredient == null) {
            return ItemStack.EMPTY;
        }
        return ingredient.items().findFirst().map(holder -> holder.value().getDefaultInstance()).orElse(ItemStack.EMPTY);
    }

    public ItemStack getItemStack() {
        return withRecipeComponents(this.item);
    }

    public ItemStack getCookedItemStack() {
        return withRecipeComponents(this.cookedItem);
    }

    @Override
    public String toString() {
        return "Loot data - item: " + this.item.toString() + ", chance: " + this.chance + ", min: " + this.minAmount + ", max: " + this.maxAmount;
    }

    public float getChance() {
        return this.chance;
    }

    public Ingredient getItem() {
        return this.item;
    }

    public Ingredient getCookedItem() {
        return this.cookedItem;
    }

    public Optional<Ingredient> getOptionalCookedItem() {
        return Optional.ofNullable(this.cookedItem).filter(ingredient -> !ingredient.isEmpty());
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public boolean isLighting(){
        return this.lighting;
    }
    public boolean isCooking(){
        return this.cookedItem != null && !this.cookedItem.isEmpty();
    }
    public boolean isArrow(){
        return this.arrow;
    }

    public boolean hasColor(){
        return this.getColor() != -1;
    }

    public int getColor(){
        return this.color;
    }

    public boolean ifRandomDurability(){
        return this.randomDurability;
    }

    public String getComponents(){
        return this.components;
    }

    private ItemStack withRecipeComponents(Ingredient ingredient) {
        ItemStack stack = firstStack(ingredient);
        if (stack.isEmpty() || this.components.isEmpty()) {
            return stack;
        }
        return writeComponents(this.components, stack);
    }
}
