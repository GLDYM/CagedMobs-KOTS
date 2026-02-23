package dev.polaris_light.cagedmobs.blocks.mob_cage;

import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.items.DnaSamplerDiamondItem;
import dev.polaris_light.cagedmobs.items.DnaSamplerItem;
import dev.polaris_light.cagedmobs.items.DnaSamplerNetheriteItem;
import dev.polaris_light.cagedmobs.items.EmptySpawnEggItem;
import dev.polaris_light.cagedmobs.items.upgrades.UpgradeItem;
import dev.polaris_light.cagedmobs.registers.CagedBlockEntities;
import dev.polaris_light.cagedmobs.registers.CagedItems;
import dev.polaris_light.cagedmobs.serializers.RecipesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import static dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity.ENVIRONMENT_SLOT;

public class MobCageBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<MobCageBlock> CODEC = simpleCodec(MobCageBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty HOPPING = BooleanProperty.create("hopping");
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 15.0D, 14.0D);

    public MobCageBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(HOPPING, Boolean.valueOf(false)));
    }

    @Override
    protected MapCodec<MobCageBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED).add(HOPPING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MobCageBlockEntity(pPos, pState);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        // If on client side, skip.
        if(level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MobCageBlockEntity cageBE) {
            // Try to add environment
            if(!cageBE.hasEnvironment()){
                // Check if there exists a recipe for given item
                if(MobCageBlockEntity.existsEnvironmentFromItemStack(itemStack)){
                    // Set environment
                    cageBE.setEnvironment(itemStack);
                    if(!player.isCreative()){
                        itemStack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            // Try to add upgrades
            if(cageBE.acceptsUpgrades()){
                if(itemStack.getItem() instanceof UpgradeItem){
                    cageBE.addUpgrade(itemStack);
                    if(!player.isCreative()){
                        itemStack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            // Add or remove entity
            if(itemStack.getItem() instanceof DnaSamplerItem sampler){
                if(!CommonConfig.disableSamplers.get()) {
                    if (!cageBE.hasEntity()) {
                        // Check if there exists a recipe for a given entity type,
                        // if the environment is suitable for that entity and if it is not blacklisted.
                        if (cageBE.existsEntityDataFromType(sampler.getEntityType(itemStack))
                                && cageBE.isEnvironmentSuitable(player, sampler.getEntityType(itemStack), state)
                                && !RecipesHelper.isEntityTypeBlacklisted(sampler.getEntityType(itemStack))) {
                            // Add entity
                            cageBE.setEntityFromSampler(sampler.getEntityType(itemStack), itemStack);
                            // Clear the sampler
                            if (!player.isCreative()) {
                                // If single use samplers config is enabled, destroy the sampler. If not, just use its DNA.
                                if (CommonConfig.singleUseSamplers.get()) {
                                    player.onEquippedItemBroken(itemStack.getItem(), hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                                    itemStack.shrink(1);
                                } else {
                                    sampler.removeEntityType(itemStack);
                                }
                            }
                            return ItemInteractionResult.SUCCESS;
                        }
                        return ItemInteractionResult.FAIL;
                    // Retrieve entity from the cage
                    } else {
                        if (!DnaSamplerItem.containsEntityType(itemStack) && cageBE.getEntity().isPresent()) {
                            // Check if sampler's tier is sufficient
                            if (cageBE.getEntity().get().getSamplerTier() >= 3 && !(itemStack.getItem() instanceof DnaSamplerNetheriteItem)) {
                                player.displayClientMessage(Component.translatable("block.cagedmobs.mob_cage.samplerNotSufficient").withStyle(ChatFormatting.RED), true);
                                return ItemInteractionResult.FAIL;
                            }
                            if (cageBE.getEntity().get().getSamplerTier() >= 2 && !((itemStack.getItem() instanceof DnaSamplerNetheriteItem) || (itemStack.getItem() instanceof DnaSamplerDiamondItem))) {
                                player.displayClientMessage(Component.translatable("block.cagedmobs.mob_cage.samplerNotSufficient").withStyle(ChatFormatting.RED), true);
                                return ItemInteractionResult.FAIL;
                            }
                            // Get back the entity
                            sampler.setEntityTypeFromCage(cageBE, itemStack, player, hand);
                            cageBE.setChanged();
                        } else {
                            player.displayClientMessage(Component.translatable("block.cagedmobs.mob_cage.cageAlreadyUsed").withStyle(ChatFormatting.RED), true);
                            return ItemInteractionResult.FAIL;
                        }
                        cageBE.removeEntity();
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            // Add entity from spawn egg
            if(itemStack.getItem() instanceof SpawnEggItem spawnEggItem){
                if(!CommonConfig.disableSpawnEggs.get()) {
                    if (!cageBE.hasEntity()) {
                        EntityType<?> entityType = spawnEggItem.getType(itemStack);
                        // Check if there exists a recipe for a given entity type,
                        // if the environment is suitable for that entity and if it is not blacklisted.
                        if (cageBE.existsEntityDataFromType(entityType)
                                && cageBE.isEnvironmentSuitable(player, entityType, state)
                                && !RecipesHelper.isEntityTypeBlacklisted(entityType)) {
                            // Add entity
                            cageBE.setEntityFromSampler(entityType, itemStack);
                            // Clear the sampler
                            if (!player.isCreative()) {
                                itemStack.shrink(1);
                                player.addItem(new ItemStack(CagedItems.EMPTY_SPAWN_EGG.get()));
                            }
                            return ItemInteractionResult.SUCCESS;
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("block.cagedmobs.mob_cage.cageAlreadyUsed").withStyle(ChatFormatting.RED), true);
                        return ItemInteractionResult.FAIL;
                    }
                }else{
                    player.displayClientMessage(Component.translatable("block.cagedmobs.mob_cage.spawnEggsDisabled").withStyle(ChatFormatting.RED), true);
                }
                return ItemInteractionResult.CONSUME;
            }
            // Retrieve entity from the cage with empty spawn egg
            if(itemStack.getItem() instanceof EmptySpawnEggItem){
                if(!CommonConfig.disableSpawnEggs.get()){
                    if(cageBE.hasEntity()){
                        SpawnEggItem spawnEgg = SpawnEggItem.byId(cageBE.getEntityType());
                        if(spawnEgg != null){
                            if(!player.isCreative()){
                                player.addItem(new ItemStack(spawnEgg));
                                itemStack.shrink(1);
                            }
                            cageBE.removeEntity();
                            return ItemInteractionResult.SUCCESS;
                        }
                       return ItemInteractionResult.FAIL;
                    }
                }else{
                    player.displayClientMessage(Component.translatable("block.cagedmobs.mob_cage.spawnEggsDisabled").withStyle(ChatFormatting.RED), true);
                    return ItemInteractionResult.FAIL;
                }
            }
            // Try to harvest the cage with sword
            if(itemStack.getItem() instanceof SwordItem){
                if((!state.getValue(HOPPING) || CommonConfig.hoppingCagesDisabled.get()) && cageBE.isWaitingForHarvest()){
                    cageBE.onPlayerHarvest(cageBE.getBlockState());
                    if(!player.isCreative() && !level.isClientSide()){
                        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            // If crouching remove entity
            // But I hate this when carrying on a mob cage.
            // if(player.isCrouching()){
            //     if(cageBE.hasEntity()){
            //         cageBE.removeEntity();
            //         cageBE.setChanged();
            //     }
            //     return ItemInteractionResult.SUCCESS;
            // }
            // Open the GUI
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return state.getValue(HOPPING) ? Component.translatable("block.cagedmobs.hopping_mob_cage") : Component.translatable("block.cagedmobs.mob_cage");
                }
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    ContainerData data = new SimpleContainerData(2);              
                    if (cageBE.getEntityType() != null) {
                        data.set(0, BuiltInRegistries.ENTITY_TYPE.getId(cageBE.getEntityType()));
                    } else {
                        data.set(0, -1);
                    }
                    data.set(1, cageBE.getColor());
                    AbstractContainerMenu menu = new MobCageContainer(
                        pContainerId, 
                        pPlayerInventory, 
                        ContainerLevelAccess.create(level, pos), 
                        cageBE.getInventoryHandler(),
                        data
                    );
                    menu.addSlotListener(new ContainerListener() {
                        @Override
                        public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
                            if(pDataSlotIndex == ENVIRONMENT_SLOT){
                                cageBE.updateEnvironment();
                            }
                        }
                        @Override
                        public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {}
                    });
                    return menu;
                }
            };
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.openMenu(containerProvider, cageBE.getBlockPos());
            return ItemInteractionResult.SUCCESS;
        } else {
            throw new IllegalStateException("Mob Cage container provider is missing!");
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult trace) {
        // I don't know why this method will not be called even when without item on your hands, Bruh.
        if(level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MobCageBlockEntity cageBE){
            // If crouching remove entity
            // But I hate this when carrying on a mob cage.
            // if(player.isCrouching()){
            //     if(cageBE.hasEntity()){
            //         cageBE.removeEntity();
            //         cageBE.setChanged();
            //     }
            //     return InteractionResult.SUCCESS;
            // }
            // Open the GUI
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return state.getValue(HOPPING) ? Component.translatable("block.cagedmobs.hopping_mob_cage") : Component.translatable("block.cagedmobs.mob_cage");
                }
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    ContainerData data = new SimpleContainerData(2);
                    if (cageBE.getEntityType() != null) {
                        data.set(0, BuiltInRegistries.ENTITY_TYPE.getId(cageBE.getEntityType()));
                    } else {
                        data.set(0, -1);
                    }
                    data.set(1, cageBE.getColor());
                    AbstractContainerMenu menu = new MobCageContainer(
                        pContainerId, 
                        pPlayerInventory, 
                        ContainerLevelAccess.create(level, pos), 
                        cageBE.getInventoryHandler(),
                        data
                    );
                    menu.addSlotListener(new ContainerListener() {
                        @Override
                        public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
                            if(pDataSlotIndex == ENVIRONMENT_SLOT){
                                cageBE.updateEnvironment();
                            }
                        }
                        @Override
                        public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {}
                    });
                    return menu;
                }
            };
            // Open the GUI
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.openMenu(containerProvider, cageBE.getBlockPos());
            return InteractionResult.SUCCESS;
        } else {
            throw new IllegalStateException("Mob Cage container provider is missing!");
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), MobCageBlockEntity::tick);
    }

    /**
     * Called on block remove, should drop all items in the inventory.
     */
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof final MobCageBlockEntity tile) {
                tile.dropInventory();
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    // Block shape
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Water-logging
    @javax.annotation.Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return super.getStateForPlacement(pContext).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    /**
     * Called when placed or when neighbour is updated.
     */
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    // Mods Support

//    /**
//     * Used for TheOneProbe mod support.
//     */
//    @Override
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
//        if((world.getBlockEntity(data.getPos()) instanceof MobCageBlockEntity tile)) {
//            if (tile.hasEnvironment() && tile.hasEntity()) {
//                probeInfo.progress((int) (tile.getGrowthPercentage() * 100), 100, probeInfo.defaultProgressStyle().suffix("%").filledColor(0xff44AA44).alternateFilledColor(0xff44AA44).backgroundColor(0xff836953));
//            }
//            if (tile.hasEnvironment()) {
//                probeInfo.horizontal().text(Component.translatable("JADE.tooltip.cagedmobs.cage.environment"));
//                ItemStack envItem = tile.getInventoryHandler().getStackInSlot(ENVIRONMENT_SLOT);
//                if(!envItem.isEmpty()){
//                    probeInfo.horizontal().item(envItem).itemLabel(envItem);
//                }
//            }
//            if(tile.hasEntity()){
//                probeInfo.horizontal().text(Component.translatable("JADE.tooltip.cagedmobs.cage.entity").withStyle(ChatFormatting.GRAY).getString() +
//                        Component.translatable(tile.getEntityType().getDescriptionId()).withStyle(ChatFormatting.GRAY).getString());
//            }
//            // Upgrades
//            if(tile.hasAnyUpgrades()){
//                // Add Upgrade text
//                probeInfo.horizontal().text(Component.translatable("TOP.tooltip.cagedmobs.cage.upgrades"));
//                IProbeInfo hor = probeInfo.horizontal();
//                for(ItemStack upgrade : tile.getUpgradesAsItemStacks()){
//                    if(!upgrade.isEmpty()){
//                        hor.item(upgrade);
//                    }
//                }
//            }
//        }
//    }
}