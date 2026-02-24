package dev.polaris_light.cagedmobs.addons.jade;

import dev.polaris_light.cagedmobs.CagedMobs;
import dev.polaris_light.cagedmobs.blocks.mob_cage.MobCageBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public class HideContainerItemsProvider implements IServerExtensionProvider<ItemStack> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CagedMobs.MODID,"hide_container_items");

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
        return List.of();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }


}
