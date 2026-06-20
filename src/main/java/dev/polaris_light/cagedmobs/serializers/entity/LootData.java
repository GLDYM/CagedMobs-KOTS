package dev.polaris_light.cagedmobs.serializers.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.polaris_light.cagedmobs.CagedMobs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class LootData {

    public static final Codec<LootData> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Ingredient.CODEC.fieldOf("output").forGetter(LootData::getItem),
            Ingredient.CODEC.optionalFieldOf("output_cooked", Ingredient.EMPTY).forGetter(LootData::getCookedItem),
            Codec.FLOAT.fieldOf("chance").forGetter(LootData::getChance),
            Codec.INT.fieldOf("minAmount").forGetter(LootData::getMinAmount),
            Codec.INT.fieldOf("maxAmount").forGetter(LootData::getMaxAmount),
            Codec.BOOL.fieldOf("lighting").orElse(false).forGetter(LootData::isLighting),
            Codec.BOOL.fieldOf("needsArrow").orElse(false).forGetter(LootData::isArrow),
            Codec.INT.fieldOf("color").orElse(-1).forGetter(LootData::getColor),
            Codec.BOOL.fieldOf("randomDurability").orElse(false).forGetter(LootData::ifRandomDurability),
            Codec.STRING.fieldOf("components").orElse("").forGetter(LootData::getComponents),
            Codec.STRING.fieldOf("requiredUpgrade").orElse("").forGetter(LootData::getRequiredUpgradeId)
    ).apply(builder, LootData::new));

    private final float chance;
    private Ingredient item;
    private Ingredient cookedItem;
    private final int minAmount;
    private final int maxAmount;
    @Deprecated
    private final boolean lighting;
    @Deprecated
    private final boolean arrow;
    private final int color;
    private final boolean randomDurability;
    private final String components;
    private final String requiredUpgradeId;

    public LootData(Ingredient item, Ingredient cookedItem, float chance, int min, int max, boolean lighting, boolean arrow, int color, boolean randomDurability, String components, String requiredUpgradeId) {
        this.chance = chance;
        this.item = item;
        this.cookedItem = cookedItem;
        this.minAmount = min;
        this.maxAmount = max;
        this.lighting = lighting;
        this.arrow = arrow;
        this.color = color;
        this.randomDurability = randomDurability;
        this.components = components;
        this.requiredUpgradeId = resolveRequiredUpgradeId(requiredUpgradeId, lighting, arrow);
        // Check for errors
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Amounts must not be negative!");
        }
        if (min > max) {
            throw new IllegalArgumentException("Min amount must not be greater than max amount!");
        }
        // Apply Components data
        if(!components.isEmpty()){
            ItemStack newItem = writeComponents(components, item.getItems()[0]);
            this.item = Ingredient.of(newItem);
            if (this.cookedItem != Ingredient.EMPTY) {
                ItemStack newCookedItem = writeComponents(components, cookedItem.getItems()[0]);
                this.cookedItem = Ingredient.of(newCookedItem);
            }
        }
    }

    public static void serializeBuffer(RegistryFriendlyByteBuf buffer, LootData lootData) {
        buffer.writeFloat(lootData.getChance());

        ItemStack[] items = lootData.getItem().getItems();
        if (items.length > 0) {
            buffer.writeBoolean(true);
            ItemStack.STREAM_CODEC.encode(buffer, items[0]);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeInt(lootData.getMinAmount());
        buffer.writeInt(lootData.getMaxAmount());
        buffer.writeBoolean(lootData.isLighting());
        buffer.writeBoolean(lootData.isArrow());

        ItemStack[] cookedItems = lootData.getCookedItem().getItems();
        if (cookedItems.length > 0) {
            buffer.writeBoolean(true);
            ItemStack.STREAM_CODEC.encode(buffer, cookedItems[0]);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeInt(lootData.getColor());
        buffer.writeBoolean(lootData.ifRandomDurability());
        buffer.writeUtf(lootData.getComponents());
        buffer.writeUtf(lootData.getRequiredUpgradeId());
    }


    public static LootData deserializeBuffer(RegistryFriendlyByteBuf buffer) {
        final float chance = buffer.readFloat();

        Ingredient item;
        if (buffer.readBoolean()) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            item = Ingredient.of(stack);
        } else {
            item = Ingredient.EMPTY;
        }

        final int min = buffer.readInt();
        final int max = buffer.readInt();
        final boolean isLightning = buffer.readBoolean();
        final boolean isArrow = buffer.readBoolean();

        Ingredient cookedItem;
        if (buffer.readBoolean()) {
            ItemStack cookedStack = ItemStack.STREAM_CODEC.decode(buffer);
            cookedItem = Ingredient.of(cookedStack);
        } else {
            cookedItem = Ingredient.EMPTY;
        }

        final int color = buffer.readInt();
        final boolean randomDurability = buffer.readBoolean();
        final String components = buffer.readUtf();
        final String requiredUpgradeId = buffer.readUtf();

        return new LootData(item, cookedItem, chance, min, max, isLightning, isArrow, color, randomDurability, components, requiredUpgradeId);
    }


    /**
     * Writes Components data to an item stack
     * @param components the data for the Components variable
     * @param stack the item stack to write to
     * @return the updated item stack reference
     */
    public static ItemStack writeComponents(String components, ItemStack stack) {
        ItemStack copy = stack.copy();

        JsonObject json = JsonParser.parseString(components).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            try {
                ResourceLocation id = ResourceLocation.parse(entry.getKey());
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);

                if (type == null) {
                    throw new IllegalArgumentException("Unknown component key: " + entry.getKey());
                }

                // Use the component's codec to parse the JSON value
                RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
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

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    @Deprecated
    public boolean isLighting(){
        return "cagedmobs:lightning_upgrade".equals(this.requiredUpgradeId);
    }

    public boolean isCooking(){
        return !this.cookedItem.isEmpty();
    }

    @Deprecated
    public boolean isArrow(){
        return "cagedmobs:arrow_upgrade".equals(this.requiredUpgradeId);
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

    public boolean requiresUpgrade() {
        return !this.requiredUpgradeId.isEmpty();
    }

    public String getRequiredUpgradeId() {
        return this.requiredUpgradeId;
    }

    public Item getRequiredUpgradeItem() {
        if (this.requiredUpgradeId.isEmpty()) {
            return null;
        }

        ResourceLocation id = ResourceLocation.tryParse(this.requiredUpgradeId);
        if (id == null) {
            return null;
        }

        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null ? null : item;
    }

    private static String resolveRequiredUpgradeId(String explicitId, boolean lighting, boolean arrow) {
        if (explicitId != null && !explicitId.isEmpty()) {
            return explicitId;
        }
        if (lighting) {
            return "cagedmobs:lightning_upgrade";
        }
        if (arrow) {
            return "cagedmobs:arrow_upgrade";
        }
        return "";
    }
}
