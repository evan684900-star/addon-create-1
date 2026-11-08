package io.github.evan684900star.locksmithing.content.tapestry;

import net.minecraft.world.item.BannerItem;
import net.minecraft.world.level.block.Block;

/** Objet tapisserie : c'est une "bannière" pour le métier à tisser, qui ne se pose qu'au mur. */
public class TapestryItem extends BannerItem {
	public TapestryItem(Block block, Properties properties) {
		super(block, block, properties);
	}
}
