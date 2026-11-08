package io.github.evan684900star.locksmithing.content.door;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import io.github.evan684900star.locksmithing.content.door.DoorSwing.Hinge;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import io.github.evan684900star.locksmithing.content.tapestry.TapestryPaint;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

/**
 * Animation d'ouverture de la porte blindée : pendant 1 seconde, les deux moitiés de la porte (fermée) tournent
 * autour des gonds. Le calcul se fait à chaque image (120 images si le jeu tourne à 120 FPS), avec une accélération
 * au départ et un ralentissement à l'arrivée, comme une vraie porte lourde.
 * <p>
 * Dessine aussi la tapisserie posée sur la porte : son cuir et ses motifs, juste derrière chaque face, là où la
 * texture de la porte est transparente (le bois). Elle suit l'animation.
 */
public class ArmoredDoorRenderer extends SafeBlockEntityRenderer<ArmoredDoorBlockEntity> {
	public ArmoredDoorRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(ArmoredDoorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState lower = be.getBlockState();
		if (!(lower.getBlock() instanceof ArmoredDoorBlock) || lower.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER)
			return;

		boolean open = lower.getValue(DoorBlock.OPEN);
		float now = AnimationTickHolder.getRenderTime(be.getLevel());
		float progress = be.swingProgress(open, now);
		// Le modèle fixe est affiché quand la porte ne bouge pas.
		boolean moving = !lower.getValue(ArmoredDoorBlock.VISIBLE);
		boolean tapestry = lower.getValue(ArmoredDoorBlock.TAPESTRY);
		if (!moving && !tapestry)
			return;

		BlockState closedLower = lower.setValue(DoorBlock.OPEN, false).setValue(ArmoredDoorBlock.VISIBLE, true);
		BlockState closedUpper = closedLower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
		Hinge hinge = Hinge.of(closedLower);
		float angle = DoorSwing.angle(hinge, moving ? DoorSwing.ease(progress) : open ? 1 : 0);

		BlockPos pos = be.getBlockPos();
		int upperLight = LevelRenderer.getLightColor(be.getLevel(), pos.above());
		if (moving) {
			VertexConsumer consumer = buffer.getBuffer(RenderType.cutoutMipped());
			renderHalf(closedLower, 0, hinge, angle, light, ms, consumer);
			renderHalf(closedUpper, 1, hinge, angle, upperLight, ms, consumer);
		}
		if (tapestry)
			renderTapestry(be.getTapestryPatterns(), closedLower, hinge, angle, ms, buffer, light, upperLight, overlay);
	}

	/* Tapisserie */

	/** La tapisserie est à 1/4 de pixel sous chaque face de la porte. */
	private static final float INSET = 0.25f / 16;
	/**
	 * Zone couverte (le drapeau de 20x40 de la bannière, réduit à 14x28 pixels de porte) : tout le bois, le cadre en
	 * cuivre cache 1 pixel de chaque côté. Hauteurs comptées depuis le bas de la moitié basse.
	 */
	private static final float SIDE_MIN = 1 / 16f, SIDE_MAX = 15 / 16f, BOTTOM = 2 / 16f, TOP = 30 / 16f;

	/** Le cuir, puis chaque motif teint par-dessus, comme une bannière. Tourne avec la porte. */
	private static void renderTapestry(BannerPatternLayers patterns, BlockState closed, Hinge hinge, float angle,
		PoseStack ms, MultiBufferSource buffer, int lowerLight, int upperLight, int overlay) {
		AABB shut = DoorSwing.closedBounds(closed);
		boolean thinX = shut.getXsize() < shut.getZsize();
		ms.pushPose();
		TransformStack.of(ms)
			.translate(hinge.x(), 0, hinge.z())
			.rotateYDegrees(angle)
			.translate(-hinge.x(), 0, -hinge.z());
		PoseStack.Pose pose = ms.last();
		TapestryPaint.paint(buffer, patterns,
			(consumer, color) -> drawLayer(pose, consumer, color, shut, thinX, lowerLight, upperLight, overlay));
		ms.popPose();
	}

	/**
	 * Une couche sur les deux faces de la porte. Chaque face se lit à l'endroit pour celui qui la regarde ;
	 * elle est coupée en deux (moitié basse, moitié haute) pour prendre la lumière de chaque bloc.
	 */
	private static void drawLayer(PoseStack.Pose pose, VertexConsumer consumer, int color, AABB shut, boolean thinX,
		int lowerLight, int upperLight, int overlay) {
		for (int side = -1; side <= 1; side += 2) {
			float nx = thinX ? side : 0, nz = thinX ? 0 : side;
			double plane = thinX ? (side < 0 ? shut.minX + INSET : shut.maxX - INSET)
				: (side < 0 ? shut.minZ + INSET : shut.maxZ - INSET);
			// La droite de celui qui regarde cette face : haut x normale = (nz, 0, -nx)
			boolean rightIsPositive = (thinX ? -nx : nz) > 0;
			float left = rightIsPositive ? SIDE_MIN : SIDE_MAX, right = rightIsPositive ? SIDE_MAX : SIDE_MIN;
			Face face = new Face(pose, consumer, color, thinX, (float) plane, nx, nz, overlay);
			face.quad(left, right, BOTTOM, 1, TapestryPaint.V_MIDDLE, TapestryPaint.V_BOTTOM, lowerLight);
			face.quad(left, right, 1, TOP, TapestryPaint.V_TOP, TapestryPaint.V_MIDDLE, upperLight);
		}
	}

	private record Face(PoseStack.Pose pose, VertexConsumer consumer, int color, boolean thinX, float plane, float nx,
		float nz, int overlay) {
		/** Gauche-bas, droite-bas, droite-haut, gauche-haut : sens inverse des aiguilles d'une montre vu de devant. */
		void quad(float left, float right, float bottom, float top, float vTop, float vBottom, int light) {
			vertex(left, bottom, TapestryPaint.U_LEFT, vBottom, light);
			vertex(right, bottom, TapestryPaint.U_RIGHT, vBottom, light);
			vertex(right, top, TapestryPaint.U_RIGHT, vTop, light);
			vertex(left, top, TapestryPaint.U_LEFT, vTop, light);
		}

		void vertex(float along, float y, float u, float v, int light) {
			float x = thinX ? plane : along, z = thinX ? along : plane;
			TapestryPaint.vertex(consumer, pose, x, y, z, u, v, color, light, overlay, nx, 0, nz);
		}
	}

	private static void renderHalf(BlockState state, float yOffset, Hinge hinge, float angle, int light, PoseStack ms,
		VertexConsumer consumer) {
		SuperByteBuffer model = CachedBuffers.block(state);
		model.translate(0, yOffset, 0)
			.translate(hinge.x(), 0, hinge.z())
			.rotateYDegrees(angle)
			.translate(-hinge.x(), 0, -hinge.z())
			.light(light)
			.renderInto(ms, consumer);
	}

	@Override
	public AABB getRenderBoundingBox(ArmoredDoorBlockEntity be) {
		BlockPos pos = be.getBlockPos();
		return new AABB(pos).inflate(1, 0, 1).expandTowards(0, 1, 0);
	}
}
