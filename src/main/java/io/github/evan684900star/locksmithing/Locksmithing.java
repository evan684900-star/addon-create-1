package io.github.evan684900star.locksmithing;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import io.github.evan684900star.locksmithing.registry.ModBlockEntities;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModCapabilities;
import io.github.evan684900star.locksmithing.registry.ModCreativeTabs;
import io.github.evan684900star.locksmithing.registry.ModDataComponents;
import io.github.evan684900star.locksmithing.registry.ModFluids;
import io.github.evan684900star.locksmithing.registry.ModItems;
import io.github.evan684900star.locksmithing.registry.ModMenus;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import io.github.evan684900star.locksmithing.registry.ModSounds;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Locksmithing.ID)
public class Locksmithing {
	public static final String ID = "create_locksmithing";
	public static final String NAME = "Create: Locksmithing";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
		.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
			.andThen(TooltipModifier.mapNull(KineticStats.create(item))));

	public Locksmithing(IEventBus modEventBus, ModContainer container) {
		REGISTRATE.registerEventListeners(modEventBus);

		ModCreativeTabs.register(modEventBus);
		ModDataComponents.register(modEventBus);
		ModRecipeTypes.register(modEventBus);
		ModSounds.register(modEventBus);

		ModFluids.register();
		ModBlocks.register();
		ModItems.register();
		ModBlockEntities.register();
		ModMenus.register();

		modEventBus.addListener(ModCapabilities::register);

		if (FMLEnvironment.dist == Dist.CLIENT)
			LocksmithingClient.onCtor(modEventBus);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
