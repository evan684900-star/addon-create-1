package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import com.tterrag.registrate.util.entry.ItemEntry;

import io.github.evan684900star.locksmithing.content.door.DoorMetal;
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

	// Clés vierges (alliage d'andésite + barres du métal) puis clés taillées, une par métal de porte
	public static final ItemEntry<Item> COPPER_BLANK_KEY = blankKey(DoorMetal.COPPER);
	public static final ItemEntry<Item> IRON_BLANK_KEY = blankKey(DoorMetal.IRON);
	public static final ItemEntry<Item> ZINC_BLANK_KEY = blankKey(DoorMetal.ZINC);
	public static final ItemEntry<Item> GOLD_BLANK_KEY = blankKey(DoorMetal.GOLD);
	public static final ItemEntry<Item> NETHERITE_BLANK_KEY = blankKey(DoorMetal.NETHERITE);

	public static final ItemEntry<KeyItem> COPPER_KEY = key(DoorMetal.COPPER);
	public static final ItemEntry<KeyItem> IRON_KEY = key(DoorMetal.IRON);
	public static final ItemEntry<KeyItem> ZINC_KEY = key(DoorMetal.ZINC);
	public static final ItemEntry<KeyItem> GOLD_KEY = key(DoorMetal.GOLD);
	public static final ItemEntry<KeyItem> NETHERITE_KEY = key(DoorMetal.NETHERITE);

	private static ItemEntry<Item> blankKey(DoorMetal metal) {
		return REGISTRATE.item(metal.id() + "_blank_key", Item::new)
			.properties(p -> metal.fireResistant() ? p.fireResistant() : p)
			.register();
	}

	private static ItemEntry<KeyItem> key(DoorMetal metal) {
		return REGISTRATE.item(metal.id() + "_key", KeyItem::new)
			.properties(p -> p.stacksTo(1))
			.properties(p -> metal.fireResistant() ? p.fireResistant() : p)
			.register();
	}

	/** La clé vierge de ce métal. */
	public static ItemEntry<Item> blankKeyFor(DoorMetal metal) {
		return switch (metal) {
			case COPPER -> COPPER_BLANK_KEY;
			case IRON -> IRON_BLANK_KEY;
			case ZINC -> ZINC_BLANK_KEY;
			case GOLD -> GOLD_BLANK_KEY;
			case NETHERITE -> NETHERITE_BLANK_KEY;
		};
	}

	/** La clé taillée qui ouvre les portes de ce métal. */
	public static ItemEntry<KeyItem> keyFor(DoorMetal metal) {
		return switch (metal) {
			case COPPER -> COPPER_KEY;
			case IRON -> IRON_KEY;
			case ZINC -> ZINC_KEY;
			case GOLD -> GOLD_KEY;
			case NETHERITE -> NETHERITE_KEY;
		};
	}

	public static void register() {}
}
