package net.insideseras.flyticketmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.insideseras.flyticketmod.FlyTicketMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


public class modItems {
    public static final Item RAINBOW_PAPER = registerItem("rainbow_paper", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(FlyTicketMod.MOD_ID, name), item);
    }

    public static void registerModItems(){
        FlyTicketMod.LOGGER.info("Registering Mod Items for " + FlyTicketMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(RAINBOW_PAPER);
        });
    }
}

