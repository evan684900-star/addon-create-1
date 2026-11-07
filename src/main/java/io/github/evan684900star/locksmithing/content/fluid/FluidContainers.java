package io.github.evan684900star.locksmithing.content.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Petites fonctions pour passer d'un seau à du fluide et inversement. */
public final class FluidContainers {
	/** Une barre = un lingot = 90 mB. */
	public static final int INGOT = 90;
	public static final int BUCKET = FluidType.BUCKET_VOLUME;

	private FluidContainers() {}

	/** Le fluide contenu dans un seau plein (vide sinon). */
	public static FluidStack getContained(ItemStack stack) {
		if (stack.isEmpty())
			return FluidStack.EMPTY;
		return FluidUtil.getFluidContained(stack.copyWithCount(1))
			.orElse(FluidStack.EMPTY);
	}

	/** Le seau vide qui reste après avoir vidé {@code filled}. */
	public static ItemStack getEmptied(ItemStack filled) {
		ItemStack remainder = filled.getCraftingRemainingItem();
		return remainder.isEmpty() ? new ItemStack(Items.BUCKET) : remainder;
	}

	public static boolean isEmptyContainer(ItemStack stack) {
		return stack.is(Items.BUCKET);
	}

	/** Combien de fluide il faut pour remplir ce récipient vide (0 si ce n'en est pas un). */
	public static int capacityOf(ItemStack empty) {
		return empty.is(Items.BUCKET) ? BUCKET : 0;
	}

	/** Le seau plein obtenu en remplissant {@code empty} avec {@code fluid} (vide si impossible). */
	public static ItemStack fill(ItemStack empty, Fluid fluid) {
		if (!empty.is(Items.BUCKET))
			return ItemStack.EMPTY;
		Item bucket = fluid.getBucket();
		return bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
	}
}
