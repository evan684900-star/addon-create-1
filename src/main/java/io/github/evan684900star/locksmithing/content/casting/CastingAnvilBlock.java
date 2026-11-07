package io.github.evan684900star.locksmithing.content.casting;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Enclume de moulage : une enclume avec un moule creusé au milieu. Clic droit = interface. */
public class CastingAnvilBlock extends HorizontalDirectionalBlock implements IBE<CastingAnvilBlockEntity>, IWrenchable {
	public static final MapCodec<CastingAnvilBlock> CODEC = simpleCodec(CastingAnvilBlock::new);

	// Forme pour une enclume orientée nord/sud (longue sur l'axe X) et est/ouest (longue sur l'axe Z).
	private static final VoxelShape SHAPE_X = Shapes.or(
		Block.box(2, 0, 2, 14, 4, 14),
		Block.box(4, 4, 3, 12, 5, 13),
		Block.box(6, 5, 4, 10, 10, 12),
		Block.box(0, 10, 3, 16, 16, 13));
	private static final VoxelShape SHAPE_Z = Shapes.or(
		Block.box(2, 0, 2, 14, 4, 14),
		Block.box(3, 4, 4, 13, 5, 12),
		Block.box(4, 5, 6, 12, 10, 10),
		Block.box(3, 10, 0, 13, 16, 16));

	public CastingAnvilBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_X : SHAPE_Z;
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
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()))
			withBlockEntityDo(level, pos, CastingAnvilBlockEntity::dropContents);
		IBE.onRemove(state, level, pos, newState);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public Class<CastingAnvilBlockEntity> getBlockEntityClass() {
		return CastingAnvilBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CastingAnvilBlockEntity> getBlockEntityType() {
		return ModBlockEntities.CASTING_ANVIL.get();
	}
}
