package dev.polaris_light.cagedmobs.configs;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder()
        .comment("Client side config for CagedMobs. Do not change when shipping in ModPacks!")
        .push("client");

    public static final ModConfigSpec.BooleanValue disableEnvsRender = BUILDER
        .comment("Disables environments rendering inside cages to save on performance.")
        .define("disableEnvsRender", false);
    public static final ModConfigSpec.BooleanValue disableEntitiesRender = BUILDER
        .comment("Disables entities models rendering inside cages to save on performance.")
        .define("disableEntitiesRender", false);
     public static final ModConfigSpec.BooleanValue disableGrowthRender = BUILDER
        .comment("Disables entities growth progress rendering inside cages and keeps the size of the entity inside the cage always the same.")
        .define("disableGrowthRender", false);
     public static final ModConfigSpec.BooleanValue disableUpgradesParticles = BUILDER
        .comment("Disables particles emitted by the cage upgrades")
        .define("disableUpgradesParticles", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

}