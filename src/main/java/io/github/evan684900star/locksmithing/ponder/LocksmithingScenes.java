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
import io.github.evan684900star.locksmithing.content.furnace.MuffleFurnaceBlock;
import io.github.evan684900star.locksmithing.registry.ModFluids;
import io.github.evan684900star.locksmithing.registry.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Les 5 tutos Ponder. Les textes anglais ci-dessous servent de référence ; les traductions affichées en jeu
 * sont dans les fichiers de langue (create_locksmithing.ponder.<scène>.text_N).
 */
public class LocksmithingScenes {

	/* 1. Bois compressé : presse sur dépôt, puis presse + bassin */
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
		scene.world().setKineticSpeed(kinetics, 0);
		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(10);

		Vec3 pressSide = util.vector().blockSurface(press, Direction.WEST);
		scene.overlay().showText(70)
			.pointAt(pressSide.subtract(0, 2, 0))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Any wooden plank pressed by a Mechanical Press becomes a Pressed Plank");
		scene.idle(50);

		ItemStack plank = new ItemStack(Items.OAK_PLANKS);
		ItemStack pressed = ModItems.PRESSED_PLANK.asStack();
		scene.world().createItemOnBeltLike(depot, Direction.NORTH, plank);
		scene.overlay().showControls(util.vector().topOf(depot), Pointing.DOWN, 30).withItem(plank);
		scene.idle(10);
		scene.world().modifyBlockEntity(press, MechanicalPressBlockEntity.class,
			be -> be.getPressingBehaviour().start(Mode.BELT));
		scene.idle(30);
		scene.world().removeItemsFromBelt(depot);
		scene.world().createItemOnBeltLike(depot, Direction.UP, pressed);
		scene.overlay().showControls(util.vector().topOf(depot), Pointing.DOWN, 40).withItem(pressed);
		scene.idle(50);

		// On remplace le dépôt par un bassin
		scene.world().setBlock(depot, AllBlocks.BASIN.getDefaultState(), true);
		scene.idle(10);
		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(depot, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Then, compacting 4 Pressed Planks in a Basin gives 1 Compressed Wood");
		scene.idle(40);

		ItemStack four = ModItems.PRESSED_PLANK.asStack(4);
		scene.overlay().showControls(util.vector().topOf(depot), Pointing.DOWN, 30).withItem(four);
		scene.world().modifyBlockEntity(depot, BasinBlockEntity.class,
			be -> be.getInputInventory().setStackInSlot(0, four.copy()));
		scene.idle(30);
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

		scene.overlay().showText(70)
			.pointAt(util.vector().topOf(depot))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Compressed Wood is used to craft Armored Doors");
		scene.idle(80);
	}

	/* 2. Four à moufle */
	public static void muffleFurnace(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("muffle_furnace", "Melting Ingots in the Muffle Furnace");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos burner = util.grid().at(2, 1, 2);
		BlockPos furnace = util.grid().at(2, 2, 2);
		scene.world().showSection(util.select().position(burner), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(furnace), Direction.DOWN);
		scene.idle(15);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(burner, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Muffle Furnace has to sit on a lit Blaze Burner");
		scene.idle(50);
		scene.overlay().showControls(util.vector().blockSurface(burner, Direction.WEST), Pointing.LEFT, 30)
			.rightClick()
			.withItem(new ItemStack(Items.COAL));
		scene.idle(10);
		scene.world().modifyBlock(burner, s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
		scene.world().modifyBlock(furnace, s -> s.setValue(MuffleFurnaceBlock.LIT, true), false);
		scene.idle(40);

		ItemStack ingot = new ItemStack(Items.IRON_INGOT, 16);
		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(furnace))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Right-click it with Iron, Gold, Copper or Zinc ingots to load them. Funnels and hoppers work too");
		scene.idle(30);
		scene.overlay().showControls(util.vector().topOf(furnace), Pointing.DOWN, 40).rightClick().withItem(ingot);
		scene.idle(60);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(furnace, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Each ingot melts into 90 mB of molten metal: exactly one bar");
		scene.idle(80);

		scene.overlay().showText(90)
			.pointAt(util.vector().topOf(furnace))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Collect it with an empty Bucket (1000 mB) or Glass Bottle (90 mB), or pump it out through pipes");
		scene.idle(30);
		scene.overlay().showControls(util.vector().topOf(furnace), Pointing.DOWN, 30).rightClick()
			.withItem(new ItemStack(Items.BUCKET));
		scene.idle(40);
		scene.overlay().showControls(util.vector().topOf(furnace), Pointing.DOWN, 40)
			.withItem(new ItemStack(ModFluids.MOLTEN_IRON.get().getBucket()));
		scene.idle(50);

		scene.world().modifyBlock(burner, s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.SEETHING), false);
		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(burner, Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.attachKeyFrame()
			.text("A superheated Blaze Burner melts twice as fast");
		scene.idle(80);
	}

	/* 3. Enclume de moulage */
	public static void castingAnvil(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("casting_anvil", "Casting Bars with the Casting Anvil");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos anvil = util.grid().at(2, 1, 2);
		scene.world().showSection(util.select().position(anvil), Direction.DOWN);
		scene.idle(20);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(anvil))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Right-click the Casting Anvil to open its interface");
		scene.overlay().showControls(util.vector().topOf(anvil), Pointing.DOWN, 40).rightClick();
		scene.idle(70);

		ItemStack bucket = new ItemStack(ModFluids.MOLTEN_COPPER.get().getBucket());
		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(anvil))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Put a bucket or a bottle of molten metal in the left slot: it is poured into the mold");
		scene.overlay().showControls(util.vector().topOf(anvil), Pointing.DOWN, 40).withItem(bucket);
		scene.idle(30);
		scene.world().modifyBlockEntity(anvil, CastingAnvilBlockEntity.class,
			be -> be.ponderShow(new FluidStack(ModFluids.source(ModFluids.MOLTEN_COPPER), 1000), ItemStack.EMPTY));
		scene.idle(60);

		ItemStack bar = ModItems.COPPER_BAR.asStack();
		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(anvil))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Once it has cooled down, a bar comes out on the right. One bar uses 90 mB");
		scene.idle(40);
		scene.world().modifyBlockEntity(anvil, CastingAnvilBlockEntity.class,
			be -> be.ponderShow(new FluidStack(ModFluids.source(ModFluids.MOLTEN_COPPER), 910), bar.copy()));
		scene.overlay().showControls(util.vector().topOf(anvil), Pointing.DOWN, 40).withItem(bar);
		scene.idle(60);

