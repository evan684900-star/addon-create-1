package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import java.util.List;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.fluid.MoltenBucketItem;
import io.github.evan684900star.locksmithing.content.fluid.MoltenMetalFluid;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.SoundActions;

public class ModFluids {
	public static final FluidEntry<MoltenMetalFluid> MOLTEN_IRON = molten("iron");
	public static final FluidEntry<MoltenMetalFluid> MOLTEN_GOLD = molten("gold");
	public static final FluidEntry<MoltenMetalFluid> MOLTEN_COPPER = molten("copper");
	public static final FluidEntry<MoltenMetalFluid> MOLTEN_ZINC = molten("zinc");

	public static final List<FluidEntry<MoltenMetalFluid>> ALL = List.of(MOLTEN_IRON, MOLTEN_GOLD, MOLTEN_COPPER, MOLTEN_ZINC);

	/**
	 * Métal en fusion : se met en seau, en fiole et passe dans les tuyaux/réservoirs de Create,
	 * mais ne peut pas être posé dans le monde (pas de bloc liquide).
	 */
	private static FluidEntry<MoltenMetalFluid> molten(String metal) {
		String name = "molten_" + metal;
		return REGISTRATE.virtualFluid(name,
				Locksmithing.asResource("block/fluid/" + name + "_still"),
				Locksmithing.asResource("block/fluid/" + name + "_flow"),
				CreateRegistrate::defaultFluidType, MoltenMetalFluid::source, MoltenMetalFluid::flowing)
			.properties(p -> p.temperature(1300)
				.lightLevel(12)
				.density(3000)
				.viscosity(6000)
				.canSwim(false)
				.canDrown(false)
				.canPushEntity(false)
				.canExtinguish(false)
				.canConvertToSource(false)
				.supportsBoating(false)
				.canHydrate(false)
				.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
				.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA))
			.bucket(MoltenBucketItem::new)
			.build()
			.register();
	}

	/** Registrate renvoie le fluide "qui coule" avec get() : les recettes et réservoirs utilisent la source. */
	public static MoltenMetalFluid source(FluidEntry<MoltenMetalFluid> entry) {
		return entry.getSource();
	}

	public static void register() {}
}
