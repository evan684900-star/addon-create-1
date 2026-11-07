package io.github.evan684900star.locksmithing;

import io.github.evan684900star.locksmithing.ponder.LocksmithingPonderPlugin;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class LocksmithingClient {
	public static void onCtor(IEventBus modEventBus) {
		ModPartialModels.init();
		modEventBus.addListener(LocksmithingClient::clientSetup);
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		PonderIndex.addPlugin(new LocksmithingPonderPlugin());
	}
}
