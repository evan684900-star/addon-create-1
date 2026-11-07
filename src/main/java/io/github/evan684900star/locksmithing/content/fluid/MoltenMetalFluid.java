package io.github.evan684900star.locksmithing.content.fluid;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Fluide sans bloc dans le monde (comme les fluides "virtuels" de Create), mais avec un seau. */
public class MoltenMetalFluid extends BaseFlowingFluid {
	private final boolean source;

	public static MoltenMetalFluid source(Properties properties) {
		return new MoltenMetalFluid(properties, true);
	}

	public static MoltenMetalFluid flowing(Properties properties) {
		return new MoltenMetalFluid(properties, false);
	}

	protected MoltenMetalFluid(Properties properties, boolean source) {
		super(properties);
		this.source = source;
	}

	@Override
	public Fluid getSource() {
		return source ? this : super.getSource();
	}

	@Override
	public Fluid getFlowing() {
		return source ? super.getFlowing() : this;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSource(FluidState state) {
		return source;
	}

	@Override
	public int getAmount(FluidState state) {
		return 0;
	}
}
