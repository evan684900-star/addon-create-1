package io.github.evan684900star.locksmithing.content.keycutter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.github.evan684900star.locksmithing.registry.ModPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * La clé posée sur l'enclume ; et, sans Flywheel (ou dans les tutos Ponder), l'axe et la lame qui tournent.
 */
public class KeyCutterRenderer extends KineticBlockEntityRenderer<KeyCutterBlockEntity> {
	public KeyCutterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KeyCutterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState state = be.getBlockState();
		Direction facing = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
		renderKey(be, facing, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		net.minecraft.core.Direction.Axis axis = getRotationAxisOf(be);
		renderRotatingKineticBlock(be, shaft(axis), ms, buffer.getBuffer(RenderType.solid()), light);

		Direction along = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		SuperByteBuffer saw = CachedBuffers.partialFacing(ModPartialModels.KEY_CUTTER_SAW, state, along);
		Vec3 offset = KeyCutterBlock.sawOffset(facing);
		ms.pushPose();
		ms.translate(offset.x, offset.y, offset.z);
		standardKineticRotationTransform(saw, be, light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
		ms.popPose();
	}

	/** La clé (vierge ou taillée) couchée sur l'enclume, devant la lame. */
	private void renderKey(KeyCutterBlockEntity be, Direction facing, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		ItemStack stack = be.getDisplayedKey();
		if (stack.isEmpty())
			return;
		ms.pushPose();
		ms.translate(0.5 - facing.getStepX() * 2 / 16f, 16.5 / 16f, 0.5 - facing.getStepZ() * 2 / 16f);
		ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
		ms.mulPose(Axis.XP.rotationDegrees(90));
		ms.scale(0.45f, 0.45f, 0.45f);
		Minecraft.getInstance()
			.getItemRenderer()
			.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
		ms.popPose();
	}
}
