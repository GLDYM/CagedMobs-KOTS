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
    public final static DeferredItem<Item> MOB_CAGE = CAGED_ITEMS_REGISTER.register("mob_cage", () -> new MobCageBlockItem(CagedBlocks.MOB_CAGE.get(), new Item.Properties()));
    public final static DeferredItem<Item> HOPPING_MOB_CAGE = CAGED_ITEMS_REGISTER.register("hopping_mob_cage", () -> new MobCageBlockItem(CagedBlocks.HOPPING_MOB_CAGE.get(), new Item.Properties()));
    // SAMPLER
    public final static DeferredItem<Item> DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("dna_sampler", () -> new DnaSamplerItem(new Item.Properties().stacksTo(1)));
    public final static DeferredItem<Item> DIAMOND_DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("diamond_dna_sampler", () -> new DnaSamplerDiamondItem(new Item.Properties().stacksTo(1)));
    public final static DeferredItem<Item> NETHERITE_DNA_SAMPLER = CAGED_ITEMS_REGISTER.register("netherite_dna_sampler", () -> new DnaSamplerNetheriteItem(new Item.Properties().stacksTo(1)));
    // UPGRADES
    public final static DeferredItem<Item> SPEED_I_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_i_upgrade", () -> new SpeedIUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> SPEED_II_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_ii_upgrade", () -> new SpeedIIUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> SPEED_III_UPGRADE = CAGED_ITEMS_REGISTER.register("speed_iii_upgrade", () -> new SpeedIIIUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> LOOTING_UPGRADE = CAGED_ITEMS_REGISTER.register("looting_upgrade", () -> new LootingUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> COOKING_UPGRADE = CAGED_ITEMS_REGISTER.register("cooking_upgrade", () -> new CookingUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> LIGHTNING_UPGRADE = CAGED_ITEMS_REGISTER.register("lightning_upgrade", () -> new LightningUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> ARROW_UPGRADE = CAGED_ITEMS_REGISTER.register("arrow_upgrade", () -> new ArrowUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> EXPERIENCE_UPGRADE = CAGED_ITEMS_REGISTER.register("experience_upgrade", () -> new ExperienceUpgradeItem(new Item.Properties()));
    public final static DeferredItem<Item> CREATIVE_UPGRADE = CAGED_ITEMS_REGISTER.register("creative_upgrade", () -> new CreativeUpgradeItem(new Item.Properties()));

    // // MISC
    public final static DeferredItem<Item> DRAGON_SCALE = CAGED_ITEMS_REGISTER.register("dragon_scale", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public final static DeferredItem<Item> NETHER_STAR_FRAGMENT = CAGED_ITEMS_REGISTER.register("nether_star_fragment", () -> new NetherStarFragmentItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public final static DeferredItem<Item> WARDEN_RECEPTOR = CAGED_ITEMS_REGISTER.register("warden_receptor", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public final static DeferredItem<Item> SPONGE_FRAGMENT = CAGED_ITEMS_REGISTER.register("sponge_fragment", () -> new Item(new Item.Properties()));
    public final static DeferredItem<Item> HONEY_DROP = CAGED_ITEMS_REGISTER.register("honey_drop", () -> new Item(new Item.Properties()));
    public final static DeferredItem<Item> MILK_DROP = CAGED_ITEMS_REGISTER.register("milk_drop", () -> new Item(new Item.Properties()));
    public final static DeferredItem<Item> CRYSTALLIZED_EXPERIENCE = CAGED_ITEMS_REGISTER.register("crystallized_experience", () -> new CrystallizedExperienceItem(new Item.Properties()));
    public final static DeferredItem<Item> EMPTY_SPAWN_EGG = CAGED_ITEMS_REGISTER.register("empty_spawn_egg", () -> new EmptySpawnEggItem(new Item.Properties()));
    public final static DeferredItem<Item> STAR_INFUSED_NETHERITE_INGOT = CAGED_ITEMS_REGISTER.register("star_infused_netherite_ingot", () -> new StarInfusedNetheriteIngotItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public final static DeferredItem<Item> STAR_INFUSED_NETHERITE_NUGGET = CAGED_ITEMS_REGISTER.register("star_infused_netherite_nugget", () -> new StarInfusedNetheriteNuggetItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // BLOCKS
    public final static DeferredItem<Item> STAR_INFUSED_NETHERITE_BLOCK = CAGED_ITEMS_REGISTER.register("star_infused_netherite_block", () -> new StarInfusedNetheriteBlockItem(CagedBlocks.STAR_INFUSED_NETHERITE_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public final static DeferredItem<Item> CRYSTALLIZED_EXPERIENCE_BLOCK = CAGED_ITEMS_REGISTER.register("crystallized_experience_block", () -> new CrystallizedExperienceBlockItem(CagedBlocks.CRYSTALLIZED_EXPERIENCE_BLOCK.get(), new Item.Properties()));
}
