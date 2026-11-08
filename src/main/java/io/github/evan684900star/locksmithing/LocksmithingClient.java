package io.github.evan684900star.locksmithing;

import io.github.evan684900star.locksmithing.content.tapestry.TapestryItemRenderer;
import io.github.evan684900star.locksmithing.ponder.LocksmithingPonderPlugin;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class LocksmithingClient {
	public static void onCtor(IEventBus modEventBus) {
		ModPartialModels.init();
		modEventBus.addListener(LocksmithingClient::clientSetup);
		modEventBus.addListener(LocksmithingClient::clientExtensions);
	}

	/** L'icône de la tapisserie est dessinée en direct (avec ses motifs). */
	private static void clientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return TapestryItemRenderer.INSTANCE;
			}
		}, ModBlocks.TAPESTRY.asItem());
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		PonderIndex.addPlugin(new LocksmithingPonderPlugin());
	}
}
