package io.github.evan684900star.locksmithing.registry;

import com.simibubi.create.AllCreativeModeTabs;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.door.DoorMetal;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
	private static final DeferredRegister<CreativeModeTab> REGISTER =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Locksmithing.ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register("main",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.create_locksmithing.main"))
			.withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
			.icon(() -> ModBlocks.ARMORED_COPPER_DOOR.asStack())
			.displayItems((params, output) -> {
				for (var door : ModBlocks.ARMORED_DOORS)
					output.accept(door);
				output.accept(ModBlocks.MUFFLE_FURNACE);
				output.accept(ModBlocks.CASTING_ANVIL);
				output.accept(ModBlocks.KEY_CUTTER);
				output.accept(ModBlocks.TAPESTRY);
				output.accept(ModItems.PRESSED_PLANK);
				output.accept(ModItems.COMPRESSED_WOOD);
				output.accept(ModItems.IRON_BAR);
				output.accept(ModItems.GOLD_BAR);
				output.accept(ModItems.COPPER_BAR);
				output.accept(ModItems.ZINC_BAR);
				output.accept(ModItems.MOLD);
				output.accept(ModItems.SAW_BLADE);
				for (DoorMetal metal : DoorMetal.values()) {
					output.accept(ModItems.blankKeyFor(metal));
					output.accept(ModItems.keyFor(metal));
				}
				ModFluids.ALL.forEach(f -> output.accept(f.get().getBucket()));
			})
			.build());

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
