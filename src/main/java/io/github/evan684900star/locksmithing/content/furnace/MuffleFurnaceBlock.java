package io.github.evan684900star.locksmithing.content.furnace;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Four à moufle : posé sur un Brûleur à Blaze allumé, il fait fondre les lingots. */
public class MuffleFurnaceBlock extends HorizontalDirectionalBlock implements IBE<MuffleFurnaceBlockEntity>, IWrenchable {
	public static final MapCodec<MuffleFurnaceBlock> CODEC = simpleCodec(MuffleFurnaceBlock::new);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public MuffleFurnaceBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		return onBlockEntityUseItemOn(level, pos, be -> be.onUse(player, hand, stack));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
		BlockHitResult hitResult) {
		if (!player.isShiftKeyDown())
			return InteractionResult.PASS;
		return onBlockEntityUse(level, pos, be -> be.takeIngots(player));
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()))
			withBlockEntityDo(level, pos, MuffleFurnaceBlockEntity::dropContents);
		IBE.onRemove(state, level, pos, newState);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT))
			return;
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.3;
		double z = pos.getZ() + 0.5;
		if (random.nextDouble() < 0.1)
			level.playLocalSound(x, y, z, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		Direction facing = state.getValue(FACING);
		double offset = 0.52;
		double side = random.nextDouble() * 0.5 - 0.25;
		double px = x + facing.getStepX() * offset + (facing.getAxis() == Direction.Axis.Z ? side : 0);
		double pz = z + facing.getStepZ() * offset + (facing.getAxis() == Direction.Axis.X ? side : 0);
		level.addParticle(ParticleTypes.SMOKE, px, y + random.nextDouble() * 0.3, pz, 0, 0, 0);
		if (random.nextDouble() < 0.3)
			level.addParticle(ParticleTypes.SMALL_FLAME, px, y + random.nextDouble() * 0.2, pz, 0, 0, 0);
	}

	@Override
	public Class<MuffleFurnaceBlockEntity> getBlockEntityClass() {
		return MuffleFurnaceBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends MuffleFurnaceBlockEntity> getBlockEntityType() {
		return ModBlockEntities.MUFFLE_FURNACE.get();
	}
}
