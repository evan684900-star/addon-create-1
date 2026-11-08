package io.github.evan684900star.locksmithing.gametest;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.casting.CastingAnvilBlockEntity;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlock;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterBlockEntity;
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlockEntity;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModFluids;
import io.github.evan684900star.locksmithing.registry.ModItems;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Visite automatique pour vérifier le rendu (lancée seulement avec -Dlocksmithing.screenshots=true) :
 * crée un monde plat, pose les blocs, ouvre les interfaces et les tutos Ponder, prend des captures puis quitte.
 */
@EventBusSubscriber(modid = Locksmithing.ID, value = Dist.CLIENT)
public class ScreenshotTour {
	private static final boolean ENABLED = Boolean.getBoolean("locksmithing.screenshots");
	private static final BlockPos BASE = new BlockPos(0, -60, 0);

	private record Step(int delay, Runnable action) {}

	private static final List<Step> STEPS = new ArrayList<>();
	private static int stepIndex = -1;
	private static int wait;
	private static boolean worldRequested;

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event) {
		if (!ENABLED)
			return;
		Minecraft mc = Minecraft.getInstance();

		if (!worldRequested) {
			if (mc.screen instanceof TitleScreen) {
				worldRequested = true;
				createWorld(mc);
			}
			return;
		}
		if (stepIndex < 0) {
			if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null)
				return;
			buildSteps(mc);
			stepIndex = 0;
			wait = 40;
			return;
		}
		if (wait-- > 0)
			return;
		if (stepIndex >= STEPS.size()) {
			mc.stop();
			return;
		}
		Step step = STEPS.get(stepIndex++);
		step.action.run();
		wait = step.delay;
	}

	private static void createWorld(Minecraft mc) {
		// Toujours un monde neuf (sinon les blocs d'une ancienne visite restent là)
		org.apache.commons.io.FileUtils.deleteQuietly(new java.io.File(mc.gameDirectory, "saves/screenshots"));
		GameRules rules = new GameRules();
		rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
		rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
		rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
		LevelSettings settings = new LevelSettings("screenshots", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
			rules, WorldDataConfiguration.DEFAULT);
		mc.createWorldOpenFlows().createFreshLevel("screenshots", settings, new WorldOptions(1L, false, false),
			access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()
				.createWorldDimensions(), new TitleScreen());
	}

	private static void server(Minecraft mc, java.util.function.Consumer<ServerLevel> action) {
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> action.accept(server.overworld()));
	}

	private static void teleport(Minecraft mc, double x, double y, double z, float yRot, float xRot) {
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayers().get(0);
			player.teleportTo(server.overworld(), x, y, z, yRot, xRot);
		});
	}

	/** Une tapisserie avec quelques motifs de bannière. */
	private static ItemStack patterned(ServerLevel level, int variant) {
		var patterns = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN);
		var builder = new net.minecraft.world.level.block.entity.BannerPatternLayers.Builder();
		if (variant == 0) {
			builder.add(patterns.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.BORDER), net.minecraft.world.item.DyeColor.YELLOW);
			builder.add(patterns.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.RHOMBUS_MIDDLE), net.minecraft.world.item.DyeColor.RED);
			builder.add(patterns.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CIRCLE_MIDDLE), net.minecraft.world.item.DyeColor.YELLOW);
		} else {
			builder.add(patterns.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.STRIPE_CENTER), net.minecraft.world.item.DyeColor.WHITE);
			builder.add(patterns.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.STRIPE_MIDDLE), net.minecraft.world.item.DyeColor.WHITE);
			builder.add(patterns.getHolderOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CURLY_BORDER), net.minecraft.world.item.DyeColor.BLACK);
		}
		ItemStack stack = ModBlocks.TAPESTRY.asStack();
		stack.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, builder.build());
		return stack;
	}

	private static void seekPonderFromEnd(Minecraft mc, int ticks) {
		if (mc.screen instanceof net.createmod.ponder.foundation.ui.PonderUI ui)
			ui.seekToTime(ui.getActiveScene().getTotalTime() - ticks);
	}

	/** Une tapisserie collée au mur (moitié basse + moitié haute), tournée vers le sud. */
	private static void placeTapestry(ServerLevel level, BlockPos pos, ItemStack stack) {
		BlockState state = ModBlocks.TAPESTRY.getDefaultState()
			.setValue(io.github.evan684900star.locksmithing.content.tapestry.TapestryBlock.FACING, Direction.SOUTH);
		level.setBlock(pos, state, 2);
		level.setBlock(pos.above(), state.setValue(io.github.evan684900star.locksmithing.content.tapestry.TapestryBlock.HALF,
			DoubleBlockHalf.UPPER), 2);
		if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BlockEntity be)
			be.applyComponentsFromItemStack(stack);
		level.sendBlockUpdated(pos, state, state, 3);
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), msg -> {});
	}

	private static void buildSteps(Minecraft mc) {
		// 1. Le décor
		STEPS.add(new Step(20, () -> server(mc, level -> {
			level.setDayTime(6000);
			BlockPos p = BASE;
			// Four à moufle sur un brûleur allumé
			level.setBlockAndUpdate(p, AllBlocks.BLAZE_BURNER.getDefaultState()
				.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED));
			if (level.getBlockEntity(p) instanceof BlazeBurnerBlockEntity burner)
				burner.isCreative = true;
			level.setBlockAndUpdate(p.above(), ModBlocks.MUFFLE_FURNACE.getDefaultState()
				.setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
			if (level.getBlockEntity(p.above()) instanceof MuffleFurnaceBlockEntity furnace)
				furnace.getItemCapability().insertItem(0, new ItemStack(Items.IRON_INGOT, 8), false);
			// Enclume de moulage remplie de cuivre, avec une barre coulée
			BlockPos anvil = p.east(2);
			level.setBlockAndUpdate(anvil, ModBlocks.CASTING_ANVIL.getDefaultState()
				.setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
			if (level.getBlockEntity(anvil) instanceof CastingAnvilBlockEntity be)
				be.ponderShow(new FluidStack(ModFluids.source(ModFluids.MOLTEN_COPPER), 1500), ModItems.COPPER_BAR.asStack());
			// Machine à clés + moteur créatif derrière
			BlockPos cutter = p.east(4);
			level.setBlockAndUpdate(cutter, ModBlocks.KEY_CUTTER.getDefaultState()
				.setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.SOUTH));
			// orientée sud : l'axe la traverse d'est en ouest, le moteur est à l'ouest
			level.setBlockAndUpdate(cutter.west(), AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(CreativeMotorBlock.FACING, Direction.EAST));
			if (level.getBlockEntity(cutter) instanceof KeyCutterBlockEntity be)
				be.getItemCapability().insertItem(0, ModItems.COPPER_BLANK_KEY.asStack(4), false);
			// Une deuxième machine sans moteur (pour l'interface "rotation nécessaire")
			level.setBlockAndUpdate(p.offset(4, 0, 4), ModBlocks.KEY_CUTTER.getDefaultState()
				.setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.SOUTH));
			// Les 4 niveaux d'oxydation de la porte
			WeatherState[] stages = WeatherState.values();
			for (int i = 0; i < stages.length; i++) {
				BlockPos door = p.east(6 + i);
				BlockState state = ModBlocks.ARMORED_COPPER_DOOR.getDefaultState()
					.setValue(DoorBlock.FACING, Direction.NORTH)
					.setValue(ArmoredDoorBlock.OXIDATION, stages[i]);
				level.setBlock(door, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
				level.setBlock(door.above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
				level.setBlock(door.above(2), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			}
			level.setBlock(p.east(5), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			level.setBlock(p.east(5).above(), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			level.setBlock(p.east(10), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			level.setBlock(p.east(10).above(), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			// Mur tapissé : les tapisseries collées côte à côte, comme du papier peint
			for (int x = 11; x <= 17; x++)
				for (int y = 0; y <= 2; y++)
					level.setBlock(p.offset(x, y, 0), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			ItemStack[] tapestries = { ModBlocks.TAPESTRY.asStack(), patterned(level, 0), patterned(level, 1) };
			ItemStack[] wall = { tapestries[1], tapestries[1], tapestries[1], tapestries[2], tapestries[0] };
			for (int i = 0; i < wall.length; i++)
				placeTapestry(level, p.offset(12 + i, 0, 1), wall[i]);
			// Portes blindées avec une tapisserie (unie, puis décorées)
			for (int x = 18; x <= 24; x++)
				for (int y = 0; y <= 2; y++)
					if (y == 2 || x % 2 == 0)
						level.setBlock(p.offset(x, y, 0), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			for (int i = 0; i < tapestries.length; i++) {
				BlockPos door = p.offset(19 + i * 2, 0, 0);
				BlockState state = ModBlocks.ARMORED_COPPER_DOOR.getDefaultState()
					.setValue(DoorBlock.FACING, Direction.NORTH)
					.setValue(DoorBlock.HINGE, i == 2 ? net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT
						: net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT)
					.setValue(ArmoredDoorBlock.OXIDATION, i == 1 ? WeatherState.WEATHERED : WeatherState.UNAFFECTED)
					.setValue(ArmoredDoorBlock.TAPESTRY, true);
				level.setBlock(door, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
				level.setBlock(door.above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
				if (level.getBlockEntity(door) instanceof io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlockEntity be)
					be.setTapestry(tapestries[i]);
			}
			// Portes en fer, zinc et or
			for (int x = 25; x <= 31; x++)
				for (int y = 0; y <= 2; y++)
					if (y == 2 || x % 2 == 1)
						level.setBlock(p.offset(x, y, 0), Blocks.STONE_BRICKS.defaultBlockState(), 2);
			for (int i = 1; i < ModBlocks.ARMORED_DOORS.length; i++) {
				BlockPos door = p.offset(24 + i * 2, 0, 0);
				BlockState state = ModBlocks.ARMORED_DOORS[i].getDefaultState().setValue(DoorBlock.FACING, Direction.NORTH);
				level.setBlock(door, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
				level.setBlock(door.above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
			}
		})));
		STEPS.add(new Step(80, () -> {
			mc.options.hideGui = true;
			teleport(mc, 5.0, -59.0, 7.5, 180f, 18f);
		}));
		STEPS.add(new Step(10, () -> shot(mc, "01_overview")));
		STEPS.add(new Step(60, () -> teleport(mc, 3.2, -59.4, 3.2, 160f, 30f)));
		STEPS.add(new Step(10, () -> shot(mc, "02_machines_close")));
		STEPS.add(new Step(60, () -> teleport(mc, 2.5, -58.6, 1.6, 180f, 55f)));
		STEPS.add(new Step(10, () -> shot(mc, "02b_casting_anvil_top")));
		STEPS.add(new Step(60, () -> teleport(mc, 3.4, -59.2, 2.0, 215f, 38f)));
		STEPS.add(new Step(10, () -> shot(mc, "02c_key_cutter")));
		STEPS.add(new Step(60, () -> teleport(mc, 8.0, -59.0, 3.6, 180f, 10f)));
		STEPS.add(new Step(10, () -> shot(mc, "03_doors")));
		STEPS.add(new Step(60, () -> teleport(mc, 6.5, -58.6, -2.5, 0f, 25f)));
		STEPS.add(new Step(10, () -> shot(mc, "04_back")));
		STEPS.add(new Step(60, () -> teleport(mc, 7.6, -59.0, 3.4, 160f, 12f)));
		STEPS.add(new Step(1, () -> server(mc, level -> {
			BlockPos door = BASE.east(6);
			BlockState state = level.getBlockState(door);
			if (state.getBlock() instanceof ArmoredDoorBlock block)
				block.swing(null, level, state, door, true);
		})));
		STEPS.add(new Step(6, () -> shot(mc, "03c_door_swing_1")));
		STEPS.add(new Step(5, () -> shot(mc, "03c_door_swing_2")));
		STEPS.add(new Step(25, () -> shot(mc, "03c_door_swing_3")));
		STEPS.add(new Step(60, () -> teleport(mc, 14.5, -59.0, 5.5, 180f, 8f)));
		STEPS.add(new Step(10, () -> shot(mc, "03b_tapestries")));
		STEPS.add(new Step(60, () -> teleport(mc, 17.6, -59.0, 3.2, 125f, 8f)));
		STEPS.add(new Step(10, () -> shot(mc, "03b2_tapestries_side")));
		STEPS.add(new Step(60, () -> teleport(mc, 28.5, -59.0, 4.2, 180f, 6f)));
		STEPS.add(new Step(10, () -> shot(mc, "03h_metal_doors")));
		// Contour de la porte pendant l'ouverture (avec l'interface pour voir le contour)
		STEPS.add(new Step(60, () -> {
			mc.options.hideGui = false;
			teleport(mc, 26.5, -59.0, 3.0, 180f, 25f);
		}));
		STEPS.add(new Step(1, () -> server(mc, level -> {
			BlockPos door = BASE.offset(26, 0, 0);
			BlockState state = level.getBlockState(door);
			if (state.getBlock() instanceof ArmoredDoorBlock block)
				block.swing(null, level, state, door, true);
		})));
		// Avec le rendu logiciel il y a peu d'images par seconde : plusieurs captures pour tomber en pleine ouverture
		for (int i = 1; i <= 6; i++) {
			String name = "03i_door_hitbox_swing_" + i;
			STEPS.add(new Step(3, () -> shot(mc, name)));
		}
		STEPS.add(new Step(30, () -> shot(mc, "03j_door_hitbox_open")));
		STEPS.add(new Step(1, () -> mc.options.hideGui = true));
		// Portes avec tapisserie : devant, derrière, puis pendant l'ouverture et ouvertes
		STEPS.add(new Step(60, () -> teleport(mc, 21.5, -59.0, 4.2, 180f, 6f)));
		STEPS.add(new Step(10, () -> shot(mc, "03d_tapestry_doors")));
		STEPS.add(new Step(60, () -> teleport(mc, 21.5, -59.0, -3.2, 0f, 6f)));
		STEPS.add(new Step(10, () -> shot(mc, "03e_tapestry_doors_back")));
		// De biais, tout près : la tranche de la porte reste pleine
		STEPS.add(new Step(60, () -> teleport(mc, 18.6, -59.0, 1.3, 255f, 8f)));
		STEPS.add(new Step(10, () -> shot(mc, "03e2_tapestry_door_grazing")));
		STEPS.add(new Step(60, () -> teleport(mc, 22.6, -59.0, 3.4, 160f, 10f)));
		STEPS.add(new Step(1, () -> server(mc, level -> {
			for (int i = 0; i < 3; i++) {
				BlockPos door = BASE.offset(19 + i * 2, 0, 0);
				BlockState state = level.getBlockState(door);
				if (state.getBlock() instanceof ArmoredDoorBlock block)
					block.swing(null, level, state, door, true);
			}
		})));
		STEPS.add(new Step(9, () -> shot(mc, "03f_tapestry_doors_swing")));
		STEPS.add(new Step(40, () -> shot(mc, "03g_tapestry_doors_open")));

		if (Boolean.getBoolean("locksmithing.screenshots.quick")) {
			STEPS.add(new Step(10, () -> mc.setScreen(null)));
			return;
		}
		// 2. Les interfaces
		STEPS.add(new Step(30, () -> {
			mc.options.hideGui = false;
			teleport(mc, 2.5, -59.0, 2.5, 180f, 40f);
			server(mc, level -> {
				ServerPlayer player = level.getServer().getPlayerList().getPlayers().get(0);
				if (level.getBlockEntity(BASE.east(2)) instanceof CastingAnvilBlockEntity be)
					player.openMenu(be, BASE.east(2));
			});
		}));
		STEPS.add(new Step(10, () -> shot(mc, "05_gui_casting_anvil")));
		STEPS.add(new Step(30, () -> server(mc, level -> {
			ServerPlayer player = level.getServer().getPlayerList().getPlayers().get(0);
			player.closeContainer();
			if (level.getBlockEntity(BASE.east(4)) instanceof KeyCutterBlockEntity be)
				player.openMenu(be, BASE.east(4));
		})));
		STEPS.add(new Step(10, () -> shot(mc, "06_gui_key_cutter")));
		STEPS.add(new Step(30, () -> server(mc, level -> {
			ServerPlayer player = level.getServer().getPlayerList().getPlayers().get(0);
			player.closeContainer();
			if (level.getBlockEntity(BASE.offset(4, 0, 4)) instanceof KeyCutterBlockEntity be)
				player.openMenu(be, BASE.offset(4, 0, 4));
		})));
		STEPS.add(new Step(10, () -> shot(mc, "06b_gui_key_cutter_no_rotation")));
		STEPS.add(new Step(20, () -> server(mc, level -> {
			ServerPlayer player = level.getServer().getPlayerList().getPlayers().get(0);
			player.closeContainer();
			player.setGameMode(GameType.SURVIVAL);
			player.getInventory().clearContent();
			ItemStack[] stacks = { ModItems.COPPER_KEY.asStack(), ModItems.SAW_BLADE.asStack(), patterned(level, 0),
				ModBlocks.ARMORED_COPPER_DOOR.asStack(), ModBlocks.ARMORED_IRON_DOOR.asStack(),
				ModBlocks.ARMORED_ZINC_DOOR.asStack(), ModBlocks.ARMORED_GOLD_DOOR.asStack(), ModBlocks.TAPESTRY.asStack(),
				patterned(level, 1),
				ModBlocks.MUFFLE_FURNACE.asStack(), ModBlocks.CASTING_ANVIL.asStack(), ModBlocks.KEY_CUTTER.asStack(),
				ModItems.PRESSED_PLANK.asStack(), ModItems.COMPRESSED_WOOD.asStack(), ModItems.MOLD.asStack(),
				ModItems.IRON_BAR.asStack(), ModItems.GOLD_BAR.asStack(), ModItems.COPPER_BAR.asStack(),
				ModItems.ZINC_BAR.asStack(), ModItems.COPPER_BLANK_KEY.asStack(), ModItems.IRON_BLANK_KEY.asStack(),
				ModItems.ZINC_BLANK_KEY.asStack(), ModItems.GOLD_BLANK_KEY.asStack(), ModItems.IRON_KEY.asStack(),
				ModItems.ZINC_KEY.asStack(), ModItems.GOLD_KEY.asStack(),
				new ItemStack(ModFluids.MOLTEN_IRON.get().getBucket()), new ItemStack(ModFluids.MOLTEN_GOLD.get().getBucket()),
				new ItemStack(ModFluids.MOLTEN_COPPER.get().getBucket()), new ItemStack(ModFluids.MOLTEN_ZINC.get().getBucket()) };
			// Les 9 premiers dans la barre (pour les vues "en main"), le reste dans l'inventaire
			for (int i = 0; i < stacks.length; i++)
				player.getInventory().setItem(i, stacks[i]);
			player.getInventory().selected = 0;
		})));
		STEPS.add(new Step(20, () -> mc.setScreen(new InventoryScreen(mc.player))));
		STEPS.add(new Step(10, () -> shot(mc, "07_items")));
		// En main, à la première personne : clé (l'anneau vers le joueur), lame de scie, tapisserie
		String[] inHand = { "07d_key_in_hand", "07e_saw_in_hand", "07f_tapestry_in_hand" };
		for (int i = 0; i < inHand.length; i++) {
			int slot = i;
			STEPS.add(new Step(20, () -> {
				mc.setScreen(null);
				mc.options.hideGui = false;
				mc.player.getInventory().selected = slot;
				teleport(mc, 8.0, -59.0, 6.5, 200f, 15f);
			}));
			STEPS.add(new Step(25, () -> shot(mc, inHand[slot])));
		}
		STEPS.add(new Step(1, () -> mc.options.hideGui = true));

		// Métier à tisser de Minecraft avec une tapisserie et un colorant
		STEPS.add(new Step(30, () -> server(mc, level -> {
			ServerPlayer player = level.getServer().getPlayerList().getPlayers().get(0);
			BlockPos loom = BASE.offset(2, 0, 4);
			level.setBlock(loom, Blocks.LOOM.defaultBlockState(), 2);
			player.openMenu(Blocks.LOOM.defaultBlockState().getMenuProvider(level, loom));
			if (player.containerMenu instanceof net.minecraft.world.inventory.LoomMenu menu) {
				menu.getBannerSlot().set(ModBlocks.TAPESTRY.asStack());
				menu.getDyeSlot().set(new ItemStack(Items.YELLOW_DYE));
				menu.clickMenuButton(player, 3);
			}
		})));
		STEPS.add(new Step(10, () -> shot(mc, "07a_loom")));
		STEPS.add(new Step(10, () -> server(mc, level -> level.getServer().getPlayerList().getPlayers().get(0).closeContainer())));

		// 3. JEI
		STEPS.add(new Step(40, () -> {
			mc.setScreen(null);
			var runtime = io.github.evan684900star.locksmithing.compat.jei.LocksmithingJeiPlugin.runtime;
			if (runtime != null)
				runtime.getRecipesGui().showTypes(java.util.List.of(
					io.github.evan684900star.locksmithing.compat.jei.LocksmithingJeiPlugin.MELTING));
		}));
		STEPS.add(new Step(10, () -> shot(mc, "07b_jei_melting")));
		STEPS.add(new Step(40, () -> {
			var runtime = io.github.evan684900star.locksmithing.compat.jei.LocksmithingJeiPlugin.runtime;
			if (runtime != null)
				runtime.getRecipesGui().showTypes(java.util.List.of(
					io.github.evan684900star.locksmithing.compat.jei.LocksmithingJeiPlugin.CASTING,
					io.github.evan684900star.locksmithing.compat.jei.LocksmithingJeiPlugin.KEY_CUTTING));
		}));
		STEPS.add(new Step(10, () -> shot(mc, "07c_jei_casting")));
		STEPS.add(new Step(10, () -> mc.setScreen(null)));

		// 4. Les tutos Ponder
		String[][] ponders = { { "pressed_plank", "08_ponder_compressed_wood" }, { "muffle_furnace", "09_ponder_muffle_furnace" },
			{ "casting_anvil", "10_ponder_casting_anvil" }, { "key_cutter", "11_ponder_key_cutter" },
			{ "armored_copper_door", "12_ponder_armored_door" } };
		for (String[] ponder : ponders) {
			STEPS.add(new Step(150, () -> {
				if (mc.screen instanceof InventoryScreen)
					mc.setScreen(null);
				ItemStack stack = switch (ponder[0]) {
					case "pressed_plank" -> ModItems.PRESSED_PLANK.asStack();
					case "muffle_furnace" -> ModBlocks.MUFFLE_FURNACE.asStack();
					case "casting_anvil" -> ModBlocks.CASTING_ANVIL.asStack();
					case "key_cutter" -> ModBlocks.KEY_CUTTER.asStack();
					default -> ModBlocks.ARMORED_COPPER_DOOR.asStack();
				};
				ScreenOpener.open(PonderUI.of(stack));
			}));
			STEPS.add(new Step(120, () -> shot(mc, ponder[1] + "_a")));
			STEPS.add(new Step(10, () -> shot(mc, ponder[1] + "_b")));
		}
		// Fin du tuto de la porte : la tapisserie posée, porte fermée puis ouverte
		STEPS.add(new Step(1, () -> seekPonderFromEnd(mc, 150)));
		STEPS.add(new Step(20, () -> shot(mc, "12_ponder_armored_door_c")));
		// (le tuto avance plus vite que ce compteur avec le rendu logiciel : on vise le milieu des 15 ticks où elle est ouverte)
		STEPS.add(new Step(1, () -> seekPonderFromEnd(mc, 92)));
		STEPS.add(new Step(2, () -> shot(mc, "12_ponder_armored_door_d")));
		STEPS.add(new Step(10, () -> mc.setScreen(null)));
	}
}
