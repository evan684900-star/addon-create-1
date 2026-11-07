package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

import io.github.evan684900star.locksmithing.content.casting.CastingAnvilBlockEntity;
import io.github.evan684900star.locksmithing.content.casting.CastingAnvilRenderer;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlockEntity;
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlockEntity;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterBlockEntity;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterRenderer;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterVisual;

public class ModBlockEntities {
	public static final BlockEntityEntry<ArmoredDoorBlockEntity> ARMORED_DOOR =
		REGISTRATE.blockEntity("armored_door", ArmoredDoorBlockEntity::new)
			.validBlocks(ModBlocks.ARMORED_COPPER_DOOR)
			.register();

	public static final BlockEntityEntry<MuffleFurnaceBlockEntity> MUFFLE_FURNACE =
		REGISTRATE.blockEntity("muffle_furnace", MuffleFurnaceBlockEntity::new)
			.validBlocks(ModBlocks.MUFFLE_FURNACE)
			.register();

	public static final BlockEntityEntry<CastingAnvilBlockEntity> CASTING_ANVIL =
		REGISTRATE.blockEntity("casting_anvil", CastingAnvilBlockEntity::new)
			.validBlocks(ModBlocks.CASTING_ANVIL)
			.renderer(() -> CastingAnvilRenderer::new)
			.register();

	public static final BlockEntityEntry<KeyCutterBlockEntity> KEY_CUTTER =
		REGISTRATE.blockEntity("key_cutter", KeyCutterBlockEntity::new)
			.visual(() -> KeyCutterVisual::new, true)
			.validBlocks(ModBlocks.KEY_CUTTER)
			.renderer(() -> KeyCutterRenderer::new)
			.register();

	public static void register() {}
}
