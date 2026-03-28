package dev.polaris_light.cagedmobs.blocks.mob_cage;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.configs.ClientConfig;
import dev.polaris_light.cagedmobs.configs.CommonConfig;
import dev.polaris_light.cagedmobs.items.upgrades.SpeedIIIUpgradeItem;
import dev.polaris_light.cagedmobs.items.upgrades.SpeedIIUpgradeItem;
import dev.polaris_light.cagedmobs.items.upgrades.SpeedIUpgradeItem;
import dev.polaris_light.cagedmobs.registers.CagedBlockEntities;
import dev.polaris_light.cagedmobs.registers.CagedItems;
import dev.polaris_light.cagedmobs.serializers.RecipesHelper;
import dev.polaris_light.cagedmobs.serializers.entity.EntityData;
import dev.polaris_light.cagedmobs.serializers.environment.EnvironmentData;
import dev.polaris_light.cagedmobs.serializers.entity.AdditionalLootData;
import dev.polaris_light.cagedmobs.serializers.entity.LootData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;

import static dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlock.HOPPING;
import static dev.polaris_light.cagedmobs.helpers.UpgradeItemsParticles.*;

public class MobCageBlockEntity extends BlockEntity {

    // Entity and Environment data
    private EnvironmentData environmentData = null;
    private EntityData entity = null;
    private EntityType<?> entityType = null;
    private Entity cachedEntity;
    private SpawnData renderedEntity;
    // Ticks
    private int currentGrowTicks = 0;
    private int totalGrowTicks = 0;
    private boolean waitingForHarvest = false;
    // Color of entity
    private int color = 0;
    // Saving and loading
    public static final String ITEMS_TAG = "Inventory";
    // Item capability
    public static int UPGRADES_COUNT = 3;
    public static int ENVIRONMENT_SLOT = 0;
    public static int SLOT_COUNT = UPGRADES_COUNT + 1;
    // Item handlers
    private final CageItemResourceHandler items = createItemHandler();
    private final ResourceHandler<ItemResource> inventoryResourceHandler = this.items;
    private final ResourceHandler<ItemResource> restrictedResourceHandler = new ReadOnlyItemResourceHandler(this.items);

    private static final class ReadOnlyItemResourceHandler implements ResourceHandler<ItemResource> {
        private final ResourceHandler<ItemResource> delegate;

