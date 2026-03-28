package dev.polaris_light.cagedmobs.registers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedBlockEntities {
    //Registry
    public static final DeferredRegister<BlockEntityType<?>> CAGED_BLOCK_ENTITIES_REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CagedMobs.MODID);

    // Cage Entity
    public final static DeferredHolder<BlockEntityType<?>, BlockEntityType<MobCageBlockEntity>> MOB_CAGE_BLOCK_ENTITY = CAGED_BLOCK_ENTITIES_REGISTER.register("mob_cage", 
        () -> new BlockEntityType<>(MobCageBlockEntity::new, CagedBlocks.MOB_CAGE.get(), CagedBlocks.HOPPING_MOB_CAGE.get()));
}
