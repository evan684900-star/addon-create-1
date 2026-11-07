package io.github.evan684900star.locksmithing.content.fluid;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;

/** Seau de métal en fusion : impossible de le vider par terre. */
public class MoltenBucketItem extends BucketItem {
	public MoltenBucketItem(Fluid content, Properties properties) {
		super(content, properties);
	}

	@Override
	public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result,
		@Nullable ItemStack container) {
		return false;
	}
}
