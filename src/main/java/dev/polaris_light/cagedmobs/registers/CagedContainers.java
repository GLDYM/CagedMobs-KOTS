package dev.polaris_light.cagedmobs.registers;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CagedContainers {

    public static final DeferredRegister<MenuType<?>> CAGED_MENU_TYPES_REGISTER =
            DeferredRegister.create(Registries.MENU, CagedMobs.MODID);

    // Mob Cages
    public static final DeferredHolder<MenuType<?>, MenuType<MobCageContainer>> CAGE_CONTAINER =
            CAGED_MENU_TYPES_REGISTER.register("mob_cage",
                    () -> new MenuType<>(
                            new MenuType.MenuSupplier<MobCageContainer>() {
                                @Override
                                public MobCageContainer create(int windowId, Inventory inv) {
                                    return new MobCageContainer(windowId, inv);
                                }
                            },
                            FeatureFlags.DEFAULT_FLAGS
                    ));
}
