package dev.polaris_light.cagedmobs.registers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.*;
import dev.polaris_light.cagedmobs.blocks.mob_cage.HoppingMobCageBlock;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

public class CagedBlocks {
    // Registries
    public static final DeferredRegister.Blocks CAGED_BLOCKS_REGISTER = DeferredRegister.createBlocks(CagedMobs.MODID);

    // CAGES
    public final static DeferredBlock<Block> MOB_CAGE = CAGED_BLOCKS_REGISTER.register("mob_cage", () -> new MobCageBlock(Block.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops()));
    public final static DeferredBlock<Block> HOPPING_MOB_CAGE = CAGED_BLOCKS_REGISTER.register("hopping_mob_cage", () -> new HoppingMobCageBlock(Block.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops()));
    // BLOCKS
    public final static DeferredBlock<Block> STAR_INFUSED_NETHERITE_BLOCK = CAGED_BLOCKS_REGISTER.register("star_infused_netherite_block", () -> new StarInfusedNetheriteBlock(Block.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F)));
    public final static DeferredBlock<Block> CRYSTALLIZED_EXPERIENCE_BLOCK = CAGED_BLOCKS_REGISTER.register("crystallized_experience_block", () -> new CrystallizedExperienceBlock(Block.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.SLIME_BLOCK).noOcclusion()));
}
