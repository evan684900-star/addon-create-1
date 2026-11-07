package io.github.evan684900star.locksmithing.content.fluid;

import io.github.evan684900star.locksmithing.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Petites fonctions pour passer d'un seau/d'une fiole à du fluide et inversement. */
public final class FluidContainers {
	/** Une barre = un lingot = une fiole = 90 mB. */
	public static final int INGOT = 90;
	public static final int BOTTLE = INGOT;
	public static final int BUCKET = FluidType.BUCKET_VOLUME;

	private FluidContainers() {}

	/** Le fluide contenu dans un seau ou une fiole pleine (vide sinon). */
	public static FluidStack getContained(ItemStack stack) {
		if (stack.isEmpty())
			return FluidStack.EMPTY;
		if (stack.getItem() instanceof MoltenBottleItem bottle)
			return new FluidStack(bottle.getFluid(), BOTTLE);
		return FluidUtil.getFluidContained(stack.copyWithCount(1))
			.orElse(FluidStack.EMPTY);
	}

	/** Le récipient vide qui reste après avoir vidé {@code filled} (seau ou fiole). */
	public static ItemStack getEmptied(ItemStack filled) {
		if (filled.getItem() instanceof MoltenBottleItem)
			return new ItemStack(Items.GLASS_BOTTLE);
		ItemStack remainder = filled.getCraftingRemainingItem();
		return remainder.isEmpty() ? new ItemStack(Items.BUCKET) : remainder;
	}

	public static boolean isEmptyContainer(ItemStack stack) {
		return stack.is(Items.BUCKET) || stack.is(Items.GLASS_BOTTLE);
	}

	/** Combien de fluide il faut pour remplir ce récipient vide (0 si ce n'en est pas un). */
	public static int capacityOf(ItemStack empty) {
		if (empty.is(Items.BUCKET))
			return BUCKET;
		if (empty.is(Items.GLASS_BOTTLE))
			return BOTTLE;
		return 0;
	}

	/** Le récipient plein obtenu en remplissant {@code empty} avec {@code fluid} (vide si impossible). */
	public static ItemStack fill(ItemStack empty, Fluid fluid) {
		if (empty.is(Items.BUCKET)) {
			Item bucket = fluid.getBucket();
			return bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
		}
		if (empty.is(Items.GLASS_BOTTLE)) {
			Item bottle = ModItems.bottleFor(fluid);
			return bottle == null ? ItemStack.EMPTY : new ItemStack(bottle);
		}
		return ItemStack.EMPTY;
	}
}
