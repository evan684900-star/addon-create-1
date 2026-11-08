package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import com.simibubi.create.api.stress.BlockStressValues;
import com.tterrag.registrate.util.entry.BlockEntry;

import io.github.evan684900star.locksmithing.content.casting.CastingAnvilBlock;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlock;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorItem;
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlock;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterBlock;
import io.github.evan684900star.locksmithing.content.tapestry.TapestryBlock;
import io.github.evan684900star.locksmithing.content.tapestry.TapestryItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {
	/**
	 * Porte en cuivre blindée. Dureté 168 : 42 s avec une hache en fer (minimum pour la récupérer),
	 * plus rapide avec une meilleure hache ou Efficacité. Résiste aux explosions comme l'obsidienne.
	 */
	public static final BlockEntry<ArmoredDoorBlock> ARMORED_COPPER_DOOR =
		REGISTRATE.block("armored_copper_door", p -> new ArmoredDoorBlock(p, ModItems.COPPER_KEY))
			.initialProperties(() -> Blocks.COPPER_DOOR)
			.properties(p -> p.mapColor(MapColor.COLOR_ORANGE)
				.strength(168f, 1200f)
				.requiresCorrectToolForDrops()
				.sound(SoundType.COPPER)
				.noOcclusion()
				.pushReaction(PushReaction.BLOCK))
			.item(ArmoredDoorItem::new)
			.build()
			.register();

	public static final BlockEntry<MuffleFurnaceBlock> MUFFLE_FURNACE =
		REGISTRATE.block("muffle_furnace", MuffleFurnaceBlock::new)
			.initialProperties(() -> Blocks.BLAST_FURNACE)
			.properties(p -> p.mapColor(MapColor.COLOR_GRAY)
				.strength(3.5f)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> state.getValue(MuffleFurnaceBlock.LIT) ? 13 : 0))
			.simpleItem()
			.register();

	public static final BlockEntry<CastingAnvilBlock> CASTING_ANVIL =
		REGISTRATE.block("casting_anvil", CastingAnvilBlock::new)
			.initialProperties(() -> Blocks.ANVIL)
			.properties(p -> p.mapColor(MapColor.METAL)
				.strength(5f, 1200f)
				.requiresCorrectToolForDrops()
				.sound(SoundType.ANVIL)
				.noOcclusion())
			.simpleItem()
			.register();

	public static final BlockEntry<KeyCutterBlock> KEY_CUTTER =
		REGISTRATE.block("key_cutter", KeyCutterBlock::new)
			.initialProperties(() -> Blocks.ANVIL)
			.properties(p -> p.mapColor(MapColor.METAL)
				.strength(5f, 1200f)
				.requiresCorrectToolForDrops()
				.sound(SoundType.ANVIL)
				.noOcclusion())
			.onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 4.0))
			.simpleItem()
			.register();

	/** Tapisserie en cuir : se pose au mur, se décore au métier à tisser comme une bannière. */
	public static final BlockEntry<TapestryBlock> TAPESTRY =
		REGISTRATE.block("tapestry", TapestryBlock::new)
			.properties(p -> p.mapColor(MapColor.COLOR_BROWN)
				.forceSolidOn()
				.noCollission()
				.strength(1f)
				.sound(SoundType.WOOL)
				.ignitedByLava())
			.item(TapestryItem::new)
			.properties(p -> p.stacksTo(16))
			.build()
			.register();

	public static void register() {}
}
