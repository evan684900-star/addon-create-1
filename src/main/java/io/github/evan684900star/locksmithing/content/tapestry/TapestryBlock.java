package io.github.evan684900star.locksmithing.content.tapestry;

import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Tapisserie en cuir : collée à plat contre un mur comme du papier peint, 1 bloc de large et 2 de haut (le format
 * des motifs du métier à tisser). On la pose sur un mur : elle part du bloc visé et monte d'un bloc.
 * La moitié basse garde les motifs (voir {@link TapestryBlockEntity}) ; le rendu est dans {@link TapestryRenderer}.
 */
public class TapestryBlock extends AbstractBannerBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final MapCodec<TapestryBlock> CODEC = simpleCodec(TapestryBlock::new);

	/** 1 pixel d'épaisseur contre le mur, selon le côté vers lequel elle est tournée. */
	private static final Map<Direction, VoxelShape> SHAPES = Map.of(
		Direction.NORTH, Block.box(0, 0, 15, 16, 16, 16),
		Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 1),
		Direction.WEST, Block.box(15, 0, 0, 16, 16, 16),
		Direction.EAST, Block.box(0, 0, 0, 1, 16, 16));

	/** Une "bannière" couleur cuir : c'est ce que le métier à tisser demande. */
	public TapestryBlock(Properties properties) {
		super(DyeColor.BROWN, properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected MapCodec<? extends AbstractBannerBlock> codec() {
		return CODEC;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		// Le modèle ne sert qu'aux particules ; le panneau est dessiné par le rendu du bloc.
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	/* Pose */

	/** Sur un mur, en commençant par celui qu'on vise ; il faut aussi la place au-dessus. */
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context))
			return null;
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isVertical())
				continue;
			BlockState state = defaultBlockState().setValue(FACING, direction.getOpposite());
			if (state.canSurvive(level, pos) && state.setValue(HALF, DoubleBlockHalf.UPPER).canSurvive(level, pos.above()))
				return state;
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	/** Il faut un mur plein derrière chaque moitié. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos wall = pos.relative(facing.getOpposite());
		return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
		BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		// L'autre moitié a disparu : celle-ci tombe aussi
		if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
			boolean other = neighbor.is(this) && neighbor.getValue(HALF) != half
				&& neighbor.getValue(FACING) == state.getValue(FACING);
			return other ? state : Blocks.AIR.defaultBlockState();
		}
		if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos))
			return Blocks.AIR.defaultBlockState();
		return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
	}

	/** En créatif, casser le haut ne fait rien tomber (comme les portes). */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos below = pos.below();
			BlockState lower = level.getBlockState(below);
			if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
				level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
				level.levelEvent(player, 2001, below, Block.getId(lower));
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	/* Motifs */

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? ModBlockEntities.TAPESTRY.create(pos, state) : null;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
		return level.getBlockEntity(lower) instanceof TapestryBlockEntity tapestry ? tapestry.getItem()
			: super.getCloneItemStack(level, pos, state);
	}
}