        private ReadOnlyItemResourceHandler(ResourceHandler<ItemResource> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public ItemResource getResource(int index) {
            return this.delegate.getResource(index);
        }

        @Override
        public long getAmountAsLong(int index) {
            return this.delegate.getAmountAsLong(index);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return this.delegate.getCapacityAsLong(index, resource);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return this.delegate.isValid(index, resource);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private static final class CageItemResourceHandler extends ItemStacksResourceHandler {
        private final MobCageBlockEntity blockEntity;

        private CageItemResourceHandler(MobCageBlockEntity blockEntity, int size) {
            super(size);
            this.blockEntity = blockEntity;
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            this.blockEntity.onInventoryContentsChanged(slot);
        }

        public int getSlots() {
            return this.size();
        }

        public ItemStack getStackInSlot(int slot) {
            ItemResource resource = this.getResource(slot);
            if (resource.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return resource.toStack(this.getAmountAsInt(slot));
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            return !stack.isEmpty() && this.isValid(slot, ItemResource.of(stack));
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int requested = stack.getCount();
            int inserted;
            try (Transaction tx = Transaction.openRoot()) {
                inserted = this.insert(slot, ItemResource.of(stack), requested, tx);
                if (!simulate && inserted > 0) {
                    tx.commit();
                }
            }

            if (inserted >= requested) {
                return ItemStack.EMPTY;
            }

            return stack.copyWithCount(requested - inserted);
        }
    }

    /**
     * Creates a new cage block entity
     * @param pos the position of the entity
     * @param state the state of the entity
     */
    public MobCageBlockEntity(BlockPos pos, BlockState state) {
        super(CagedBlockEntities.MOB_CAGE_BLOCK_ENTITY.get(), pos, state);
    }


    /**
     * Creates the item handler.
     * @return the item handler.
     */
    @Nonnull
    private CageItemResourceHandler createItemHandler(){
        return new CageItemResourceHandler(this, SLOT_COUNT);
    }

    private void onInventoryContentsChanged(int slot) {
        // Update the environment
        if(slot == ENVIRONMENT_SLOT){
            updateEnvironment();
        }else{
            calculateTotalGrowTicks();
        }
        // Notify client and server
        setChanged();
        if(level != null){
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * Returns the item handler for environment and upgrades
     * @return items handler
     */
    public ItemStacksResourceHandler getInventoryHandler(){
        return this.items;
    }

    public ResourceHandler<ItemResource> getInventoryResourceHandler() {
        return this.inventoryResourceHandler;
    }

    public ResourceHandler<ItemResource> getRestrictedResourceHandler() {
        return this.restrictedResourceHandler;
    }

    /**
     * Main method executed in every tick. Runs only on the server instance.
     * @param level the world in which the block entity is located
     * @param pos the position of the block entity
     * @param state the state of the block entity
     * @param blockEntity the block entity class
     */
    public static void tick(Level level, BlockPos pos, BlockState state, MobCageBlockEntity blockEntity) {
        //Tick only when env and mob is inside
        if(blockEntity.hasEnvAndEntity() && !blockEntity.waitingForHarvest) {
            // Check if ready to harvest
            if(blockEntity.currentGrowTicks >= blockEntity.totalGrowTicks) {
                blockEntity.attemptHarvest(state);
            }else {
                // Add one tick (if entity requires water-logging check for it)
                if(!blockEntity.entity.ifRequiresWater() || blockEntity.getBlockState().getValue(BlockStateProperties.WATERLOGGED)){
                    blockEntity.currentGrowTicks++;
                }
            }
        }
        // Check if the cage has cooking upgrade and spawn particles
        if(level != null && level.isClientSide()){
            if(!ClientConfig.disableUpgradesParticles.get()){
                for(ItemStack upgrade : blockEntity.getUpgradesAsItemStacks()){
                    blockEntity.emitUpgradeParticles(upgrade, blockEntity);
                }
            }
        }
    }

    /**
     * Drops the whole inventory on the ground.
     */
    public void dropInventory(){
        for (int i = 0; i < this.items.getSlots(); i++) {
            this.dropItem(this.items.getStackInSlot(i));
        }
    }

    /**
     * Creates a single item entity on the ground.
     * @param item item to drop
     */
    private void dropItem(ItemStack item) {
        if(this.level != null && !this.level.isClientSide()) {
            final double offsetX = (double) (level.getRandom().nextFloat() * 0.7F) + (double) 0.15F;
            final double offsetY = (double) (level.getRandom().nextFloat() * 0.7F) + (double) 0.060000002F + 0.6D;
            final double offsetZ = (double) (level.getRandom().nextFloat() * 0.7F) + (double) 0.15F;
            final ItemEntity itemEntity = new ItemEntity(this.level, this.worldPosition.getX() + offsetX, this.worldPosition.getY() + offsetY, this.worldPosition.getZ() + offsetZ, item);
            itemEntity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itemEntity);
        }
    }

    // ENVIRONMENT FUNCTIONS

    /**
     * Sets the environment data from held item.
     * @param heldItem the item to set the environment from
     */
    public void setEnvironment(ItemStack heldItem) {
        this.environmentData = getEnvironmentDataFromItemStack(heldItem);
        if(this.items.getStackInSlot(ENVIRONMENT_SLOT).isEmpty()){
            // Set the env item
            ItemStack itemstack = heldItem.copy();
            itemstack.setCount(1);
            this.items.insertItem(ENVIRONMENT_SLOT, itemstack, false);
        }
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Returns the environment data from item stack
     * @param heldItem the item stack
     * @return the environment data
     */
    public static EnvironmentData getEnvironmentDataFromItemStack(ItemStack heldItem) {
        EnvironmentData finalEnvData = null;
        for(final EnvironmentData recipe : RecipesHelper.getEnvironmentRecipesList()) {
            if(recipe != null) {
                if(recipe.getInputItem().test(heldItem)) {
                    finalEnvData = recipe;
                    break;
                }
            }
        }
        return finalEnvData;
    }

    /**
     * Removes the environment data.
     */
    public void removeEnvironment(){
        this.environmentData = null;
        // Update entity
        this.removeEntity();
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Updates the environment from currently held item.
     */
    public void updateEnvironment(){
        ItemStack envItem = this.items.getStackInSlot(ENVIRONMENT_SLOT);
        envItem.setCount(1);
        if(envItem.isEmpty()){
            this.removeEnvironment();
        }else if(this.environmentData == null
                || !this.environmentData.getInputItem().test(envItem)){
            this.setEnvironment(envItem);
        }
    }

    /**
     * Returns the environment data.
     * @return the environment data
     */
    public EnvironmentData getEnvironmentData() {
        return this.environmentData;
    }

    /**
     * Returns the current environment item stack
     * @return environment item stack
     */
    public ItemStack getEnvironmentItemStack(){
        return this.items.getStackInSlot(ENVIRONMENT_SLOT);
    }

    /**
     * Checks if the block entity has environment data.
     * @return if block entity has environment data.
     */
    public boolean hasEnvironment() {
        return !this.items.getStackInSlot(ENVIRONMENT_SLOT).isEmpty();
    }

    /**
     * Check if there exists an environment data from the given item.
     * @param heldItem item to check
     * @return if environment data exists
     */
    public static boolean existsEnvironmentFromItemStack(ItemStack heldItem) {
        // Check if the hand is empty
        if(heldItem.isEmpty()){
            return false;
        }
        // Check the recipes
        for(final Recipe<?> recipe : RecipesHelper.getEnvironmentRecipesList()) {
            if(recipe instanceof EnvironmentData envData) {
                if(envData.getInputItem().test(heldItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check the current environment is suitable for given entity type.
     * @param player player checking
     * @param entityType entity type to check
     * @param state block state
     * @return if environment is suitable
     */
    public boolean isEnvironmentSuitable(Player player, EntityType<?> entityType, BlockState state) {
        EntityData recipe = getMobDataFromType(entityType);
        // Check if entity needs waterlogged cage
        if(recipe.ifRequiresWater() && !state.getValue(BlockStateProperties.WATERLOGGED)){
            player.sendOverlayMessage(Component.translatable("block.cagedmobs.mob_cage.requiresWater").withStyle(ChatFormatting.RED));
            return false;
        }
        if(this.environmentData != null){
            for(String env : this.environmentData.getCategories()){
                if(recipe.getEnvironments().contains(env)){
                    return true;
                }
            }
        }
        player.sendOverlayMessage(Component.translatable("block.cagedmobs.mob_cage.envNotSuitable").withStyle(ChatFormatting.RED));
        return false;
    }

    // ENTITY FUNCTIONS

    /**
     * Sets the environment data from sampler item.
     * @param entityType entity type
     * @param sampler sampler item
     */
    public void setEntityFromSampler(EntityType<?> entityType, ItemStack sampler) {
        // Lookup the entity color
        if(entityType.toString().contains("sheep")){
            if(sampler.get(DataComponents.CUSTOM_DATA) != null && sampler.get(DataComponents.CUSTOM_DATA).contains("Color") ){
                CompoundTag tag = sampler.get(DataComponents.CUSTOM_DATA).copyTag();
                this.color = tag.getInt("Color").orElse(0);
            }
        }
        // Load the mob data
        this.entity = getMobDataFromType(entityType);
        this.entityType = entityType;
        // Calculate required ticks
        this.totalGrowTicks = this.calculateTotalGrowTicks();
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
    }

    /**
     * Returns the current entity data.
     * @return entity data
     */
    public Optional<EntityData> getEntity() {
        return Optional.ofNullable(this.entity);
    }

    /**
     * Returns the current entity type
     * @return entity type
     */
    public EntityType<?> getEntityType() {
        return this.entityType;
    }

    /**
     * Removes current entity.
     */
    public void removeEntity() {
        // Entity
        this.entity = null;
        this.entityType = null;
        // Entity render
        this.cachedEntity = null;
        this.renderedEntity = null;
        // Ticks
        this.currentGrowTicks = 0;
        this.totalGrowTicks = 0;
        this.waitingForHarvest = false;
        // Color
        this.color = 0;
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
    }

    /**
     * Returns if there is entity.
     * @return if there exists entity
     */
    public boolean hasEntity() {
        return this.entity != null;
    }

    /**
     * Checks if there exists a mob data from entity type
     * @param entityType the entity type to check
     * @return if there exists mob data
     */
    public boolean existsEntityDataFromType(EntityType<?> entityType) {
        for(final EntityData recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe != null) {
                // Check for null exception
                if(recipe.getEntityType() == null){continue;}
                if(recipe.getEntityType().equals(entityType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the cached entity.
     * @param level the world of the entity
     * @return the entity
     */
    public Entity getCachedEntity(Level level) {
        if (this.cachedEntity == null) {
            if(this.renderedEntity == null){
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", EntityType.getKey(this.entityType).toString());
                this.renderedEntity = new SpawnData(nbt, Optional.empty(), Optional.empty());
            }
            this.cachedEntity = EntityType.loadEntityRecursive(this.renderedEntity.getEntityToSpawn(), level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
        }
        return this.cachedEntity;
    }

    /**
     * Used to get the MobData from entity type. Also adds all additional Loot to the entity.
     * @param type entity type
     * @return mob data
     */
    private static EntityData getMobDataFromType(EntityType<?> type){
        EntityData finalEntityData = null;
        // Get the mobData
        for(final Recipe<?> recipe : RecipesHelper.getEntitiesRecipesList()) {
            if(recipe instanceof EntityData entityData) {
                // Check for null exception
                if(entityData.getEntityType() != null && entityData.getEntityType().equals(type)){
                    finalEntityData = entityData;
                    break;
                }
            }
        }
        // Add additional Loot
        if(finalEntityData != null){
            addAdditionalLootData(finalEntityData);
        }
        return finalEntityData;
    }

    /**
     * Adds all loot to the additional loot data object
     * @param entityData the object to add items to
     */
    private static void addAdditionalLootData(EntityData entityData){
        for(final Recipe<?> recipe : RecipesHelper.getAdditionalLootRecipesList()) {
            if(recipe instanceof AdditionalLootData additionalLootData) {
                // Check for null exception
                if(entityData.getEntityType() != null){
                    // If entity types are equal
                    if(entityData.getEntityType().equals(additionalLootData.getEntityType())) {
                        for(LootData data : additionalLootData.getResults()){
                            // Add loot
                            if(!additionalLootData.isRemoveFromEntity()){
                                if(!entityData.getResults().contains(data)){
                                    entityData.getResults().add(data);
                                }
                            // Remove loot
                            }else{
                                entityData.getResults().removeIf(lootData -> lootData.getItemStack().getItem().equals(data.getItemStack().getItem()));
                            }
                        }
                    }
                }
            }
        }
    }

    // UPGRADES FUNCTIONS

    /**
     * Adds a new upgrade to the cage
     * @param heldItem upgrade to add.
     */
    public void addUpgrade(ItemStack heldItem) {
        for (int slot = ENVIRONMENT_SLOT+1; slot < this.items.getSlots(); slot++) {
            // Check for empty slot
            if(this.items.getStackInSlot(slot).isEmpty()){
                ItemStack upgrade = heldItem.copy();
                upgrade.setCount(1);
                // Check if upgrade is valid
                if(this.items.isItemValid(slot, upgrade)){
                    // Insert upgrade
                    this.items.insertItem(slot, upgrade, false);
                    return;
                }
            }
        }
    }

    /**
     * Returns if the cage can accept more upgrades.
     * @return if cage can accept more upgrades.
     */
    public boolean acceptsUpgrades() {
        return this.items.getStackInSlot(ENVIRONMENT_SLOT+1).isEmpty() ||
                this.items.getStackInSlot(ENVIRONMENT_SLOT+2).isEmpty() ||
                this.items.getStackInSlot(ENVIRONMENT_SLOT+3).isEmpty();
    }

    /**
     * Returns if the cage has any upgrade.
     * @return if the cage has any upgrade
     */
    public boolean hasAnyUpgrades(){
        return !this.items.getStackInSlot(ENVIRONMENT_SLOT+1).isEmpty() ||
                !this.items.getStackInSlot(ENVIRONMENT_SLOT+2).isEmpty() ||
                !this.items.getStackInSlot(ENVIRONMENT_SLOT+3).isEmpty();
    }

    /**
     * Return the amount of a specific upgrade.
     * @param upgradeItem the upgrade to check for
     * @return the amount of specific upgrade
     */
    public int getUpgradeCount(Item upgradeItem){
        int currentCount = 0;
        for (int i = 0; i < this.items.getSlots(); i++) {
            if(this.items.getStackInSlot(i).getItem().equals(upgradeItem)){
                currentCount++;
            }
        }
        return currentCount;
    }

    public boolean hasUpgrades(Item upgradeItem, int requiredCount){
        int currentCount = 0;
        for (int i = 0; i < this.items.getSlots(); i++) {
            if(this.items.getStackInSlot(i).getItem().equals(upgradeItem)){
                currentCount++;
            }
        }
        return currentCount >= requiredCount;
    }

    /**
     * Returns a list of upgrades as ItemStacks.
     * @return list of upgrades
     */
    public List<ItemStack> getUpgradesAsItemStacks(){
        List<ItemStack> upgrades = new ArrayList<>();
        for(int slot = ENVIRONMENT_SLOT+1; slot <= UPGRADES_COUNT; slot++){
            upgrades.add(this.items.getStackInSlot(slot));
        }
        return upgrades;
    }

    /**
     * Emits particles specific for the given upgrade.
     * @param upgrade upgrade item stack
     */
    private void emitUpgradeParticles(ItemStack upgrade, MobCageBlockEntity blockEntity) {
        if(upgrade.getItem().equals(CagedItems.COOKING_UPGRADE.get())){
            emitCookingParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.LIGHTNING_UPGRADE.get())){
            emitLightningParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.ARROW_UPGRADE.get())){
            emitArrowParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.EXPERIENCE_UPGRADE.get())){
            emitExperienceParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.LOOTING_UPGRADE.get())){
            emitFortuneParticles(blockEntity);
        }else if(upgrade.getItem() instanceof SpeedIUpgradeItem ||
        upgrade.getItem() instanceof SpeedIIUpgradeItem ||
        upgrade.getItem() instanceof SpeedIIIUpgradeItem){
            emitSpeedParticles(blockEntity);
        }else if(upgrade.getItem().equals(CagedItems.CREATIVE_UPGRADE.get())){
            emitCreativeParticles(blockEntity);
        }
    }
    // HARVESTING FUNCTIONS

    /**
     * Returns if the cage is waiting for harvest
     * @return if waiting for harvest
     */
    public boolean isWaitingForHarvest() {
        return this.waitingForHarvest;
    }

    /**
     * Called when player is harvesting the cage.
     */
    public void onPlayerHarvest(BlockState state) {
        if((!state.getValue(HOPPING)|| CommonConfig.hoppingCagesDisabled.get()) && canPlayerHarvest()){
            this.currentGrowTicks = 0;
            this.waitingForHarvest = false;
            List<ItemStack> drops = createDropsList();
            for( ItemStack item : drops) {
                dropItem(item.copy());
            }
            this.setChanged();
            // Sync with client
            if(this.level != null){
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
            // Check this block as a block to be saved upon exiting the chunk
            this.setChanged();
        }
    }

    /**
     * Attempt harvest.
     * For hopping cages and an inventory bellow harvest.
     * For not hopping cages, lock and wait for players interaction.
     * @param state the block state
     */
    private void attemptHarvest(BlockState state) {
        if(state.getValue(HOPPING) && !CommonConfig.hoppingCagesDisabled.get()) {
            // Try to auto harvest
            if(this.autoHarvest()){
                this.currentGrowTicks = 0;
            }else{
                this.currentGrowTicks = this.totalGrowTicks;
            }
        }else {
            // Lock
            waitingForHarvest = true;
            this.currentGrowTicks = this.totalGrowTicks;
        }
        // Sync with client
        if(this.level != null){
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        // Check this block as a block to be saved upon exiting the chunk
        this.setChanged();
    }

    /**
     * Auto-harvests the cage, when there is a valid inventory bellow.
     * @return if the harvest happened
     */
    private boolean autoHarvest() {
        final ResourceHandler<ItemResource> inventory = getInv(this.level, this.worldPosition.below(), Direction.UP);
        if(inventory != null && !this.level.isClientSide()){
            // For every item in drop list
            NonNullList<ItemStack> drops =this.createDropsList();
            for(final ItemStack item : drops){
                // For every slot in inv
                for(int slot = 0; slot < inventory.size(); slot++){
                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = inventory.insert(slot, ItemResource.of(item), item.getCount(), tx);
                        if (inserted > 0) {
                            tx.commit();
                            // Keep legacy behavior: once any amount goes in, stop trying slots for this stack.
                            // This mirrors the previous implementation that ignored insert remainders.
                            break;
                        }
                    }
                    if (item.isEmpty()) {
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the ItemHandler from the block
     * @param world the world of the block
     * @param pos the position of the block
     * @param side the side of the block
     * @return the ItemHandler
     */
    private ResourceHandler<ItemResource> getInv(Level world, BlockPos pos, Direction side) {
        ResourceHandler<ItemResource> handler = world.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (handler != null) {
            return handler;
        }

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof WorldlyContainerHolder invProvider) {
            WorldlyContainer inv = invProvider.getContainer(state, world, pos);
            return new WorldlyContainerWrapper(inv, side);
        }

        return null;
    }


    /**
     * Creates the drop list for the entity.
     * @return a list of items to drop.
     */
    private NonNullList<ItemStack> createDropsList(){
        NonNullList<ItemStack> drops = NonNullList.create();
        List<Item> blacklistedItems = RecipesHelper.getItemsFromConfigList();
        for(LootData loot : this.entity.getResults()) {
            // Skip item if it's blacklisted or whitelisted
            if(!CommonConfig.entitiesListInWhitelistMode.get()){
                if(blacklistedItems.contains(loot.getItemStack().getItem())){
                    continue;
                }
            }else{
                if(!blacklistedItems.contains(loot.getItemStack().getItem())){
                    continue;
                }
            }
            // Choose a loot type for entity's color
            if(loot.getColor() != -1){
                if(loot.getColor() != this.color){
                    continue;
                }
            }
            // Skip if loot needs lightning upgrade, but it's not present in the cage.
            if(!this.hasUpgrades(CagedItems.LIGHTNING_UPGRADE.get(), 1) && loot.isLighting()){
                continue;
            }
            // Skip if loot needs arrow upgrade, but it's not present in the cage.
            if(!this.hasUpgrades(CagedItems.ARROW_UPGRADE.get(), 1) && loot.isArrow()){
                continue;
            }
            if(this.level != null && !this.level.isClientSide() && this.level.getRandom().nextFloat() <= loot.getChance()) {
                // Roll the amount of items
                int range = loot.getMaxAmount() - loot.getMinAmount() + 1;
                int amount = this.level.getRandom().nextInt(range) + loot.getMinAmount();
                if(amount > 0) {
                    // Add copied item stack to the drop list
                    ItemStack stack = loot.getItemStack().copy();
                    // Replace the item if there is a cooking upgrade.
                    if(this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 3) && loot.isCooking()){
                        stack = new ItemStack(Items.COAL);
                    }else if(this.hasUpgrades(CagedItems.COOKING_UPGRADE.get(), 1) && loot.isCooking()){
                        stack = loot.getCookedItemStack().copy();
                    }
                    stack.setCount(amount);
                    if(loot.ifRandomDurability()){
                        stack = applyRandomDurability(stack);
                    }
                    drops.add(stack);
                    if(this.hasUpgrades(CagedItems.LOOTING_UPGRADE.get(),1 )){
                        this.calculateFortune(loot, drops, stack);
                    }
                }
            }
        }
        // Add experience orb if the experience upgrade is present
        if(this.hasUpgrades(CagedItems.EXPERIENCE_UPGRADE.get(), 1)){
            if(this.level != null && !this.level.isClientSide() && this.level.getRandom().nextFloat() <= 0.7){
                ItemStack experienceOrbItem = new ItemStack(CagedItems.CRYSTALLIZED_EXPERIENCE.get());
                experienceOrbItem.setCount(this.getUpgradeCount(CagedItems.EXPERIENCE_UPGRADE.get()));
                drops.add(experienceOrbItem);
                // Take fortune upgrade into account
               if(this.hasUpgrades(CagedItems.LOOTING_UPGRADE.get(),1 )){
                    this.calculateFortune(null, drops, experienceOrbItem);
               }
            }
        }
        return drops;
    }

    /**
     * Takes into account the fortune upgrade, multiplying the item drop by 2,3 or 4.
     * Each fortune upgrade adds 20% chance for the fortune effect to take place.
     * @param dropList the main drop list to add items to
     * @param item item to duplicate
     */
    private void calculateFortune(@Nullable LootData lootData, NonNullList<ItemStack> dropList, ItemStack item){
        double fortuneChance = this.getUpgradeCount(CagedItems.LOOTING_UPGRADE.get()) * 0.2;
        if(this.level != null && !this.level.isClientSide() && this.level.getRandom().nextFloat() < fortuneChance){
            int countMultiplayer = this.level.getRandom().nextInt(2) + 2;
            for(int i = 0; i < countMultiplayer - 1; i++){
                if(lootData !=null && lootData.ifRandomDurability()){
                    item = applyRandomDurability(item.copy());
                }
                dropList.add(item);
            }
        }
    }

    /**
     * Applies random durability to the item stack
     * @param stack item stack
     */
    public ItemStack applyRandomDurability(ItemStack stack){
        if(this.level != null && !this.level.isClientSide() && stack.getMaxDamage() > 0){
            int randomDurability = Math.max(1,this.level.getRandom().nextInt(stack.getMaxDamage()));
            stack.setDamageValue(randomDurability);
        }
        return stack;
    }

    /**
     * Check if the cage is ready to be harvested.
     * @return if the cage is ready to be harvested
     */
    private boolean canPlayerHarvest() {
        return this.hasEnvAndEntity() && this.totalGrowTicks > 0 && this.currentGrowTicks >= this.totalGrowTicks;
    }

    /**
     * Checks if the cage has both the entity and the environment.
     * @return if the cage has both the entity and the environment
     */
    private boolean hasEnvAndEntity() {
        return this.hasEntity() && this.hasEnvironment();
    }

    /**
     * Returns the current growth percentage as float between 0.0F and 1.0F,
     * where 1.0F is the fully grown entity.
     * @return the current growth percentage
     */
    public float getGrowthPercentage() {
        if(this.totalGrowTicks != 0) {
            if(this.currentGrowTicks >= this.totalGrowTicks){
                return 1.0F;
            }else{
                return (float) this.currentGrowTicks / this.totalGrowTicks;
            }
        }else{
            return 0;
        }
    }

    /**
     * Returns the total grow ticks
     * @return total grow ticks
     */
    private int calculateTotalGrowTicks() {
        if(this.environmentData != null && this.getEntity().isPresent()){
            // Take into account creative upgrade
            if(this.hasUpgrades(CagedItems.CREATIVE_UPGRADE.get(), 1)){
                this.totalGrowTicks = 1;
                // Take into account current growth ticks
                if(this.currentGrowTicks > this.totalGrowTicks){
                    this.currentGrowTicks = this.totalGrowTicks;
                }
                return this.totalGrowTicks;
            }
            float growModifier = this.environmentData.getGrowModifier();
            // Take into account speed upgrades
            for(int i=0; i < this.getUpgradeCount(CagedItems.SPEED_I_UPGRADE.get()); i++){
                growModifier *= 1.5F;
            }
            for(int i=0; i < this.getUpgradeCount(CagedItems.SPEED_II_UPGRADE.get()); i++){
                growModifier *= 2F;
            }
            for(int i=0; i < this.getUpgradeCount(CagedItems.SPEED_III_UPGRADE.get()); i++){
                growModifier *= 3F;
            }
            // Get grow ticks from the entity
            int basicTotalGrowTicks = Math.round(this.getEntity().get().getTotalGrowTicks()/growModifier);
            // Take into account the config speed of cages
            this.totalGrowTicks = (int) Math.round(basicTotalGrowTicks/CommonConfig.cagesSpeed.get());
            // Round up the ticks to the max amount
            if(this.currentGrowTicks >= this.totalGrowTicks){
                this.currentGrowTicks = this.totalGrowTicks;
            }else{
                // If the cage was waiting for harvest and now it is not take that into account.
                this.waitingForHarvest = false;
            }
            return this.totalGrowTicks;
        }
        return 0;
    }

    // COLOR FUNCTIONS

    /**
     * Returns the color nbt of the entity
     * @return color id
     */
    public int getColor() {
        return this.color;
    }

    // SAVING AND LOADING

    /**
     * Saves all the additional tags of the block entity.
     * @param output the value output to save to
     */
    @Override
    protected void saveAdditional(@NotNull ValueOutput output){
        super.saveAdditional(output);
        this.saveTag(output);
    }

    private void saveTag(@NotNull ValueOutput output){
        // Item capability
        ValueOutput itemsOutput = output.child(ITEMS_TAG);
        items.serialize(itemsOutput);
        if (itemsOutput.isEmpty()) {
            output.discard(ITEMS_TAG);
        }
        // Put entity type
        if(this.hasEntity()){
            output.store("entityType", EntityType.CODEC, this.entityType);
        }
        // Put color
        output.putInt("color",this.color);
        // Put ticks info
        output.putInt("currentGrowTicks", this.currentGrowTicks);
        output.putBoolean("waitingForHarvest", this.waitingForHarvest);
    }

    /**
     * Loads all the additional tags of the block entity.
     * @param input the value input to load from
     */
    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.loadTag(input);
    }

    /**
     * Loads the block entity data from the tag.
     * @param input the value input to load
     */
    private void loadTag(@NotNull ValueInput input){
        // Store old env and entity type
        ItemStack oldEnv = this.items.getStackInSlot(ENVIRONMENT_SLOT);
        EntityType<?> oldEntityType = this.entityType;
        // Item capability
        input.child(ITEMS_TAG).ifPresent(items::deserialize);
        // Read the env
        this.environmentData = MobCageBlockEntity.getEnvironmentDataFromItemStack(this.items.getStackInSlot(ENVIRONMENT_SLOT));
        // Read the mob data
        this.entityType = input.read("entityType", EntityType.CODEC).orElse(null);
        this.entity = MobCageBlockEntity.getMobDataFromType(this.entityType);
        if(this.entityType == null){
            this.renderedEntity = null;
            this.cachedEntity = null;
        }
        // Read color
        this.color = input.getIntOr("color", 0);
        // Read ticks info
        this.waitingForHarvest = input.getBooleanOr("waitingForHarvest", false);
        this.currentGrowTicks = input.getIntOr("currentGrowTicks", 0);
        if(hasEntity()){
            this.totalGrowTicks = this.calculateTotalGrowTicks();
        }
        // If env or entity changed, refresh model data
        if(!Objects.equals(oldEnv, this.items.getStackInSlot(ENVIRONMENT_SLOT)) || !Objects.equals(oldEntityType,this.entityType)){
            requestModelDataUpdate();
            // Sync with client
            if(this.level != null){
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // CLIENT-SERVER SYNCHRONIZATION

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
