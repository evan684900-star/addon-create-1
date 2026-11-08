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
import io.github.evan684900star.locksmithing.content.door.DoorMetal;
import io.github.evan684900star.locksmithing.content.door.DoorSwing;
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlockEntity;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterBlockEntity;
import io.github.evan684900star.locksmithing.content.tapestry.TapestryBlock;
import io.github.evan684900star.locksmithing.content.tapestry.TapestryBlockEntity;
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
		for (DoorMetal metal : DoorMetal.values()) {
			var cuts = ModRecipeTypes.findKeyCutting(level, ModItems.blankKeyFor(metal).asStack());
			helper.assertTrue(cuts.size() == 1 && cuts.get(0).value().result().is(ModItems.keyFor(metal).get()),
				"The blank " + metal.id() + " key should cut into a " + metal.id() + " key");
			for (String id : new String[] { "crafting/" + metal.id() + "_blank_key", "crafting/armored_" + metal.id() + "_door" })
				helper.assertTrue(level.getRecipeManager().byKey(Locksmithing.asResource(id)).isPresent(), "Missing recipe " + id);
		}
		for (String id : new String[] { "pressing/pressed_plank", "compacting/compressed_wood", "crafting/mold",
			"crafting/casting_anvil", "crafting/saw_blade", "crafting/key_cutter", "crafting/muffle_furnace",
			"crafting/tapestry" })
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
		be.getItemCapability().insertItem(0, ModItems.COPPER_BLANK_KEY.asStack(2), false);
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
		be.getItemCapability().insertItem(0, ModItems.COPPER_BLANK_KEY.asStack(), false);
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
	public static void doorBreakTimeDependsOnMetal(GameTestHelper helper) {
		int[] expected = { 42, 56, 70, 84 };
		for (int i = 0; i < ModBlocks.ARMORED_DOORS.length; i++) {
			BlockState door = ModBlocks.ARMORED_DOORS[i].getDefaultState();
			float hardness = door.getDestroySpeed(helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
			ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
			helper.assertTrue(ironAxe.isCorrectToolForDrops(door), "An iron axe should harvest " + door);
			helper.assertFalse(new ItemStack(Items.STONE_AXE).isCorrectToolForDrops(door), "A stone axe must not harvest it");
			helper.assertFalse(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(door), "It is mined with an axe");
			float perTick = ironAxe.getDestroySpeed(door) / hardness / 30f;
			int ticks = (int) Math.ceil(1f / perTick);
			helper.assertTrue(ticks == expected[i] * 20,
				"Expected " + expected[i] + " s with an iron axe for " + door.getBlock() + ", got " + ticks + " ticks");
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void doorsOnlyTakeTheirOwnKey(GameTestHelper helper) {
		BlockPos copper = new BlockPos(1, 1, 2);
		BlockPos iron = new BlockPos(3, 1, 2);
		placeDoor(helper, copper, ModBlocks.ARMORED_COPPER_DOOR.getDefaultState());
		placeDoor(helper, iron, ModBlocks.ARMORED_IRON_DOOR.getDefaultState());
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack ironKey = ModItems.IRON_KEY.asStack();
		ironKey.set(ModDataComponents.KEY_ID.get(), UUID.randomUUID());
		use(helper, player, copper, ironKey);
		helper.assertFalse(isOpen(helper, copper), "An iron key must not open a copper door");
		use(helper, player, iron, ironKey);
		helper.assertTrue(isOpen(helper, iron), "The iron key should open the iron door");
		// Seul le cuivre s'oxyde et se cire
		helper.assertFalse(helper.getBlockState(iron).isRandomlyTicking(), "An iron door must not oxidize");
		use(helper, player, iron, new ItemStack(Items.HONEYCOMB));
		helper.assertFalse(helper.getBlockState(iron).getValue(ArmoredDoorBlock.WAXED), "An iron door cannot be waxed");
		helper.succeed();
	}

	/** Pendant l'ouverture, la forme de la porte (contour, collisions) tourne avec elle. */
	@GameTest(template = "empty", timeoutTicks = 100)
	public static void doorHitboxFollowsTheSwing(GameTestHelper helper) {
		// Les formes calculées retombent exactement sur celles de Minecraft, fermée et ouverte
		for (Direction facing : Direction.Plane.HORIZONTAL)
			for (var hinge : net.minecraft.world.level.block.state.properties.DoorHingeSide.values()) {
				BlockState closed = ModBlocks.ARMORED_COPPER_DOOR.getDefaultState().setValue(DoorBlock.FACING, facing)
					.setValue(DoorBlock.HINGE, hinge);
				var empty = net.minecraft.world.level.EmptyBlockGetter.INSTANCE;
				helper.assertTrue(DoorSwing.shape(closed, 0).bounds().equals(closed.getShape(empty, BlockPos.ZERO).bounds()),
					"Closed swing shape should match the door for " + facing + "/" + hinge);
				helper.assertTrue(DoorSwing.shape(closed, 1).bounds()
						.equals(closed.setValue(DoorBlock.OPEN, true).getShape(empty, BlockPos.ZERO).bounds()),
					"Open swing shape should match the open door for " + facing + "/" + hinge);
			}

		BlockPos lower = new BlockPos(2, 1, 2);
		placeDoor(helper, lower);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		var closedBox = shapeOf(helper, lower);
		use(helper, player, lower, keyWithId(UUID.randomUUID()));
		helper.runAfterDelay(10, () -> {
			var middle = shapeOf(helper, lower);
			helper.assertFalse(middle.equals(closedBox), "Halfway, the hitbox should have left the closed position");
			helper.assertTrue(middle.getXsize() > 0.3 && middle.getZsize() > 0.3,
				"Halfway, the hitbox should be diagonal, got " + middle);
			helper.assertTrue(shapeOf(helper, lower.above()).equals(middle), "Both halves move together");
		});
		helper.runAfterDelay(30, () -> {
			var open = shapeOf(helper, lower);
			var vanillaOpen = helper.getBlockState(lower).setValue(ArmoredDoorBlock.VISIBLE, true)
				.getShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
			helper.assertTrue(open.equals(vanillaOpen), "At the end, the hitbox is the open door: " + open);
			helper.succeed();
		});
	}

	private static net.minecraft.world.phys.AABB shapeOf(GameTestHelper helper, BlockPos pos) {
		BlockPos abs = helper.absolutePos(pos);
		return helper.getLevel().getBlockState(abs).getShape(helper.getLevel(), abs).bounds();
	}

	@GameTest(template = "empty")
	public static void tapestryWorksWithLoomAndKeepsPatterns(GameTestHelper helper) {
		// Le métier à tisser accepte tout BannerItem
		ItemStack plain = ModBlocks.TAPESTRY.asStack();
		helper.assertTrue(plain.getItem() instanceof net.minecraft.world.item.BannerItem banner
			&& banner.getColor() == net.minecraft.world.item.DyeColor.BROWN, "The tapestry must be usable in a Loom");
		helper.assertTrue("block.create_locksmithing.tapestry".equals(plain.getDescriptionId()),
			"The tapestry needs its own name");

		// Posée avec l'objet sur la face sud d'un mur : à plat contre le mur, sur 2 blocs vers le haut
		// (y = 1 est le sol de la zone de test, on se met juste au-dessus)
		BlockPos wall = new BlockPos(2, 2, 2);
		helper.setBlock(wall, Blocks.STONE_BRICKS);
		helper.setBlock(wall.above(), Blocks.STONE_BRICKS);
		var registry = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN);
		var patterns = new net.minecraft.world.level.block.entity.BannerPatternLayers.Builder()
			.add(registry.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.BORDER),
				net.minecraft.world.item.DyeColor.RED)
			.build();
		ItemStack decorated = ModBlocks.TAPESTRY.asStack();
		decorated.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, patterns);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, decorated);
		BlockPos absWall = helper.absolutePos(wall);
		decorated.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(absWall).add(0, 0, 0.5), Direction.SOUTH, absWall, false)));
		BlockPos pos = wall.south();
		helper.assertBlockPresent(ModBlocks.TAPESTRY.get(), pos);
		helper.assertBlockPresent(ModBlocks.TAPESTRY.get(), pos.above());
		helper.assertTrue(helper.getBlockState(pos).getValue(TapestryBlock.FACING) == Direction.SOUTH,
			"The tapestry should face away from the wall");
		helper.assertTrue(helper.getBlockState(pos).getValue(TapestryBlock.HALF) == DoubleBlockHalf.LOWER
			&& helper.getBlockState(pos.above()).getValue(TapestryBlock.HALF) == DoubleBlockHalf.UPPER,
			"It goes up from the clicked block (never into the ground)");
		TapestryBlockEntity be = helper.getBlockEntity(pos);
		helper.assertTrue(be.getPatterns().equals(patterns), "The tapestry should take the item's patterns");

		// Sans mur derrière, elle ne tient pas
		helper.setBlock(wall.above(), Blocks.AIR);
		helper.assertBlockNotPresent(ModBlocks.TAPESTRY.get(), pos.above());
		helper.assertBlockNotPresent(ModBlocks.TAPESTRY.get(), pos);
		helper.assertTrue(droppedTapestryMatches(helper, pos, patterns), "It should drop once, with its patterns");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void tapestryCoversDoorAndComesBack(GameTestHelper helper) {
		BlockPos lower = new BlockPos(2, 1, 2);
		placeDoor(helper, lower);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		var registry = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN);
		var patterns = new net.minecraft.world.level.block.entity.BannerPatternLayers.Builder()
			.add(registry.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CROSS),
				net.minecraft.world.item.DyeColor.BLUE)
			.build();
		ItemStack tapestries = ModBlocks.TAPESTRY.asStack(2);
		tapestries.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, patterns);

		// Clic droit (sur la moitié haute) : les deux moitiés passent en tapisserie, une seule est utilisée
		use(helper, player, lower.above(), tapestries);
		helper.assertTrue(hasTapestry(helper, lower) && hasTapestry(helper, lower.above()),
			"Both halves should show the tapestry");
		helper.assertTrue(tapestries.getCount() == 1, "One tapestry should be used, got " + tapestries.getCount());
		ArmoredDoorBlockEntity door = helper.getBlockEntity(lower);
		helper.assertTrue(door.getTapestryPatterns().equals(patterns), "The door should keep the tapestry's patterns");
		use(helper, player, lower, tapestries);
		helper.assertTrue(tapestries.getCount() == 1, "Only one tapestry per door");

		// La clé marche toujours et la tapisserie reste
		use(helper, player, lower, keyWithId(UUID.randomUUID()));
		helper.assertTrue(isOpen(helper, lower) && hasTapestry(helper, lower), "The door should open with its tapestry");

		// Cisailles : la tapisserie revient, avec ses motifs
		ItemStack shears = new ItemStack(Items.SHEARS);
		use(helper, player, lower.above(), shears);
		helper.assertFalse(hasTapestry(helper, lower) || hasTapestry(helper, lower.above()), "Shears should remove it");
		helper.assertTrue(shears.getDamageValue() == 1, "Shears should take damage");
		helper.assertTrue(droppedTapestryMatches(helper, lower, patterns), "Shears should give the tapestry back");

		// Casser la porte rend la porte et la tapisserie
		use(helper, player, lower, tapestries);
		helper.assertTrue(tapestries.isEmpty(), "The last tapestry should be used");
		helper.getLevel().destroyBlock(helper.absolutePos(lower), true);
		helper.assertBlockNotPresent(ModBlocks.ARMORED_COPPER_DOOR.get(), lower.above());
		helper.assertItemEntityCountIs(ModBlocks.ARMORED_COPPER_DOOR.asItem(), lower, 3, 1);
		helper.assertTrue(droppedTapestryMatches(helper, lower, patterns), "Breaking the door should drop the tapestry");
		helper.succeed();
	}

	/* Outils */

	private static boolean hasTapestry(GameTestHelper helper, BlockPos pos) {
		BlockState state = helper.getBlockState(pos);
		return state.getBlock() instanceof ArmoredDoorBlock && state.getValue(ArmoredDoorBlock.TAPESTRY);
	}

	/** Une seule tapisserie par terre, avec les bons motifs (elle est ramassée pour la suite). */
	private static boolean droppedTapestryMatches(GameTestHelper helper, BlockPos pos,
		net.minecraft.world.level.block.entity.BannerPatternLayers patterns) {
		var items = helper.getEntities(net.minecraft.world.entity.EntityType.ITEM, pos, 3).stream()
			.filter(e -> e.getItem().is(ModBlocks.TAPESTRY.asItem()))
			.toList();
		boolean ok = items.size() == 1 && items.get(0).getItem().getCount() == 1
			&& patterns.equals(items.get(0).getItem().get(net.minecraft.core.component.DataComponents.BANNER_PATTERNS));
		items.forEach(net.minecraft.world.entity.Entity::discard);
		return ok;
	}

	private static void placeDoor(GameTestHelper helper, BlockPos lower) {
		placeDoor(helper, lower, ModBlocks.ARMORED_COPPER_DOOR.getDefaultState());
	}

	private static void placeDoor(GameTestHelper helper, BlockPos lower, BlockState door) {
		BlockState state = door.setValue(DoorBlock.FACING, Direction.NORTH);
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
