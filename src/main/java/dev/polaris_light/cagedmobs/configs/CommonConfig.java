package dev.polaris_light.cagedmobs.configs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;


public class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder()
        .comment("Server side config for CagedMobs. If changed it will affect the whole server!")
        .push("server");

    public static final ModConfigSpec.BooleanValue hoppingCagesDisabled = BUILDER
        .comment("Disables Hopping Cages' automatic harvest, making them work the same as the non-hopping variant.")
        .define("hoppingCagesDisabled", false);

    public static final ModConfigSpec.BooleanValue entitiesListInWhitelistMode = BUILDER
        .comment("Whether the entities list is in whitelist mode (true) or blacklist mode (false)")
        .define("entitiesListInWhitelistMode", false);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> entitiesList = BUILDER
        .comment("The list of entities to be affected by the caged mobs system")
        .defineListAllowEmpty("entitiesList", List.of(), () -> "", CommonConfig::validateEntityName);

    public static final ModConfigSpec.BooleanValue itemsListInWhitelistMode = BUILDER
        .comment("Whether the items list is in whitelist mode (true) or blacklist mode (false)")
        .define("itemsListInWhitelistMode", false);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> itemsList = BUILDER
        .comment("The list of items to be affected by the caged mobs system")
        .defineListAllowEmpty("itemsList", List.of(), () -> "", CommonConfig::validateItemName);
        
    public static final ModConfigSpec.BooleanValue singleUseSamplers = BUILDER
        .comment("Makes all samplers (all tiers) only single use. After a mob is sampled and put into the cage the sampler will break.")
        .define("singleUseSamplers", false);
    public static final ModConfigSpec.BooleanValue disableSamplers = BUILDER
        .comment("Disables all samplers, requiring the player to use only the spawn eggs.")
        .define("disableSamplers", false);
    public static final ModConfigSpec.BooleanValue disableSpawnEggs = BUILDER
        .comment("Disables the ability to use spawn eggs on mob cages, requiring the player to use only the samplers.")
        .define("disableSpawnEggs", false);

    public static final ModConfigSpec.DoubleValue cagesSpeed = BUILDER
        .comment("Sets the speed of all cages. The bigger the value the faster the cages will work (by default: 1.00).")
        .defineInRange("cagesSpeed",1.00,0.01,100.00);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    private static boolean validateEntityName(final Object obj) {
        return obj instanceof String entityName && BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(entityName));
    }
}
