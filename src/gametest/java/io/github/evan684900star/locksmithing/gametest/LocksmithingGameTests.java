package io.github.evan684900star.locksmithing.gametest;

import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.casting.CastingAnvilBlockEntity;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlock;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlockEntity;
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlockEntity;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterBlockEntity;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModDataComponents;
import io.github.evan684900star.locksmithing.registry.ModFluids;
import io.github.evan684900star.locksmithing.registry.ModItems;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tests lancés par {@code ./gradlew runGameTestServer} (et sur GitHub Actions). */
@GameTestHolder(Locksmithing.ID)
@PrefixGameTestTemplate(false)
public class LocksmithingGameTests {

	@GameTest(template = "empty")
	public static void recipesAreLoaded(GameTestHelper helper) {
		Level level = helper.getLevel();
		for (ItemStack ingot : new ItemStack[] { new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
			new ItemStack(Items.COPPER_INGOT), AllItems.ZINC_INGOT.asStack() })
			helper.assertTrue(ModRecipeTypes.findMelting(level, ingot).isPresent(), "No melting recipe for " + ingot);
		helper.assertFalse(ModRecipeTypes.findMelting(level, AllItems.BRASS_INGOT.asStack()).isPresent(),
			"Brass should not melt");
		for (var fluid : ModFluids.ALL)
			helper.assertTrue(ModRecipeTypes.findCasting(level, new FluidStack(ModFluids.source(fluid), 90)).isPresent(),
				"No casting recipe for " + fluid.getId());
		helper.assertTrue(ModRecipeTypes.findKeyCutting(level, ModItems.BLANK_KEY.asStack()).size() == 1,
			"The blank key should have exactly one key to cut");
		for (String id : new String[] { "pressing/pressed_plank", "compacting/compressed_wood", "crafting/mold",
			"crafting/casting_anvil", "crafting/saw_blade", "crafting/key_cutter", "crafting/blank_key",
			"crafting/armored_copper_door", "crafting/muffle_furnace", "crafting/tapestry" })
			helper.assertTrue(level.getRecipeManager().byKey(Locksmithing.asResource(id)).isPresent(), "Missing recipe " + id);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 400)
	public static void furnaceMeltsIngots(GameTestHelper helper) {
		BlockPos burner = new BlockPos(2, 1, 2);
		BlockPos furnace = burner.above();
		helper.setBlock(burner, AllBlocks.BLAZE_BURNER.getDefaultState()
			.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED));
		BlazeBurnerBlockEntity burnerBe = helper.getBlockEntity(burner);
		burnerBe.isCreative = true;
		helper.setBlock(furnace, ModBlocks.MUFFLE_FURNACE.getDefaultState());
		MuffleFurnaceBlockEntity be = helper.getBlockEntity(furnace);
		ItemStack rest = be.getItemCapability().insertItem(0, new ItemStack(Items.IRON_INGOT, 2), false);
		helper.assertTrue(rest.isEmpty(), "The furnace should accept iron ingots");
		helper.assertFalse(be.getItemCapability().insertItem(0, new ItemStack(Items.DIRT), true).isEmpty(),
			"The furnace should refuse dirt");
		helper.succeedWhen(() -> {
			FluidStack fluid = be.getFluid();
			helper.assertTrue(fluid.is(ModFluids.source(ModFluids.MOLTEN_IRON)) && fluid.getAmount() == 180,
				"Expected 180 mB of molten iron, got " + fluid.getAmount());
			helper.assertTrue(helper.getBlockState(furnace).getValue(
				io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlock.LIT), "Furnace should be lit");
		});
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void furnaceNeedsHeat(GameTestHelper helper) {
		BlockPos furnace = new BlockPos(2, 1, 2);
		helper.setBlock(furnace, ModBlocks.MUFFLE_FURNACE.getDefaultState());
		MuffleFurnaceBlockEntity be = helper.getBlockEntity(furnace);
		be.getItemCapability().insertItem(0, new ItemStack(Items.IRON_INGOT), false);
		helper.runAfterDelay(120, () -> {
			helper.assertTrue(be.getFluid().isEmpty(), "Nothing should melt without a Blaze Burner");
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 300)
	public static void castingAnvilCastsBars(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.CASTING_ANVIL.getDefaultState());
		CastingAnvilBlockEntity be = helper.getBlockEntity(pos);
		int filled = be.getFluidCapability()
			.fill(new FluidStack(ModFluids.source(ModFluids.MOLTEN_COPPER), 180), IFluidHandler.FluidAction.EXECUTE);
		helper.assertTrue(filled == 180, "The mold should accept 180 mB of molten copper");
		helper.assertTrue(be.getFluidCapability()
				.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100), IFluidHandler.FluidAction.SIMULATE) == 0,
			"The mold should refuse water");
		helper.succeedWhen(() -> {
			ItemStack out = be.getOutput();
			helper.assertTrue(out.is(ModItems.COPPER_BAR.get()) && out.getCount() == 2,
				"Expected 2 copper bars, got " + out);
			helper.assertTrue(be.getFluid().isEmpty(), "The mold should be empty");
		});
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void castingAnvilEmptiesBuckets(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.CASTING_ANVIL.getDefaultState());
		CastingAnvilBlockEntity be = helper.getBlockEntity(pos);
		ItemStack bucket = new ItemStack(ModFluids.MOLTEN_IRON.get().getBucket());
		helper.assertTrue(be.getItemCapability().insertItem(0, bucket, false).isEmpty(),
			"The mold should accept a molten iron bucket");
		helper.succeedWhen(() -> {
			helper.assertTrue(be.getItemCapability().getStackInSlot(0).is(Items.BUCKET), "The bucket should be emptied");
			helper.assertTrue(be.getOutput().is(ModItems.IRON_BAR.get()), "An iron bar should be cast");
		});
	}

	@GameTest(template = "empty", timeoutTicks = 600)
	public static void keyCutterCutsUniqueKeys(GameTestHelper helper) {
		// Machine orientée nord : l'axe la traverse d'ouest en est, le moteur est à l'ouest
		BlockPos motor = new BlockPos(1, 1, 2);
		BlockPos cutter = new BlockPos(2, 1, 2);
		helper.setBlock(cutter, ModBlocks.KEY_CUTTER.getDefaultState()
			.setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.NORTH));
		helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState()
			.setValue(CreativeMotorBlock.FACING, Direction.EAST));
		KeyCutterBlockEntity be = helper.getBlockEntity(cutter);
		be.getItemCapability().insertItem(0, ModItems.BLANK_KEY.asStack(2), false);
		UUID[] first = new UUID[1];
		helper.startSequence()
			.thenWaitUntil(() -> helper.assertTrue(be.getItemCapability().getStackInSlot(1).is(ModItems.COPPER_KEY.get()),
				"Waiting for the first key"))
			.thenExecute(() -> {
				ItemStack key = be.getItemCapability().extractItem(1, 1, false);
				first[0] = key.get(ModDataComponents.KEY_ID.get());
				helper.assertTrue(first[0] != null, "A cut key must have an id");
			})
			.thenWaitUntil(() -> helper.assertTrue(be.getItemCapability().getStackInSlot(1).is(ModItems.COPPER_KEY.get()),
				"Waiting for the second key"))
			.thenExecute(() -> {
				UUID second = be.getItemCapability().getStackInSlot(1).get(ModDataComponents.KEY_ID.get());
				helper.assertTrue(second != null && !second.equals(first[0]), "Two cut keys must be different");
			})
			.thenSucceed();
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void keyCutterNeedsRotation(GameTestHelper helper) {
		BlockPos cutter = new BlockPos(2, 1, 2);
		helper.setBlock(cutter, ModBlocks.KEY_CUTTER.getDefaultState());
		KeyCutterBlockEntity be = helper.getBlockEntity(cutter);
		be.getItemCapability().insertItem(0, ModItems.BLANK_KEY.asStack(), false);
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(be.getItemCapability().getStackInSlot(1).isEmpty(), "No key without rotation");
			helper.succeed();
		});
	}

	@GameTest(template = "empty")
	public static void doorLocksToFirstKey(GameTestHelper helper) {
		BlockPos lower = new BlockPos(2, 1, 2);
		placeDoor(helper, lower);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		ItemStack keyA = keyWithId(UUID.randomUUID());
		ItemStack keyB = keyWithId(UUID.randomUUID());

		// À la main : rien
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		helper.getBlockState(lower).useWithoutItem(helper.getLevel(), player, hit(helper, lower));
		helper.assertFalse(isOpen(helper, lower), "The door must not open by hand");

		// Première clé : elle devient la clé de la porte
		use(helper, player, lower, keyA);
		helper.assertTrue(isOpen(helper, lower), "The first key should open the door");
		helper.assertTrue(isOpen(helper, lower.above()), "Both halves should open");
		ArmoredDoorBlockEntity lock = helper.getBlockEntity(lower);
		helper.assertTrue(keyA.get(ModDataComponents.KEY_ID.get()).equals(lock.getLockId()), "The lock should remember key A");

		// Autre clé : refusée
		use(helper, player, lower.above(), keyB);
		helper.assertTrue(isOpen(helper, lower), "Key B must not change the door");

		// La bonne clé referme (en cliquant sur la moitié haute)
		use(helper, player, lower.above(), keyA);
		helper.assertFalse(isOpen(helper, lower), "Key A should close the door");

		// Redstone : rien
		helper.setBlock(lower.east(), Blocks.REDSTONE_BLOCK);
		helper.assertFalse(isOpen(helper, lower), "Redstone must not open the door");

		// Casser la porte remet la serrure à zéro
		helper.setBlock(lower.east(), Blocks.AIR);
		helper.setBlock(lower.above(), Blocks.AIR);
		helper.setBlock(lower, Blocks.AIR);
		placeDoor(helper, lower);
		use(helper, player, lower, keyB);
		helper.assertTrue(isOpen(helper, lower), "After breaking, the first key used (B) should own the door");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 100)
	public static void doorSwingAnimationEnds(GameTestHelper helper) {
		BlockPos lower = new BlockPos(2, 1, 2);
		placeDoor(helper, lower);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		use(helper, player, lower, keyWithId(UUID.randomUUID()));
		helper.assertTrue(isOpen(helper, lower), "The key should open the door");
		helper.assertFalse(helper.getBlockState(lower).getValue(ArmoredDoorBlock.VISIBLE), "The door is animating");
		helper.assertFalse(helper.getBlockState(lower.above()).getValue(ArmoredDoorBlock.VISIBLE), "Both halves animate");
		helper.succeedWhen(() -> {
			helper.assertTrue(helper.getBlockState(lower).getValue(ArmoredDoorBlock.VISIBLE), "Lower half visible again");
			helper.assertTrue(helper.getBlockState(lower.above()).getValue(ArmoredDoorBlock.VISIBLE), "Upper half visible again");
			helper.assertTrue(isOpen(helper, lower), "Still open");
		});
	}

	@GameTest(template = "empty")
	public static void doorWaxAndOxidation(GameTestHelper helper) {
		BlockPos lower = new BlockPos(2, 1, 2);
		placeDoor(helper, lower);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		use(helper, player, lower, new ItemStack(Items.HONEYCOMB));
		helper.assertTrue(helper.getBlockState(lower).getValue(ArmoredDoorBlock.WAXED), "Lower half should be waxed");
		helper.assertTrue(helper.getBlockState(lower.above()).getValue(ArmoredDoorBlock.WAXED), "Upper half should be waxed");
		helper.assertFalse(helper.getBlockState(lower).isRandomlyTicking(), "A waxed door must not oxidize");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void doorTakes42SecondsWithIronAxe(GameTestHelper helper) {
		BlockState door = ModBlocks.ARMORED_COPPER_DOOR.getDefaultState();
		float hardness = door.getDestroySpeed(helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
		ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
		helper.assertTrue(ironAxe.isCorrectToolForDrops(door), "An iron axe should harvest the door");
		helper.assertFalse(new ItemStack(Items.STONE_AXE).isCorrectToolForDrops(door), "A stone axe must not harvest it");
		helper.assertFalse(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(door), "It is mined with an axe");
		float perTick = ironAxe.getDestroySpeed(door) / hardness / 30f;
		int ticks = (int) Math.ceil(1f / perTick);
		helper.assertTrue(ticks == 840, "Expected 840 ticks (42 s) with an iron axe, got " + ticks);
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void tapestryWorksWithLoomAndKeepsPatterns(GameTestHelper helper) {
		// Le métier à tisser accepte tout BannerItem
		helper.assertTrue(ModBlocks.TAPESTRY.asStack().getItem() instanceof net.minecraft.world.item.BannerItem,
			"The tapestry must be usable in a Loom");
		helper.assertTrue("block.create_locksmithing.tapestry".equals(ModBlocks.TAPESTRY.asStack().getDescriptionId()),
			"The tapestry needs its own name");
		BlockPos wall = new BlockPos(2, 1, 2);
		BlockPos pos = wall.south();
		helper.setBlock(wall, Blocks.STONE_BRICKS);
		helper.setBlock(pos, ModBlocks.TAPESTRY.getDefaultState()
			.setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.SOUTH));
		var registry = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN);
		var patterns = new net.minecraft.world.level.block.entity.BannerPatternLayers.Builder()
			.add(registry.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.BORDER),
				net.minecraft.world.item.DyeColor.RED)
			.build();
		ItemStack decorated = ModBlocks.TAPESTRY.asStack();
		decorated.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, patterns);
		io.github.evan684900star.locksmithing.content.tapestry.TapestryBlockEntity be = helper.getBlockEntity(pos);
		be.applyComponentsFromItemStack(decorated);
		helper.assertTrue(be.getPatterns().equals(patterns), "The tapestry should take the item's patterns");
		helper.assertTrue(patterns.equals(be.getItem().get(net.minecraft.core.component.DataComponents.BANNER_PATTERNS)),
			"Picking or breaking it should give back the patterns");
		helper.succeed();
	}

	/* Outils */

	private static void placeDoor(GameTestHelper helper, BlockPos lower) {
		BlockState state = ModBlocks.ARMORED_COPPER_DOOR.getDefaultState().setValue(DoorBlock.FACING, Direction.NORTH);
		helper.setBlock(lower, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
		helper.setBlock(lower.above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
	}

	private static ItemStack keyWithId(UUID id) {
		ItemStack key = ModItems.COPPER_KEY.asStack();
		key.set(ModDataComponents.KEY_ID.get(), id);
		return key;
	}

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos) {
		BlockPos abs = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false);
	}

	private static void use(GameTestHelper helper, Player player, BlockPos pos, ItemStack stack) {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		helper.getBlockState(pos).useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, pos));
	}

	private static boolean isOpen(GameTestHelper helper, BlockPos pos) {
		return helper.getBlockState(pos).getValue(DoorBlock.OPEN);
	}
}
