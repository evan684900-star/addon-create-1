package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import java.util.List;

import javax.annotation.Nullable;

import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemEntry;

import io.github.evan684900star.locksmithing.content.fluid.MoltenBottleItem;
import io.github.evan684900star.locksmithing.content.fluid.MoltenMetalFluid;
import io.github.evan684900star.locksmithing.content.key.KeyItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

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

	// Fioles de métal en fusion (90 mB = une barre)
	public static final ItemEntry<MoltenBottleItem> MOLTEN_IRON_BOTTLE = bottle("iron", ModFluids.MOLTEN_IRON);
	public static final ItemEntry<MoltenBottleItem> MOLTEN_GOLD_BOTTLE = bottle("gold", ModFluids.MOLTEN_GOLD);
	public static final ItemEntry<MoltenBottleItem> MOLTEN_COPPER_BOTTLE = bottle("copper", ModFluids.MOLTEN_COPPER);
	public static final ItemEntry<MoltenBottleItem> MOLTEN_ZINC_BOTTLE = bottle("zinc", ModFluids.MOLTEN_ZINC);

	public static final List<ItemEntry<MoltenBottleItem>> BOTTLES =
		List.of(MOLTEN_IRON_BOTTLE, MOLTEN_GOLD_BOTTLE, MOLTEN_COPPER_BOTTLE, MOLTEN_ZINC_BOTTLE);

	private static ItemEntry<MoltenBottleItem> bottle(String metal, FluidEntry<MoltenMetalFluid> fluid) {
		return REGISTRATE.item("molten_" + metal + "_bottle", p -> new MoltenBottleItem(p, () -> ModFluids.source(fluid)))
			.properties(p -> p.stacksTo(16).craftRemainder(Items.GLASS_BOTTLE))
			.register();
	}

	@Nullable
	public static Item bottleFor(Fluid fluid) {
		for (ItemEntry<MoltenBottleItem> bottle : BOTTLES)
			if (bottle.get().getFluid().isSame(fluid))
				return bottle.get();
		return null;
	}

	public static void register() {}
}
