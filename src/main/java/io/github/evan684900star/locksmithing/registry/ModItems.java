package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import com.tterrag.registrate.util.entry.ItemEntry;

import io.github.evan684900star.locksmithing.content.key.KeyItem;
import net.minecraft.world.item.Item;

public class ModItems {
	// Bois compressé : planche -> (presse) planche pressée -> (presse + bassin, x4) bois compressé
	public static final ItemEntry<Item> PRESSED_PLANK = REGISTRATE.item("pressed_plank", Item::new).register();
	public static final ItemEntry<Item> COMPRESSED_WOOD = REGISTRATE.item("compressed_wood", Item::new).register();

	// Barres sorties de l'enclume de moulage
	public static final ItemEntry<Item> IRON_BAR = REGISTRATE.item("iron_bar", Item::new).register();
	public static final ItemEntry<Item> GOLD_BAR = REGISTRATE.item("gold_bar", Item::new).register();
	public static final ItemEntry<Item> COPPER_BAR = REGISTRATE.item("copper_bar", Item::new).register();
	public static final ItemEntry<Item> ZINC_BAR = REGISTRATE.item("zinc_bar", Item::new).register();

	// Composants
	public static final ItemEntry<Item> MOLD = REGISTRATE.item("mold", Item::new).register();
	public static final ItemEntry<Item> SAW_BLADE = REGISTRATE.item("saw_blade", Item::new).register();
	public static final ItemEntry<Item> BLANK_KEY = REGISTRATE.item("blank_key", Item::new).register();

	// Clés taillées
	public static final ItemEntry<KeyItem> COPPER_KEY = REGISTRATE.item("copper_key", KeyItem::new)
		.properties(p -> p.stacksTo(1))
		.register();

	public static void register() {}
}
