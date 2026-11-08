package io.github.evan684900star.locksmithing.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import io.github.evan684900star.locksmithing.content.casting.CastingAnvilBlockEntity;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlock;
import io.github.evan684900star.locksmithing.content.door.ArmoredDoorBlockEntity;
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlock;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModFluids;
import io.github.evan684900star.locksmithing.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Les 5 tutos Ponder, volontairement courts. Les textes anglais ci-dessous servent de référence ;
 * ceux affichés en jeu sont dans les fichiers de langue (create_locksmithing.ponder.<scène>.text_N).
 */
public class LocksmithingScenes {

	/* 1. Bois compressé */
	public static void compressedWood(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("compressed_wood", "Making Compressed Wood");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos depot = util.grid().at(2, 1, 2);
		BlockPos press = util.grid().at(2, 3, 2);
		Selection kinetics = util.select().fromTo(2, 3, 2, 2, 3, 3);
		scene.world().showSection(util.select().position(depot), Direction.DOWN);
		scene.idle(5);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(15);

		ItemStack plank = new ItemStack(Items.OAK_PLANKS);
		ItemStack pressed = ModItems.PRESSED_PLANK.asStack();
		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(depot))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Pressing any plank gives a Pressed Plank");
		scene.world().createItemOnBeltLike(depot, Direction.NORTH, plank);
		scene.idle(10);
		scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
			be -> be.getPressingBehaviour().start(Mode.BELT));
		scene.idle(30);
		scene.world().removeItemsFromBelt(depot);
		scene.world().createItemOnBeltLike(depot, Direction.UP, pressed);
		scene.overlay().showControls(util.vector().topOf(depot), Pointing.DOWN, 30).withItem(pressed);
		scene.idle(40);

		scene.world().setBlock(depot, AllBlocks.BASIN.getDefaultState(), true);
		scene.idle(10);
		ItemStack four = ModItems.PRESSED_PLANK.asStack(4);
		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(depot, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("4 Pressed Planks compacted in a Basin = 1 Compressed Wood");
		scene.world().modifyBlockEntity(depot, BasinBlockEntity.class,
			be -> be.getInputInventory().setStackInSlot(0, four.copy()));
		scene.idle(25);
		scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
			be -> be.getPressingBehaviour().start(Mode.BASIN));
		scene.idle(30);
		ItemStack wood = ModItems.COMPRESSED_WOOD.asStack();
		scene.world().modifyBlockEntity(depot, BasinBlockEntity.class, be -> {
			be.getInputInventory().setStackInSlot(0, ItemStack.EMPTY);
			be.getOutputInventory().setStackInSlot(0, wood.copy());
		});
		scene.overlay().showControls(util.vector().topOf(depot), Pointing.DOWN, 50).withItem(wood);
		scene.idle(60);
	}

	/* 2. Four à moufle */
	public static void muffleFurnace(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("muffle_furnace", "The Muffle Furnace");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos burner = util.grid().at(2, 1, 2);
		BlockPos furnace = util.grid().at(2, 2, 2);
		scene.world().showSection(util.select().position(burner), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(furnace), Direction.DOWN);
		scene.idle(15);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(burner, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Needs a lit Blaze Burner below");
		scene.overlay().showControls(util.vector().blockSurface(burner, Direction.WEST), Pointing.LEFT, 30)
			.rightClick()
			.withItem(new ItemStack(Items.COAL));
		scene.idle(30);
		scene.world().modifyBlock(burner, s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
		scene.world().modifyBlock(furnace, s -> s.setValue(MuffleFurnaceBlock.LIT, true), false);
		scene.idle(30);

		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(furnace))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Right-click with ingots to load them");
		scene.overlay().showControls(util.vector().topOf(furnace), Pointing.DOWN, 40).rightClick()
			.withItem(new ItemStack(Items.IRON_INGOT, 16));
		scene.idle(60);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(furnace, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("1 ingot = 90 mB of molten metal");
		scene.idle(60);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(furnace))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Collect it with a Bucket or pipes");
		scene.overlay().showControls(util.vector().topOf(furnace), Pointing.DOWN, 30).rightClick()
			.withItem(new ItemStack(Items.BUCKET));
		scene.idle(35);
		scene.overlay().showControls(util.vector().topOf(furnace), Pointing.DOWN, 30)
			.withItem(new ItemStack(ModFluids.MOLTEN_IRON.get().getBucket()));
		scene.idle(40);

		scene.world().modifyBlock(burner, s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.SEETHING), false);
		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(burner, Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("Superheated: twice as fast");
		scene.idle(60);
	}

	/* 3. Enclume de moulage */
	public static void castingAnvil(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("casting_anvil", "The Casting Anvil");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos anvil = util.grid().at(2, 1, 2);
		scene.world().showSection(util.select().position(anvil), Direction.DOWN);
		scene.idle(20);

		scene.overlay().showText(40)
			.pointAt(util.vector().topOf(anvil))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Right-click to open it");
		scene.overlay().showControls(util.vector().topOf(anvil), Pointing.DOWN, 30).rightClick();
		scene.idle(50);

		ItemStack bucket = new ItemStack(ModFluids.MOLTEN_COPPER.get().getBucket());
		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(anvil))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Bucket of molten metal on the left");
		scene.overlay().showControls(util.vector().topOf(anvil), Pointing.DOWN, 30).withItem(bucket);
		scene.idle(25);
		scene.world().modifyBlockEntity(anvil, CastingAnvilBlockEntity.class,
			be -> be.ponderShow(new FluidStack(ModFluids.source(ModFluids.MOLTEN_COPPER), 1000), ItemStack.EMPTY));
		scene.idle(40);

		ItemStack bar = ModItems.COPPER_BAR.asStack();
		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(anvil))
			.placeNearTarget()
			.attachKeyFrame()
			.text("90 mB = 1 bar, out on the right");
		scene.idle(30);
		scene.world().modifyBlockEntity(anvil, CastingAnvilBlockEntity.class,
			be -> be.ponderShow(new FluidStack(ModFluids.source(ModFluids.MOLTEN_COPPER), 910), bar.copy()));
		scene.overlay().showControls(util.vector().topOf(anvil), Pointing.DOWN, 40).withItem(bar);
		scene.idle(50);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(anvil, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Pipes and funnels work too");
		scene.idle(60);
	}

	/* 4. Machine à tailler les clés */
	public static void keyCutter(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("key_cutter", "The Key Cutter");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos cutter = util.grid().at(2, 1, 2);
		Selection kinetics = util.select().fromTo(1, 1, 2, 2, 1, 2);
		scene.world().setKineticSpeed(kinetics, 0);
		scene.world().showSection(util.select().position(cutter), Direction.DOWN);
		scene.idle(15);

		scene.overlay().showText(50)
			.pointAt(util.vector().blockSurface(cutter, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Needs rotation through its shaft");
		scene.world().showSection(util.select().position(1, 1, 2), Direction.EAST);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.effects().indicateSuccess(cutter);
		scene.idle(45);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(cutter))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Blank Key on the left, then pick the key");
		scene.overlay().showControls(util.vector().topOf(cutter), Pointing.DOWN, 40).rightClick()
			.withItem(ModItems.BLANK_KEY.asStack());
		scene.idle(70);

		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(cutter))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Every cut key is unique");
		scene.overlay().showControls(util.vector().topOf(cutter), Pointing.DOWN, 40)
			.withItem(ModItems.COPPER_KEY.asStack());
		scene.idle(60);

		scene.world().setKineticSpeed(kinetics, 128);
		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(cutter))
			.placeNearTarget()
			.text("Faster rotation, faster cutting");
		scene.idle(60);
	}

	/** Ouvre/ferme la porte avec son animation (1 s), puis remet le modèle fixe. */
	private static void swing(CreateSceneBuilder scene, Selection door, boolean open) {
		scene.world().modifyBlocks(door, s -> s.setValue(DoorBlock.OPEN, open).setValue(ArmoredDoorBlock.VISIBLE, false), false);
		scene.idle(ArmoredDoorBlock.SWING_TICKS + 2);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.VISIBLE, true), false);
	}

	/* 5. Porte blindée */
	public static void armoredDoor(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("armored_door", "The Armored Copper Door");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos lower = util.grid().at(2, 1, 2);
		Selection door = util.select().fromTo(2, 1, 2, 2, 2, 2);
		scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 3, 2).substract(door), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(door, Direction.DOWN);
		scene.idle(15);

		Vec3 front = util.vector().blockSurface(lower, Direction.NORTH);
		ItemStack key = ModItems.COPPER_KEY.asStack();
		scene.overlay().showText(50)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("No hand, no redstone: only a key");
		scene.idle(60);

		scene.overlay().showText(50)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The first key used becomes its key");
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(key);
		scene.idle(10);
		swing(scene, door, true);
		scene.idle(15);
		swing(scene, door, false);
		scene.idle(5);

		scene.overlay().showText(50)
			.pointAt(front)
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.text("Other keys are refused");
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(key);
		scene.idle(60);

		scene.overlay().showText(60)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Breaking it resets the lock (42 s with an Iron Axe)");
		scene.idle(70);

		scene.overlay().showText(50)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("It oxidizes like copper");
		scene.idle(15);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.OXIDATION, WeatherState.EXPOSED), false);
		scene.idle(15);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.OXIDATION, WeatherState.WEATHERED), false);
		scene.idle(30);

		scene.overlay().showText(50)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Honeycomb waxes it, an Axe scrapes it");
		scene.overlay().showControls(front, Pointing.RIGHT, 25).rightClick().withItem(new ItemStack(Items.HONEYCOMB));
		scene.idle(35);
		scene.overlay().showControls(front, Pointing.RIGHT, 25).rightClick().withItem(new ItemStack(Items.IRON_AXE));
		scene.idle(10);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.OXIDATION, WeatherState.UNAFFECTED), false);
		scene.idle(40);

		// Tapisserie : elle remplace le bois et suit l'ouverture
		scene.overlay().showText(50)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("A Tapestry replaces the wood");
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(ModBlocks.TAPESTRY.asStack());
		scene.idle(10);
		scene.world().modifyBlockEntity(lower, ArmoredDoorBlockEntity.class, be -> {
			ItemStack tapestry = ModBlocks.TAPESTRY.asStack();
			var patterns = be.getLevel().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
			tapestry.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
				.addIfRegistered(patterns, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.ORANGE)
				.addIfRegistered(patterns, BannerPatterns.CIRCLE_MIDDLE, DyeColor.YELLOW)
				.addIfRegistered(patterns, BannerPatterns.STRIPE_TOP, DyeColor.BLACK)
				.addIfRegistered(patterns, BannerPatterns.STRIPE_BOTTOM, DyeColor.BLACK)
				.build());
			be.setTapestry(tapestry);
		});
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.TAPESTRY, true), false);
		scene.idle(45);
		swing(scene, door, true);
		scene.idle(15);
		swing(scene, door, false);
		scene.idle(10);

		scene.overlay().showText(40)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Shears take it back");
		scene.overlay().showControls(front, Pointing.RIGHT, 25).rightClick().withItem(new ItemStack(Items.SHEARS));
		scene.idle(10);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.TAPESTRY, false), false);
		scene.idle(40);
	}
}
