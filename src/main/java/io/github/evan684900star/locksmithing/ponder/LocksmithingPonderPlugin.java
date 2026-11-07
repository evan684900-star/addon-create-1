package io.github.evan684900star.locksmithing.ponder;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModItems;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Les tutos (touche W au-dessus d'un objet). Schémas dans assets/create_locksmithing/ponder/. */
public class LocksmithingPonderPlugin implements PonderPlugin {
	@Override
	public String getModId() {
		return Locksmithing.ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		helper.forComponents(ModItems.PRESSED_PLANK.getId(), ModItems.COMPRESSED_WOOD.getId())
			.addStoryBoard("compressed_wood", LocksmithingScenes::compressedWood);

		helper.forComponents(ModBlocks.MUFFLE_FURNACE.getId())
			.addStoryBoard("muffle_furnace", LocksmithingScenes::muffleFurnace);

		helper.forComponents(ModBlocks.CASTING_ANVIL.getId(), ModItems.MOLD.getId(), ModItems.COPPER_BAR.getId(),
				ModItems.IRON_BAR.getId(), ModItems.GOLD_BAR.getId(), ModItems.ZINC_BAR.getId())
			.addStoryBoard("casting_anvil", LocksmithingScenes::castingAnvil);

		helper.forComponents(ModBlocks.KEY_CUTTER.getId(), ModItems.BLANK_KEY.getId(), ModItems.SAW_BLADE.getId())
			.addStoryBoard("key_cutter", LocksmithingScenes::keyCutter);

		helper.forComponents(ModBlocks.ARMORED_COPPER_DOOR.getId(), ModItems.COPPER_KEY.getId())
			.addStoryBoard("armored_door", LocksmithingScenes::armoredDoor);
	}
}