		scene.overlay().showText(80)
			.pointAt(util.vector().blockSurface(anvil, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Fluid pipes can also fill the mold, and funnels can take the bars out");
		scene.idle(90);
	}

	/* 4. Machine à tailler les clés */
	public static void keyCutter(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("key_cutter", "Cutting Keys");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos cutter = util.grid().at(2, 1, 2);
		Selection kinetics = util.select().fromTo(2, 1, 2, 2, 1, 3);
		scene.world().setKineticSpeed(kinetics, 0);
		scene.world().showSection(util.select().position(cutter), Direction.DOWN);
		scene.idle(15);

		scene.overlay().showText(70)
			.pointAt(util.vector().blockSurface(cutter, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Key Cutter needs rotational force, given to the shaft at its back");
		scene.idle(30);
		scene.world().showSection(util.select().position(2, 1, 3), Direction.NORTH);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.effects().indicateSuccess(cutter);
		scene.idle(50);

		scene.overlay().showText(90)
			.pointAt(util.vector().topOf(cutter))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Right-click it, put a Blank Key in the left slot and choose the key to cut");
		scene.overlay().showControls(util.vector().topOf(cutter), Pointing.DOWN, 40).rightClick()
			.withItem(ModItems.BLANK_KEY.asStack());
		scene.idle(100);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(cutter))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Every key that comes out is unique, even if two of them look the same");
		scene.overlay().showControls(util.vector().topOf(cutter), Pointing.DOWN, 50).withItem(ModItems.COPPER_KEY.asStack());
		scene.idle(90);

		scene.world().setKineticSpeed(kinetics, 128);
		scene.overlay().showText(60)
			.pointAt(util.vector().blockSurface(cutter, Direction.NORTH))
			.placeNearTarget()
			.text("The faster it spins, the faster keys are cut");
		scene.idle(70);
	}

	/* 5. Porte blindée et serrure */
	public static void armoredDoor(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("armored_door", "Locking an Armored Copper Door");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos lower = util.grid().at(2, 1, 2);
		Selection door = util.select().fromTo(2, 1, 2, 2, 2, 2);
		scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 3, 2).substract(door), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(door, Direction.DOWN);
		scene.idle(20);

		Vec3 front = util.vector().blockSurface(lower, Direction.NORTH);
		scene.overlay().showText(70)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Armored Doors cannot be opened by hand, nor with redstone");
		scene.idle(80);

		ItemStack key = ModItems.COPPER_KEY.asStack();
		scene.overlay().showText(80)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The first cut key used on a door becomes its only key");
		scene.idle(20);
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(key);
		scene.idle(10);
		scene.world().modifyBlocks(door, s -> s.setValue(DoorBlock.OPEN, true), false);
		scene.idle(40);
		scene.world().modifyBlocks(door, s -> s.setValue(DoorBlock.OPEN, false), false);
		scene.idle(30);

		scene.overlay().showText(70)
			.pointAt(front)
			.placeNearTarget()
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.text("Any other key is refused, even another Copper Key");
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(key);
		scene.idle(80);

		scene.overlay().showText(80)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Breaking the door resets its lock. It takes 42 seconds with an Iron Axe, less with a better one");
		scene.idle(90);

		scene.overlay().showText(70)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Like copper, the door slowly oxidizes over time");
		scene.idle(20);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.OXIDATION, WeatherState.EXPOSED), false);
		scene.idle(20);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.OXIDATION, WeatherState.WEATHERED), false);
		scene.idle(40);

		scene.overlay().showText(80)
			.pointAt(front)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Use a Honeycomb to wax it and keep its look. An Axe scrapes off wax and oxidation");
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(new ItemStack(Items.HONEYCOMB));
		scene.idle(40);
		scene.overlay().showControls(front, Pointing.RIGHT, 30).rightClick().withItem(new ItemStack(Items.IRON_AXE));
		scene.idle(10);
		scene.world().modifyBlocks(door, s -> s.setValue(ArmoredDoorBlock.OXIDATION, WeatherState.UNAFFECTED), false);
		scene.idle(50);
	}
}
