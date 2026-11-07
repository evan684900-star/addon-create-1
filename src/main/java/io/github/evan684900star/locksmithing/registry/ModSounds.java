package io.github.evan684900star.locksmithing.registry;

import io.github.evan684900star.locksmithing.Locksmithing;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sons du mod (fichiers .ogg dans assets/create_locksmithing/sounds, provenance dans SOUNDS_CREDITS.md). */
public class ModSounds {
	private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(Registries.SOUND_EVENT, Locksmithing.ID);

	public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_UNLOCK = register("door.unlock");
	public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_LOCK = register("door.lock");
	public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_LOCKED = register("door.locked");
	public static final DeferredHolder<SoundEvent, SoundEvent> KEY_BIND = register("key.bind");
	public static final DeferredHolder<SoundEvent, SoundEvent> KEY_CUTTER_CUT = register("key_cutter.cut");
	public static final DeferredHolder<SoundEvent, SoundEvent> KEY_CUTTER_DONE = register("key_cutter.done");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASTING_HISS = register("casting_anvil.hiss");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASTING_CLINK = register("casting_anvil.clink");
	public static final DeferredHolder<SoundEvent, SoundEvent> FURNACE_LOAD = register("muffle_furnace.load");

	private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
		return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(Locksmithing.asResource(name)));
	}

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
