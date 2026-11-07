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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Machine à tailler les clés : l'enclume de Minecraft avec une lame de scie sur le dessus, au bout.
 * L'enclume est longue dans l'axe de FACING ; l'axe de rotation la traverse dans l'autre sens (sous le dessus),
 * et la lame est au bout situé du côté de FACING.
 */
public class KeyCutterBlock extends HorizontalKineticBlock implements IBE<KeyCutterBlockEntity> {
	private static final VoxelShape SHAPE_Z = Shapes.or(
		Block.box(2, 0, 2, 14, 4, 14),
		Block.box(4, 4, 3, 12, 5, 13),
		Block.box(6, 5, 4, 10, 10, 12),
		Block.box(3, 10, 0, 13, 16, 16));
	private static final VoxelShape SHAPE_X = Shapes.or(
		Block.box(2, 0, 2, 14, 4, 14),
		Block.box(3, 4, 4, 13, 5, 12),
		Block.box(4, 5, 6, 12, 10, 10),
		Block.box(0, 10, 3, 16, 16, 13));

	public KeyCutterBlock(Properties properties) {
		super(properties);
	}

	/** Centre de la lame par rapport au centre du bloc : sur le dessus, au bout côté FACING. */
	public static Vec3 sawOffset(Direction facing) {
		return new Vec3(facing.getStepX() * 3 / 16f, 7 / 16f, facing.getStepZ() * 3 / 16f);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		// Si un axe tourne déjà à côté, on oriente la machine pour s'y brancher.
		for (Direction side : Iterate.horizontalDirections) {
			BlockPos neighbourPos = pos.relative(side);
			BlockState neighbour = level.getBlockState(neighbourPos);
			if (neighbour.getBlock() instanceof IRotate rotate
				&& rotate.hasShaftTowards(level, neighbourPos, neighbour, side.getOpposite()))
				return defaultBlockState().setValue(HORIZONTAL_FACING, side.getClockWise());
		}
		// Comme l'enclume de Minecraft : on voit son grand côté en la posant
		return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getClockWise());
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HORIZONTAL_FACING).getAxis() == Axis.Z ? SHAPE_Z : SHAPE_X;
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
