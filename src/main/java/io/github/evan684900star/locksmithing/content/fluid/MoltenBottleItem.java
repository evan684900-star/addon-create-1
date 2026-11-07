package io.github.evan684900star.locksmithing.content.fluid;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

/** Fiole contenant exactement une barre de métal en fusion (90 mB). */
public class MoltenBottleItem extends Item {
	private final Supplier<? extends Fluid> fluid;

	public MoltenBottleItem(Properties properties, Supplier<? extends Fluid> fluid) {
		super(properties);
		this.fluid = fluid;
	}

	public Fluid getFluid() {
		return fluid.get();
	}
}
