package dev.polaris_light.cagedmobs.serializers.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.component.DataComponents;

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
            Codec.STRING.fieldOf("nbtName").orElse("").forGetter(LootData::getNbtName),
            Codec.STRING.fieldOf("nbtData").orElse("").forGetter(LootData::getNbtData)
    ).apply(builder, LootData::new));

    private final float chance;
    private Ingredient item;
    private final Ingredient cookedItem;
    private final int minAmount;
    private final int maxAmount;
    private final boolean lighting;
    private final boolean arrow;
    private final int color;
    private final boolean randomDurability;
    private final String nbtName;
    private final String nbtData;

    public LootData(Ingredient item, Ingredient cookedItem, float chance, int min, int max, boolean lighting, boolean arrow, int color, boolean randomDurability, String nbtName, String nbtData){
        this.chance = chance;
        this.item = item;
        this.cookedItem = cookedItem;
        this.minAmount = min;
        this.maxAmount = max;
        this.lighting = lighting;
        this.arrow = arrow;
        this.color = color;
        this.randomDurability = randomDurability;
        this.nbtName = nbtName;
        this.nbtData = nbtData;
        // Check for errors
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Amounts must not be negative!");
        }
        if (min > max) {
            throw new IllegalArgumentException("Min amount must not be greater than max amount!");
        }
        // Apply NBT data
        if(!nbtName.isEmpty() && !nbtData.isEmpty()){
            ItemStack newItem = writeNBTtoItem(nbtName, nbtData, item.getItems()[0]);
            this.item = Ingredient.of(newItem);
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
        buffer.writeUtf(lootData.getNbtName());
        buffer.writeUtf(lootData.getNbtData());
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
        final String nbtName = buffer.readUtf();
        final String nbtData = buffer.readUtf();

        return new LootData(item, cookedItem, chance, min, max, isLightning, isArrow, color, randomDurability, nbtName, nbtData);
    }


    /**
     * Writes NBT data to an item stack
     * @param nbtName the name of the NBT variable
     * @param nbtData the data for the NBT variable
     * @param stack the item stack to write to
     * @return the updated item stack reference
     */
    public static ItemStack writeNBTtoItem(String nbtName, String nbtData, ItemStack stack){
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())); 
        stack.get(DataComponents.CUSTOM_DATA).update(tag -> tag.putString(nbtName, nbtData));
        return stack;
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

    public boolean isLighting(){
        return this.lighting;
    }
    public boolean isCooking(){
        return !this.cookedItem.isEmpty();
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

    public String getNbtName(){
        return this.nbtName;
    }

    public String getNbtData(){
        return this.nbtData;
    }
}
