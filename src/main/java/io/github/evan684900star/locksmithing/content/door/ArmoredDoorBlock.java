package io.github.evan684900star.locksmithing.content.door;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;

import io.github.evan684900star.locksmithing.content.key.KeyItem;
import io.github.evan684900star.locksmithing.content.tapestry.TapestryItem;
import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import io.github.evan684900star.locksmithing.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;

/**
 * Porte blindée :
 * - ne s'ouvre ni à la main, ni à la redstone, uniquement avec une clé ;
 * - la première clé utilisée dessus devient la seule qui l'ouvre (remis à zéro si la porte est cassée) ;
 * - s'oxyde comme le cuivre, se protège avec un rayon de miel et se gratte à la hache.
 */
public class ArmoredDoorBlock extends DoorBlock implements IBE<ArmoredDoorBlockEntity> {
	public static final EnumProperty<WeatherState> OXIDATION = EnumProperty.create("oxidation", WeatherState.class);
	public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
	/** Faux pendant l'animation d'ouverture/fermeture : le modèle fixe est caché, le rendu animé prend le relais. */
	public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
	/** Une tapisserie remplace le bois de la porte (elle est gardée dans la moitié basse). */
	public static final BooleanProperty TAPESTRY = BooleanProperty.create("tapestry");

	public static final BlockSetType ARMORED_COPPER = BlockSetType.register(new BlockSetType(
		"create_locksmithing:armored_copper", false, false, false, BlockSetType.PressurePlateSensitivity.MOBS,
		net.minecraft.world.level.block.SoundType.COPPER, SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
		SoundEvents.COPPER_TRAPDOOR_CLOSE, SoundEvents.COPPER_TRAPDOOR_OPEN, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
		SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.COPPER_BULB_TURN_OFF, SoundEvents.COPPER_BULB_TURN_ON));

	public static final MapCodec<ArmoredDoorBlock> CODEC =
		simpleCodec(p -> new ArmoredDoorBlock(p, DoorMetal.COPPER, () -> Items.AIR));

	private final DoorMetal metal;
	private final Supplier<? extends Item> key;

	public ArmoredDoorBlock(Properties properties, DoorMetal metal, Supplier<? extends Item> key) {
		super(ARMORED_COPPER, properties);
		this.metal = metal;
		this.key = key;
		registerDefaultState(defaultBlockState().setValue(OXIDATION, WeatherState.UNAFFECTED)
			.setValue(WAXED, false)
			.setValue(TAPESTRY, false)
			.setValue(VISIBLE, true));
	}

	@Override
	@SuppressWarnings("unchecked")
	public MapCodec<? extends DoorBlock> codec() {
		return CODEC;
	}

	public DoorMetal getMetal() {
		return metal;
	}

	/** La clé (taillée) qui rentre dans cette serrure. */
	public Item getKeyItem() {
		return key.get();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OXIDATION, WAXED, TAPESTRY, VISIBLE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		// Une porte blindée posée près de la redstone reste fermée.
		return state == null ? null
			: state.setValue(OPEN, false).setValue(POWERED, false).setValue(TAPESTRY, false).setValue(VISIBLE, true);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
		boolean isMoving) {
		// Pas d'ouverture à la redstone.
	}

