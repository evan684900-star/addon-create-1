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
			level.setBlockAndUpdate(cutter.north(), AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(CreativeMotorBlock.FACING, Direction.SOUTH));
			if (level.getBlockEntity(cutter) instanceof KeyCutterBlockEntity be)
				be.getItemCapability().insertItem(0, ModItems.BLANK_KEY.asStack(4), false);
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
		})));
		STEPS.add(new Step(80, () -> {
			mc.options.hideGui = true;
			teleport(mc, 5.0, -59.0, 7.5, 180f, 18f);
		}));
		STEPS.add(new Step(10, () -> shot(mc, "01_overview")));
		STEPS.add(new Step(60, () -> teleport(mc, 3.2, -59.4, 3.2, 160f, 30f)));
		STEPS.add(new Step(10, () -> shot(mc, "02_machines_close")));
		STEPS.add(new Step(60, () -> teleport(mc, 8.0, -59.0, 3.6, 180f, 10f)));
		STEPS.add(new Step(10, () -> shot(mc, "03_doors")));
		STEPS.add(new Step(60, () -> teleport(mc, 6.5, -58.6, -2.5, 0f, 25f)));
		STEPS.add(new Step(10, () -> shot(mc, "04_back")));

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
		STEPS.add(new Step(20, () -> server(mc, level -> {
			ServerPlayer player = level.getServer().getPlayerList().getPlayers().get(0);
			player.closeContainer();
			player.setGameMode(GameType.SURVIVAL);
			player.getInventory().clearContent();
			ItemStack[] stacks = { ModBlocks.ARMORED_COPPER_DOOR.asStack(), ModBlocks.MUFFLE_FURNACE.asStack(),
				ModBlocks.CASTING_ANVIL.asStack(), ModBlocks.KEY_CUTTER.asStack(), ModItems.PRESSED_PLANK.asStack(),
				ModItems.COMPRESSED_WOOD.asStack(), ModItems.IRON_BAR.asStack(), ModItems.GOLD_BAR.asStack(),
				ModItems.COPPER_BAR.asStack(), ModItems.ZINC_BAR.asStack(), ModItems.MOLD.asStack(),
				ModItems.SAW_BLADE.asStack(), ModItems.BLANK_KEY.asStack(), ModItems.COPPER_KEY.asStack(),
				new ItemStack(ModFluids.MOLTEN_IRON.get().getBucket()), new ItemStack(ModFluids.MOLTEN_GOLD.get().getBucket()),
				new ItemStack(ModFluids.MOLTEN_COPPER.get().getBucket()), new ItemStack(ModFluids.MOLTEN_ZINC.get().getBucket()),
				ModItems.MOLTEN_IRON_BOTTLE.asStack(), ModItems.MOLTEN_GOLD_BOTTLE.asStack(),
				ModItems.MOLTEN_COPPER_BOTTLE.asStack(), ModItems.MOLTEN_ZINC_BOTTLE.asStack() };
			for (int i = 0; i < stacks.length; i++)
				player.getInventory().setItem(9 + i, stacks[i]);
		})));
		STEPS.add(new Step(20, () -> mc.setScreen(new InventoryScreen(mc.player))));
		STEPS.add(new Step(10, () -> shot(mc, "07_items")));

		// 3. Les tutos Ponder
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
		STEPS.add(new Step(10, () -> mc.setScreen(null)));
	}
}
