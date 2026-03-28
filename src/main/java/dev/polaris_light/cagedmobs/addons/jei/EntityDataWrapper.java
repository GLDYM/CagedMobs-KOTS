package dev.polaris_light.cagedmobs.addons.jei;

import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.registers.CagedItems;
import dev.polaris_light.cagedmobs.serializers.RecipesHelper;
import dev.polaris_light.cagedmobs.serializers.SerializationHelper;
import dev.polaris_light.cagedmobs.serializers.entity.AdditionalLootData;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import dev.polaris_light.cagedmobs.serializers.entity.LootData;
import dev.polaris_light.cagedmobs.serializers.environment.EnvironmentData;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EntityDataWrapper {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private final EntityData entityData;
    private final List<ItemStack> envs = NonNullList.create();
    private final List<LootData> drops = NonNullList.create();
    private final List<ItemStack> samplers = NonNullList.create();
    private final List<Integer> cookedIDs = new ArrayList<>();
    private final boolean requiresWater;
    private final int ticks;

    public EntityDataWrapper(EntityData entityData) {
        this.entityData = entityData;

        for (EnvironmentData env : RecipesHelper.getEnvironmentRecipesList()) {
            if (RecipesHelper.isEnvValidForEntity(entityData, env)) {
                this.envs.addAll(ingredientToStacks(env.getInputItem()));
            }
        }

        if (entityData.getSamplerTier() >= 3) {
            this.samplers.add(new ItemStack(CagedItems.NETHERITE_DNA_SAMPLER.get()));
        } else if (entityData.getSamplerTier() == 2) {
            this.samplers.add(new ItemStack(CagedItems.NETHERITE_DNA_SAMPLER.get()));
            this.samplers.add(new ItemStack(CagedItems.DIAMOND_DNA_SAMPLER.get()));
        } else {
            this.samplers.add(new ItemStack(CagedItems.NETHERITE_DNA_SAMPLER.get()));
            this.samplers.add(new ItemStack(CagedItems.DIAMOND_DNA_SAMPLER.get()));
            this.samplers.add(new ItemStack(CagedItems.DNA_SAMPLER.get()));
        }

        List<Item> filteredItems = RecipesHelper.getItemsFromConfigList();
        boolean whitelistMode = CommonConfig.itemsListInWhitelistMode.get();

        for (LootData data : entityData.getResults()) {
            addLootEntry(data, filteredItems, whitelistMode);
        }

        for (AdditionalLootData additionalLootData : RecipesHelper.getAdditionalLootRecipesList()) {
            if (additionalLootData.getEntityType() == null || entityData.getEntityType() == null) {
                continue;
            }
            if (!entityData.getEntityType().equals(additionalLootData.getEntityType())) {
                continue;
            }

            for (LootData data : additionalLootData.getResults()) {
                if (!additionalLootData.isRemoveFromEntity()) {
                    addLootEntry(data, filteredItems, whitelistMode);
                } else {
                    this.drops.removeIf(drop -> drop.getItemStack().getItem().equals(data.getItemStack().getItem()));
                }
            }
        }

        this.ticks = entityData.getTotalGrowTicks();
        this.requiresWater = entityData.ifRequiresWater();
    }

    public void setRecipe(IRecipeLayoutBuilder builder) {
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(this.samplers);

        IRecipeSlotBuilder samplersSlot = builder.addSlot(RecipeIngredientRole.INPUT, 15, 82)
            .setSlotName("samplers")
            .setStandardSlotBackground()
            .addItemStacks(this.getSampledSamplers());

        if (!CommonConfig.disableSpawnEggs.get() && this.entityData.getEntityType() != null) {
            SpawnEggItem.byId(this.entityData.getEntityType())
                .ifPresent(holder -> samplersSlot.add(holder.value().getDefaultInstance()));
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 35, 82)
            .setSlotName("environments")
            .setStandardSlotBackground()
            .addItemStacks(this.envs)
            .addRichTooltipCallback(this.getEnvTooltip());

        for (int index = 0; index < this.drops.size() && index < 20; index++) {
            LootData entry = this.drops.get(index);
            ItemStack stack = entry.getItemStack();
            if (entry.isCooking() && this.cookedIDs.contains(index)) {
                stack = entry.getCookedItemStack();
            }

            int x = 101 + 19 * (index % 4);
            int y = 6 + 19 * (index / 4);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .setStandardSlotBackground()
                .add(stack)
                .addRichTooltipCallback(this.getLootTooltip(entry));
        }
    }

    public void draw(GuiGraphicsExtractor graphics) {
        // Intentionally empty for now: slot layout and tooltips are fully handled in setRecipe.
    }

    private void addLootEntry(LootData data, List<Item> filteredItems, boolean whitelistMode) {
        if (data.getItemStack().isEmpty()) {
            return;
        }

        Item baseItem = data.getItemStack().getItem();
        boolean listed = filteredItems.contains(baseItem);
        boolean allowed = whitelistMode ? listed : !listed;
        if (!allowed || this.drops.contains(data)) {
            return;
        }

        this.drops.add(data);
        if (data.isCooking() && !data.getCookedItemStack().isEmpty()) {
            this.drops.add(data);
            this.cookedIDs.add(this.drops.size() - 1);
        }
    }

    public List<ItemStack> getSampledSamplers() {
        List<ItemStack> sampled = NonNullList.create();
        for (ItemStack sampler : this.samplers) {
            if (this.entityData.getEntityType() == null) {
                continue;
            }
            ItemStack stack = sampler.copy();
            CompoundTag nbt = new CompoundTag();
            SerializationHelper.serializeEntityTypeNBT(nbt, this.entityData.getEntityType());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            sampled.add(stack);
        }
        return sampled;
    }

    private IRecipeSlotRichTooltipCallback getEnvTooltip() {
        return (view, tooltip) -> view.getDisplayedItemStack().ifPresent(displayedItem -> {
            EnvironmentData env = MobCageBlockEntity.getEnvironmentDataFromItemStack(displayedItem);
            if (env != null) {
                tooltip.add(Component.translatable(
                    "jei.tooltip.cagedmobs.entity.growModifier",
                    DECIMAL_FORMAT.format(env.getGrowModifier() * 100 - 100)
                ));
            }
        });
    }

    private IRecipeSlotRichTooltipCallback getLootTooltip(LootData entry) {
        return (view, tooltip) -> view.getDisplayedItemStack().ifPresent(displayedItem -> {
            tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.chance", DECIMAL_FORMAT.format(entry.getChance() * 100)));
            if (entry.getMinAmount() == entry.getMaxAmount()) {
                tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.amountEqual", entry.getMinAmount()));
            } else {
                tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.amount", entry.getMinAmount(), entry.getMaxAmount()));
            }
            if (entry.isLighting()) {
                tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.lightning_upgrade").withStyle(ChatFormatting.YELLOW));
            }
            if (entry.isCooking() && displayedItem.getItem().equals(entry.getCookedItemStack().getItem())) {
                tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.cooking_upgrade").withStyle(ChatFormatting.YELLOW));
            }
            if (entry.isArrow()) {
                tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.arrow_upgrade").withStyle(ChatFormatting.YELLOW));
            }
            if (entry.hasColor()) {
                tooltip.add(Component.translatable("jei.tooltip.cagedmobs.entity.colorItem").withStyle(ChatFormatting.YELLOW));
            }
        });
    }

    private static List<ItemStack> ingredientToStacks(Ingredient ingredient) {
        return ingredient.items().map(holder -> holder.value().getDefaultInstance()).toList();
    }

    public int getTicks() {
        return this.ticks;
    }

    public int getSeconds() {
        return this.ticks / 20;
    }

    public boolean ifRequiresWater() {
        return this.requiresWater;
    }
}
