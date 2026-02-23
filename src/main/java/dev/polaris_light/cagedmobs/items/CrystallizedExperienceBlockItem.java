package dev.polaris_light.cagedmobs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import javax.annotation.Nonnull;

public class CrystallizedExperienceBlockItem extends BlockItem {

    public CrystallizedExperienceBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.cagedmobs.crystallized_experience.info2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.cagedmobs.crystallized_experience.info3").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if(itemStack.getItem() instanceof CrystallizedExperienceBlockItem){
            // If on client side, just play the sound
            if(level.isClientSide()){
                level.playSound(player, player.getX(), player.getY()+0.5,player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.35F + 0.9F);
            // Do the logic on server side
            }else{
                // Consume the whole stack
                if (player.isCrouching()) {
                    for(int i = 0; i < itemStack.getCount(); i++){
                        player.giveExperiencePoints(9 * (level.random.nextInt(2) + 1));
                    }
                    if(!player.isCreative()) {
                        itemStack.setCount(0);
                    }
                    // Consume single item
                } else {
                    if(!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    player.giveExperiencePoints(9 * (level.random.nextInt(2) + 1));
                }
            }
            InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

}
