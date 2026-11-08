package io.github.evan684900star.locksmithing.content.tapestry;

import com.mojang.serialization.MapCodec;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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

	private String descriptionId;

	public TapestryBlock(Properties properties) {
		super(DyeColor.BROWN, properties);
	}

	/**
	 * La bannière murale de Minecraft prend le nom de son objet (la bannière debout), mais ici l'objet est
	 * la tapisserie elle-même : on reprend donc le nom normal d'un bloc pour ne pas tourner en boucle.
	 */
	@Override
	public String getDescriptionId() {
		if (descriptionId == null)
			descriptionId = Util.makeDescriptionId("block", BuiltInRegistries.BLOCK.getKey(this));
		return descriptionId;
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
