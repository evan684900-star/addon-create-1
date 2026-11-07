package io.github.evan684900star.locksmithing.content.keycutter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Rendu sans Flywheel (et dans les tutos Ponder) : l'axe à l'arrière et la lame qui tourne à l'avant. */
public class KeyCutterRenderer extends KineticBlockEntityRenderer<KeyCutterBlockEntity> {
	public KeyCutterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KeyCutterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		BlockState state = be.getBlockState();
		Direction facing = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);

		SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing.getOpposite());
		standardKineticRotationTransform(shaft, be, light).renderInto(ms, buffer.getBuffer(RenderType.solid()));

		SuperByteBuffer saw = CachedBuffers.partialFacing(ModPartialModels.KEY_CUTTER_SAW, state, facing);
		standardKineticRotationTransform(saw, be, light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}
}
