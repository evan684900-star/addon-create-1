package io.github.evan684900star.locksmithing.ponder;

import java.util.ArrayList;
import java.util.List;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.door.DoorMetal;
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

		List<ResourceLocation> cutter = new ArrayList<>(List.of(ModBlocks.KEY_CUTTER.getId(), ModItems.SAW_BLADE.getId()));
		List<ResourceLocation> doors = new ArrayList<>();
		for (DoorMetal metal : DoorMetal.values()) {
			cutter.add(ModItems.blankKeyFor(metal).getId());
			doors.add(ModItems.keyFor(metal).getId());
		}
		for (var door : ModBlocks.ARMORED_DOORS)
			doors.add(door.getId());
		helper.forComponents(cutter).addStoryBoard("key_cutter", LocksmithingScenes::keyCutter);
		helper.forComponents(doors).addStoryBoard("armored_door", LocksmithingScenes::armoredDoor);
	}
}
