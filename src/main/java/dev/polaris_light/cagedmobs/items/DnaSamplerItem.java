package dev.polaris_light.cagedmobs.items;

import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.registers.CagedRecipeTypes;
import dev.polaris_light.cagedmobs.serializers.RecipesHelper;
import dev.polaris_light.cagedmobs.serializers.SerializationHelper;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import javax.annotation.Nonnull;

public class DnaSamplerItem extends Item {
    public DnaSamplerItem(Properties properties) {
        super(properties);
    }

    // Called on left-click on an entity to get it's sample
    @Override
    public void hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        if(!CommonConfig.disableSamplers.get()) {
            if (target.level().isClientSide() || !(attacker instanceof Player)) {
                return;
            }
            Player player = (Player) attacker;
            // Select the hand where the sampler is
            InteractionHand hand;
            if (player.getMainHandItem().equals(stack)) {
                hand = InteractionHand.MAIN_HAND;
            } else if (player.getOffhandItem().equals(stack)) {
                hand = InteractionHand.OFF_HAND;
            } else {
                return;
            }
            // Try to sample the target
            if (canBeCached(target) && !RecipesHelper.isEntityTypeBlacklisted(target.getType())) {
                if (samplerTierSufficient(stack, target)) {
                    CompoundTag nbt = new CompoundTag();
                    SerializationHelper.serializeEntityTypeNBT(nbt, target.getType());
                    // If sheep add it's color to nbt
                    if (target instanceof Sheep) {
                        Sheep sheep = (Sheep) target;
                        DyeColor color = sheep.getColor();
                        nbt.putInt("Color", color.getId());
                    }
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)); 
                    stack.get(DataComponents.CUSTOM_DATA).update(tag -> tag.putString("entity", nbt.toString()));
                    player.setItemInHand(hand, stack);
                    return;
                } else {
                    player.sendOverlayMessage(Component.translatable("item.cagedmobs.dna_sampler.not_sufficient").withStyle(ChatFormatting.RED));
                }
            } else {
                player.sendOverlayMessage(Component.translatable("item.cagedmobs.dna_sampler.not_cachable").withStyle(ChatFormatting.RED));
            }
        }
    }

    // Checks if a sampler's tier is sufficient to sample given entity
    private static boolean samplerTierSufficient(ItemStack stack, Entity target) {
        EntityType<?> type = target.getType();
        boolean sufficient = false;
        for(final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe != null) {
                // Check for null exception
                if(recipe.getEntityType() == null){continue;}
                if(recipe.getEntityType().equals(type) && recipe.getSamplerTier() <= getSamplerTierInt(stack.getItem())) {
                    sufficient = true;
                    break;
                }
            }
        }
        return sufficient;
    }

    // Returns a tier number in a form of int from Item
    private static int getSamplerTierInt(Item item) {
        if(item instanceof DnaSamplerNetheriteItem){
            return 3;
        }else if(item instanceof DnaSamplerDiamondItem){
            return 2;
        }else{
            return 1;
        }
    }

    // Check if entity can be cached based on the list of cachable entities
    private static boolean canBeCached(Entity clickedEntity) {
        boolean contains = false;
        for(final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe != null) {
                // Check for null exception
                if(recipe.getEntityType() == null){continue;}
                if(recipe.getEntityType().equals(clickedEntity.getType())) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    @Override
    public InteractionResult use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if(player.isCrouching() && containsEntityType(itemstack)) {
            removeEntityType(itemstack);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static void appendSamplerTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(getTooltip(stack));
        tooltipComponents.add(getInformationForTier(stack.getItem()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.cagedmobs.dna_sampler.makeEmpty").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.cagedmobs.dna_sampler.getBackEntity").withStyle(ChatFormatting.GRAY));
        if(CommonConfig.disableSamplers.get()){
            tooltipComponents.add(Component.translatable("item.cagedmobs.dna_sampler.disabled").withStyle(ChatFormatting.RED));
        }
    }

    private static MutableComponent getInformationForTier(Item item){
        if(item instanceof DnaSamplerNetheriteItem){
            return Component.translatable("item.cagedmobs.dna_sampler.tier3Info");
        }else if(item instanceof DnaSamplerDiamondItem){
            return Component.translatable("item.cagedmobs.dna_sampler.tier2Info");
        }else{
            return Component.translatable("item.cagedmobs.dna_sampler.tier1Info");
        }
    }

    private static Component getTooltip(ItemStack stack) {
        if(!DnaSamplerItem.containsEntityType(stack)) {
            return Component.translatable("item.cagedmobs.dna_sampler.empty").withStyle(ChatFormatting.YELLOW);
        }else {
            EntityType<?> type = getEntityType(stack);
            // Add the text component
            if(type != null){
                return Component.translatable(type.getDescriptionId()).withStyle(ChatFormatting.YELLOW);
            }else{
                // If not found say Unknown entity for crash prevention
                return Component.translatable("item.cagedmobs.dna_sampler.unknown_entity").withStyle(ChatFormatting.YELLOW);
            }
        }
    }

    public static boolean containsEntityType(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        CompoundTag tag = data.copyTag();
        return tag.contains("entity");
    }

    public void removeEntityType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            tag.remove("Color");
            tag.remove("entity");
            if (tag.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            } else {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }


    public void setEntityTypeFromCage(MobCageBlockEntity cage, ItemStack stack, Player player, InteractionHand hand){
        EntityType<?> type = cage.getEntityType();
        CompoundTag nbt = new CompoundTag();
        SerializationHelper.serializeEntityTypeNBT(nbt, type);
        // If sheep add it's color to nbt
        if(type.toString().contains("sheep")){
            nbt.putInt("Color",cage.getColor());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)); 
        stack.get(DataComponents.CUSTOM_DATA).update(tag -> tag.putString("entity", nbt.toString()));
        player.setItemInHand(hand, stack);
    }

    public static EntityType<?> getEntityType(ItemStack stack) {
        if (stack.isEmpty()) return null;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();
        if (tag.contains("entity")) {
            return SerializationHelper.deserializeEntityTypeNBT(tag);
        }
        return null;
    }
}
