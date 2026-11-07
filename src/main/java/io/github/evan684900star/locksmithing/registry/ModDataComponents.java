package io.github.evan684900star.locksmithing.registry;

import java.util.UUID;

import io.github.evan684900star.locksmithing.Locksmithing;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
	private static final DeferredRegister.DataComponents REGISTER =
		DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Locksmithing.ID);

	/** Identifiant unique d'une clé taillée : c'est lui que la serrure d'une porte retient. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> KEY_ID =
		REGISTER.registerComponentType("key_id", b -> b.persistent(UUIDUtil.CODEC)
			.networkSynchronized(UUIDUtil.STREAM_CODEC));

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