	/* Serrure */

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
		BlockHitResult hitResult) {
		if (!player.getMainHandItem().isEmpty())
			return InteractionResult.PASS;
		if (!level.isClientSide) {
			player.displayClientMessage(Component.translatable("message.create_locksmithing.door.need_key"), true);
			level.playSound(null, pos, ModSounds.DOOR_LOCKED.get(), SoundSource.BLOCKS, 0.8f, 1f);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (stack.is(Items.HONEYCOMB) && metal.oxidizes())
			return wax(stack, state, level, pos, player);
		if (stack.getItem() instanceof KeyItem)
			return useKey(stack, state, level, pos, player);
		if (stack.getItem() instanceof TapestryItem)
			return putTapestry(stack, state, level, pos, player);
		if (stack.is(Tags.Items.TOOLS_SHEAR) && state.getValue(TAPESTRY))
			return takeTapestry(stack, state, level, pos, player, hand, hitResult);
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/* Tapisserie */

	/** Clic droit avec une tapisserie : elle remplace le bois de la porte, des deux côtés. */
	private ItemInteractionResult putTapestry(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player) {
		ArmoredDoorBlockEntity door = getLock(level, pos, state);
		if (door == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (state.getValue(TAPESTRY)) {
			if (!level.isClientSide)
				player.displayClientMessage(Component.translatable("message.create_locksmithing.door.has_tapestry"), true);
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		if (!level.isClientSide) {
			door.setTapestry(stack.copyWithCount(1));
			// La moitié du dessus recopie l'état (DoorBlock.updateShape).
			level.setBlock(pos, state.setValue(TAPESTRY, true), 3);
			level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 1f, 0.9f);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			stack.consume(1, player);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	/** Clic droit avec des cisailles : la tapisserie est rendue (avec ses motifs), le bois revient. */
	private ItemInteractionResult takeTapestry(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		ArmoredDoorBlockEntity door = getLock(level, pos, state);
		if (door == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!level.isClientSide) {
			ItemStack tapestry = door.removeTapestry();
			level.setBlock(pos, state.setValue(TAPESTRY, false), 3);
			if (!tapestry.isEmpty())
				popResourceFromFace(level, pos, hitResult.getDirection(), tapestry);
			level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1f, 1f);
			level.gameEvent(player, GameEvent.SHEAR, pos);
			stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	/** Casser la porte rend aussi la tapisserie (la moitié basse garde la tapisserie et la serrure). */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> drops = super.getDrops(state, params);
		if (state.getValue(TAPESTRY)
			&& params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ArmoredDoorBlockEntity door
			&& !door.getTapestry().isEmpty()) {
			drops = new ArrayList<>(drops);
			drops.add(door.getTapestry().copy());
		}
		return drops;
	}

	private ItemInteractionResult useKey(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;

		if (!stack.is(getKeyItem())) {
			player.displayClientMessage(Component.translatable("message.create_locksmithing.door.wrong_key_type"), true);
			level.playSound(null, pos, ModSounds.DOOR_LOCKED.get(), SoundSource.BLOCKS, 0.8f, 1f);
			return ItemInteractionResult.CONSUME;
		}

		ArmoredDoorBlockEntity lock = getLock(level, pos, state);
		if (lock == null)
			return ItemInteractionResult.CONSUME;

		UUID keyId = KeyItem.getOrCreateId(stack);
		if (!lock.isBound()) {
			lock.bind(keyId);
			player.displayClientMessage(
				Component.translatable("message.create_locksmithing.door.bound", KeyItem.shortCode(keyId)), true);
			level.playSound(null, pos, ModSounds.KEY_BIND.get(), SoundSource.BLOCKS, 0.8f, 1f);
		} else if (!keyId.equals(lock.getLockId())) {
			player.displayClientMessage(Component.translatable("message.create_locksmithing.door.wrong_key"), true);
			level.playSound(null, pos, ModSounds.DOOR_LOCKED.get(), SoundSource.BLOCKS, 0.8f, 1f);
			return ItemInteractionResult.CONSUME;
		}

		boolean opening = !isOpen(state);
		level.playSound(null, pos, (opening ? ModSounds.DOOR_UNLOCK : ModSounds.DOOR_LOCK).get(), SoundSource.BLOCKS, 1f, 1f);
		swing(player, level, state, pos, opening);
		return ItemInteractionResult.CONSUME;
	}

	/* Animation */

	/**
	 * Pendant l'ouverture ou la fermeture, la forme (contour, collisions) suit la porte qui tourne au lieu de
	 * sauter tout de suite à la position finale.
	 */
	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (!state.getValue(VISIBLE)) {
			BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
			if (level.getBlockEntity(lowerPos) instanceof ArmoredDoorBlockEntity door) {
				BlockState closed = state.setValue(OPEN, false).setValue(VISIBLE, true);
				return DoorSwing.shape(closed, DoorSwing.ease(door.getShapeProgress()));
			}
		}
		return super.getShape(state, level, pos, context);
	}

	/** Durée de l'ouverture / de la fermeture, en ticks (20 = 1 seconde). */
	public static final int SWING_TICKS = 20;

	@Override
	public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
		if (state.is(this) && state.getValue(OPEN) != open)
			swing(entity, level, state, pos, open);
	}

	/** Ouvre ou ferme la porte avec l'animation : le modèle fixe revient quand la porte a fini de tourner. */
	public void swing(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
		if (state.getValue(OPEN) == open)
			return;
		level.setBlock(pos, state.setValue(OPEN, open).setValue(VISIBLE, false), 10);
		level.playSound(null, pos, open ? type().doorOpen() : type().doorClose(), SoundSource.BLOCKS, 1f,
			level.getRandom().nextFloat() * 0.1f + 0.9f);
		level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
		ArmoredDoorBlockEntity lock = getLock(level, pos, state);
		if (lock != null)
			lock.startSwing(open, level.getGameTime() + SWING_TICKS + 2);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level level, BlockState state, BlockEntityType<S> type) {
		if (level.isClientSide || type != getBlockEntityType())
			return null;
		return (lvl, pos, st, be) -> ((ArmoredDoorBlockEntity) be).serverTick();
	}

	/** La serrure est dans la moitié basse de la porte. */
	@Nullable
	public ArmoredDoorBlockEntity getLock(Level level, BlockPos pos, BlockState state) {
		BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
		return getBlockEntity(level, lowerPos);
	}

	/* Oxydation */

	private ItemInteractionResult wax(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
		if (state.getValue(WAXED))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!level.isClientSide) {
			level.setBlock(pos, state.setValue(WAXED, true), 11);
			level.levelEvent(player, 3003, pos, 0);
			if (!player.getAbilities().instabuild)
				stack.shrink(1);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility,
		boolean simulate) {
		if (!metal.oxidizes())
			return super.getToolModifiedState(state, context, itemAbility, simulate);
		if (itemAbility == ItemAbilities.AXE_WAX_OFF && state.getValue(WAXED))
			return state.setValue(WAXED, false);
		if (itemAbility == ItemAbilities.AXE_SCRAPE && !state.getValue(WAXED)) {
			WeatherState previous = previous(state.getValue(OXIDATION));
			if (previous != null)
				return state.setValue(OXIDATION, previous);
		}
		return super.getToolModifiedState(state, context, itemAbility, simulate);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return metal.oxidizes() && state.getValue(HALF) == DoubleBlockHalf.LOWER && !state.getValue(WAXED)
			&& state.getValue(OXIDATION) != WeatherState.OXIDIZED;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		WeatherState current = state.getValue(OXIDATION);
		WeatherState next = next(current);
		if (next == null)
			return;
		// Même vitesse moyenne qu'un bloc de cuivre isolé.
		float chance = 0.05688889F * (current == WeatherState.UNAFFECTED ? 0.75F : 1F);
		if (random.nextFloat() < chance)
			level.setBlockAndUpdate(pos, state.setValue(OXIDATION, next));
	}

	@Nullable
	private static WeatherState next(WeatherState state) {
		return switch (state) {
			case UNAFFECTED -> WeatherState.EXPOSED;
			case EXPOSED -> WeatherState.WEATHERED;
			case WEATHERED -> WeatherState.OXIDIZED;
			case OXIDIZED -> null;
		};
	}

	@Nullable
	private static WeatherState previous(WeatherState state) {
		return switch (state) {
			case UNAFFECTED -> null;
			case EXPOSED -> WeatherState.UNAFFECTED;
			case WEATHERED -> WeatherState.EXPOSED;
			case OXIDIZED -> WeatherState.WEATHERED;
		};
	}

	/* Block entity : uniquement dans la moitié basse */

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? getBlockEntityType().create(pos, state) : null;
	}

	@Override
	public Class<ArmoredDoorBlockEntity> getBlockEntityClass() {
		return ArmoredDoorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ArmoredDoorBlockEntity> getBlockEntityType() {
		return ModBlockEntities.ARMORED_DOOR.get();
	}
}
