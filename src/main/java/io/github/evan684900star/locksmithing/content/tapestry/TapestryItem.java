package io.github.evan684900star.locksmithing.content.tapestry;

import javax.annotation.Nullable;

import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Objet tapisserie : c'est une "bannière" pour le métier à tisser (et la copie de motifs), mais elle se pose à plat
 * contre un mur, comme du papier peint (voir {@link TapestryBlock}).
 */
public class TapestryItem extends BannerItem {
	public TapestryItem(Block block, Properties properties) {
		super(block, block, properties);
	}

	@Override
	@Nullable
	protected BlockState getPlacementState(BlockPlaceContext context) {
		BlockState state = getBlock().getStateForPlacement(context);
		return state != null && canPlace(context, state) ? state : null;
	}
}
