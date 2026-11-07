package io.github.evan684900star.locksmithing;

import io.github.evan684900star.locksmithing.ponder.LocksmithingPonderPlugin;
import io.github.evan684900star.locksmithing.registry.ModItems;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class LocksmithingClient {
	public static void onCtor(IEventBus modEventBus) {
		ModPartialModels.init();
		modEventBus.addListener(LocksmithingClient::clientSetup);
		modEventBus.addListener(LocksmithingClient::itemColors);
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		PonderIndex.addPlugin(new LocksmithingPonderPlugin());
	}

	/** Les fioles utilisent la fiole de Minecraft : on teinte le liquide de la couleur du métal (comme les potions). */
	private static void itemColors(RegisterColorHandlersEvent.Item event) {
		tint(event, ModItems.MOLTEN_IRON_BOTTLE.get(), 0xFF7A1E);
		tint(event, ModItems.MOLTEN_GOLD_BOTTLE.get(), 0xFFC21E);
		tint(event, ModItems.MOLTEN_COPPER_BOTTLE.get(), 0xE8552B);
		tint(event, ModItems.MOLTEN_ZINC_BOTTLE.get(), 0x9FC3D6);
	}

	private static void tint(RegisterColorHandlersEvent.Item event, net.minecraft.world.item.Item item, int rgb) {
		event.register((stack, layer) -> layer == 0 ? 0xFF000000 | rgb : -1, item);
	}
}
