package io.github.evan684900star.locksmithing.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.evan684900star.locksmithing.Locksmithing;

/** Modèles qui tournent (chargés côté client uniquement). */
public class ModPartialModels {
	public static final PartialModel KEY_CUTTER_SAW = PartialModel.of(Locksmithing.asResource("block/key_cutter/saw"));

	public static void init() {}
}
