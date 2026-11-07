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
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.minecraft.core.Direction;

/** Rendu Flywheel : l'axe à l'arrière et la lame de scie à l'avant tournent avec la rotation reçue. */
public class KeyCutterVisual extends KineticBlockEntityVisual<KeyCutterBlockEntity> {
	private final RotatingInstance shaft;
	private final RotatingInstance saw;

	public KeyCutterVisual(VisualizationContext context, KeyCutterBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		Direction facing = blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
		shaft = create(AllPartialModels.SHAFT_HALF, facing.getOpposite());
		saw = create(ModPartialModels.KEY_CUTTER_SAW, facing);
	}

	private RotatingInstance create(PartialModel partial, Direction towards) {
		RotatingInstance instance = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(partial))
			.createInstance();
		instance.setup(blockEntity)
			.setPosition(getVisualPosition())
			.rotateToFace(Direction.SOUTH, towards)
			.setChanged();
		return instance;
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
