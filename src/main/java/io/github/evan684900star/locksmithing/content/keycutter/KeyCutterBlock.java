package io.github.evan684900star.locksmithing.content.keycutter;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Machine à tailler les clés : une enclume avec une lame de scie au bout.
 * L'axe d'entrée arrive à l'arrière, la lame tourne à l'avant (côté FACING).
 */
public class KeyCutterBlock extends HorizontalKineticBlock implements IBE<KeyCutterBlockEntity> {
	// Corps + lame, pour une machine orientée nord/sud (axe Z) et est/ouest (axe X).
	private static final VoxelShape SHAPE_Z = Shapes.or(
		Block.box(2, 0, 2, 14, 4, 14),
		Block.box(5, 4, 2, 11, 10, 14),
		Block.box(1, 10, 2, 15, 14, 14),
		Block.box(1, 1, 0, 15, 15, 2),
		Block.box(1, 1, 14, 15, 15, 16));
	private static final VoxelShape SHAPE_X = Shapes.or(
		Block.box(2, 0, 2, 14, 4, 14),
		Block.box(2, 4, 5, 14, 10, 11),
		Block.box(2, 10, 1, 14, 14, 15),
		Block.box(0, 1, 1, 2, 15, 15),
		Block.box(14, 1, 1, 16, 15, 15));

	public KeyCutterBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		// Si un axe tourne déjà à côté, on place la machine pour s'y brancher (l'axe entre par l'arrière).
		for (Direction side : Iterate.horizontalDirections) {
			BlockPos neighbourPos = pos.relative(side);
			BlockState neighbour = level.getBlockState(neighbourPos);
			if (neighbour.getBlock() instanceof IRotate rotate
				&& rotate.hasShaftTowards(level, neighbourPos, neighbour, side.getOpposite()))
				return defaultBlockState().setValue(HORIZONTAL_FACING, side.getOpposite());
		}
		return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getRotationAxis(state) == Axis.Z ? SHAPE_Z : SHAPE_X;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
		BlockHitResult hitResult) {
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		withBlockEntityDo(level, pos, be -> {
			if (player instanceof ServerPlayer serverPlayer)
				serverPlayer.openMenu(be, pos);
		});
		return InteractionResult.CONSUME;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()))
			withBlockEntityDo(level, pos, KeyCutterBlockEntity::dropContents);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public Class<KeyCutterBlockEntity> getBlockEntityClass() {
		return KeyCutterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends KeyCutterBlockEntity> getBlockEntityType() {
		return ModBlockEntities.KEY_CUTTER.get();
	}
}
