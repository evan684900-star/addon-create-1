package io.github.evan684900star.locksmithing.registry;

import com.tterrag.registrate.util.entry.FluidEntry;

import io.github.evan684900star.locksmithing.content.fluid.MoltenMetalFluid;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

public class ModCapabilities {
	public static void register(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MUFFLE_FURNACE.get(),
			(be, side) -> be.getItemCapability());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MUFFLE_FURNACE.get(),
			(be, side) -> be.getFluidCapability());

		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CASTING_ANVIL.get(),
			(be, side) -> be.getItemCapability());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CASTING_ANVIL.get(),
			(be, side) -> be.getFluidCapability());

		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.KEY_CUTTER.get(),
			(be, side) -> be.getItemCapability());

		// Nos seaux ne sont pas des BucketItem "simples" : NeoForge ne leur donne pas la capacité tout seul.
		for (FluidEntry<MoltenMetalFluid> fluid : ModFluids.ALL)
			if (fluid.get().getBucket() != Items.AIR)
				event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack),
					fluid.get().getBucket());
	}
}
