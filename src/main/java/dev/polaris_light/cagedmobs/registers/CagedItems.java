package dev.polaris_light.cagedmobs.registers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.items.*;
import dev.polaris_light.cagedmobs.items.upgrades.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
// import net.minecraft.world.item.SimpleFoiledItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class CagedItems {
    //Registry
    public static final DeferredRegister.Items CAGED_ITEMS_REGISTER = DeferredRegister.createItems(CagedMobs.MODID);

    // // CAGES
    public final static DeferredItem<Item> MOB_CAGE = CAGED_ITEMS_REGISTER.registerItem("mob_cage", props -> new MobCageBlockItem(CagedBlocks.MOB_CAGE.get(), props));
    public final static DeferredItem<Item> HOPPING_MOB_CAGE = CAGED_ITEMS_REGISTER.registerItem("hopping_mob_cage", props -> new MobCageBlockItem(CagedBlocks.HOPPING_MOB_CAGE.get(), props));
    // SAMPLER
    public final static DeferredItem<Item> DNA_SAMPLER = CAGED_ITEMS_REGISTER.registerItem("dna_sampler", props -> new DnaSamplerItem(props.stacksTo(1)));
    public final static DeferredItem<Item> DIAMOND_DNA_SAMPLER = CAGED_ITEMS_REGISTER.registerItem("diamond_dna_sampler", props -> new DnaSamplerDiamondItem(props.stacksTo(1)));
    public final static DeferredItem<Item> NETHERITE_DNA_SAMPLER = CAGED_ITEMS_REGISTER.registerItem("netherite_dna_sampler", props -> new DnaSamplerNetheriteItem(props.stacksTo(1)));
    // UPGRADES
    public final static DeferredItem<Item> SPEED_I_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("speed_i_upgrade", props -> new SpeedIUpgradeItem(props));
    public final static DeferredItem<Item> SPEED_II_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("speed_ii_upgrade", props -> new SpeedIIUpgradeItem(props));
    public final static DeferredItem<Item> SPEED_III_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("speed_iii_upgrade", props -> new SpeedIIIUpgradeItem(props));
    public final static DeferredItem<Item> LOOTING_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("looting_upgrade", props -> new LootingUpgradeItem(props));
    public final static DeferredItem<Item> COOKING_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("cooking_upgrade", props -> new CookingUpgradeItem(props));
    public final static DeferredItem<Item> LIGHTNING_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("lightning_upgrade", props -> new LightningUpgradeItem(props));
    public final static DeferredItem<Item> ARROW_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("arrow_upgrade", props -> new ArrowUpgradeItem(props));
    public final static DeferredItem<Item> EXPERIENCE_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("experience_upgrade", props -> new ExperienceUpgradeItem(props));
    public final static DeferredItem<Item> CREATIVE_UPGRADE = CAGED_ITEMS_REGISTER.registerItem("creative_upgrade", props -> new CreativeUpgradeItem(props));

    // // MISC
    public final static DeferredItem<Item> DRAGON_SCALE = CAGED_ITEMS_REGISTER.registerItem("dragon_scale", props -> new Item(props.rarity(Rarity.EPIC)));
    public final static DeferredItem<Item> NETHER_STAR_FRAGMENT = CAGED_ITEMS_REGISTER.registerItem("nether_star_fragment", props -> new NetherStarFragmentItem(props.rarity(Rarity.UNCOMMON)));
    public final static DeferredItem<Item> WARDEN_RECEPTOR = CAGED_ITEMS_REGISTER.registerItem("warden_receptor", props -> new Item(props.rarity(Rarity.EPIC)));
    public final static DeferredItem<Item> SPONGE_FRAGMENT = CAGED_ITEMS_REGISTER.registerItem("sponge_fragment", props -> new Item(props));
    public final static DeferredItem<Item> HONEY_DROP = CAGED_ITEMS_REGISTER.registerItem("honey_drop", props -> new Item(props));
    public final static DeferredItem<Item> MILK_DROP = CAGED_ITEMS_REGISTER.registerItem("milk_drop", props -> new Item(props));
    public final static DeferredItem<Item> CRYSTALLIZED_EXPERIENCE = CAGED_ITEMS_REGISTER.registerItem("crystallized_experience", props -> new CrystallizedExperienceItem(props));
    public final static DeferredItem<Item> EMPTY_SPAWN_EGG = CAGED_ITEMS_REGISTER.registerItem("empty_spawn_egg", props -> new EmptySpawnEggItem(props));
    public final static DeferredItem<Item> STAR_INFUSED_NETHERITE_INGOT = CAGED_ITEMS_REGISTER.registerItem("star_infused_netherite_ingot", props -> new StarInfusedNetheriteIngotItem(props.rarity(Rarity.UNCOMMON)));
    public final static DeferredItem<Item> STAR_INFUSED_NETHERITE_NUGGET = CAGED_ITEMS_REGISTER.registerItem("star_infused_netherite_nugget", props -> new StarInfusedNetheriteNuggetItem(props.rarity(Rarity.UNCOMMON)));

    // BLOCKS
    public final static DeferredItem<Item> STAR_INFUSED_NETHERITE_BLOCK = CAGED_ITEMS_REGISTER.registerItem("star_infused_netherite_block", props -> new StarInfusedNetheriteBlockItem(CagedBlocks.STAR_INFUSED_NETHERITE_BLOCK.get(), props.rarity(Rarity.UNCOMMON)));
    public final static DeferredItem<Item> CRYSTALLIZED_EXPERIENCE_BLOCK = CAGED_ITEMS_REGISTER.registerItem("crystallized_experience_block", props -> new CrystallizedExperienceBlockItem(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), props));
}
