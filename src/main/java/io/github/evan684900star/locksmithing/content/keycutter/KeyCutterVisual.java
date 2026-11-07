package io.github.evan684900star.locksmithing.content.keycutter;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Rendu Flywheel : l'axe qui traverse le pied de l'enclume et la lame sur le dessus tournent ensemble. */
public class KeyCutterVisual extends KineticBlockEntityVisual<KeyCutterBlockEntity> {
	private final RotatingInstance shaft;
	private final RotatingInstance saw;

	public KeyCutterVisual(VisualizationContext context, KeyCutterBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		Direction.Axis axis = rotationAxis();
		Direction facing = blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);

		shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
			.createInstance();
		shaft.setup(blockEntity)
			.setPosition(getVisualPosition())
			.rotateToFace(Direction.UP, axis)
			.setChanged();

		Vec3 offset = KeyCutterBlock.sawOffset(facing);
		saw = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(ModPartialModels.KEY_CUTTER_SAW))
			.createInstance();
		saw.setup(blockEntity)
			.setPosition(getVisualPosition())
			.nudge((float) offset.x, (float) offset.y, (float) offset.z)
			.rotateToFace(Direction.SOUTH, axis)
			.setChanged();
	}

	@Override
	public void update(float partialTick) {
		shaft.setup(blockEntity).setChanged();
		saw.setup(blockEntity).setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(shaft, saw);
	}

	@Override
	protected void _delete() {
		shaft.delete();
		saw.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(shaft);
		consumer.accept(saw);
	}
}
