package io.github.evan684900star.locksmithing.content.tapestry;

import com.mojang.serialization.MapCodec;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tapisserie en cuir : se pose au mur comme une bannière et se décore au métier à tisser de Minecraft.
 * Elle reprend la forme de la bannière murale, avec un rendu en cuir (voir {@link TapestryRenderer}).
 */
public class TapestryBlock extends WallBannerBlock {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final MapCodec<WallBannerBlock> CODEC = (MapCodec) simpleCodec(TapestryBlock::new);

	public TapestryBlock(Properties properties) {
		super(DyeColor.BROWN, properties);
	}

	@Override
	public MapCodec<WallBannerBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return ModBlockEntities.TAPESTRY.create(pos, state);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(pos) instanceof TapestryBlockEntity tapestry ? tapestry.getItem()
			: super.getCloneItemStack(level, pos, state);
	}
}
