package io.github.evan684900star.locksmithing.content.door;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

/**
 * Animation d'ouverture de la porte blindée : pendant 1 seconde, les deux moitiés de la porte (fermée) tournent
 * autour des gonds. Le calcul se fait à chaque image (120 images si le jeu tourne à 120 FPS), avec une accélération
 * au départ et un ralentissement à l'arrivée, comme une vraie porte lourde.
 */
public class ArmoredDoorRenderer extends SafeBlockEntityRenderer<ArmoredDoorBlockEntity> {
	public ArmoredDoorRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(ArmoredDoorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState lower = be.getBlockState();
		if (!(lower.getBlock() instanceof ArmoredDoorBlock) || lower.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER)
			return;

		float now = AnimationTickHolder.getRenderTime(be.getLevel());
		float progress = be.swingProgress(lower.getValue(DoorBlock.OPEN), now);
		// Le modèle fixe est affiché quand la porte ne bouge pas.
		if (lower.getValue(ArmoredDoorBlock.VISIBLE))
			return;

		BlockState closedLower = lower.setValue(DoorBlock.OPEN, false).setValue(ArmoredDoorBlock.VISIBLE, true);
		BlockState closedUpper = closedLower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
		Hinge hinge = Hinge.of(closedLower);
		float angle = hinge.sign * 90f * ease(progress);

		BlockPos pos = be.getBlockPos();
		int upperLight = LevelRenderer.getLightColor(be.getLevel(), pos.above());
		VertexConsumer consumer = buffer.getBuffer(RenderType.cutoutMipped());
		renderHalf(closedLower, 0, hinge, angle, light, ms, consumer);
		renderHalf(closedUpper, 1, hinge, angle, upperLight, ms, consumer);
	}

	private static void renderHalf(BlockState state, float yOffset, Hinge hinge, float angle, int light, PoseStack ms,
		VertexConsumer consumer) {
		SuperByteBuffer model = CachedBuffers.block(state);
		model.translate(0, yOffset, 0)
			.translate(hinge.x, 0, hinge.z)
			.rotateYDegrees(angle)
			.translate(-hinge.x, 0, -hinge.z)
			.light(light)
			.renderInto(ms, consumer);
	}

	/** Accélère au début, ralentit à la fin (porte lourde). */
	static float ease(float t) {
		return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
	}

	/**
	 * Position des gonds et sens de rotation, déduits des formes de la porte de Minecraft :
	 * les gonds sont dans le coin commun à la porte fermée et à la porte ouverte.
	 */
	record Hinge(float x, float z, float sign) {
		static Hinge of(BlockState closed) {
			AABB shut = closed.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
			AABB open = closed.setValue(DoorBlock.OPEN, true).getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
			float x = (float) (Math.max(shut.minX, open.minX) + Math.min(shut.maxX, open.maxX)) / 2;
			float z = (float) (Math.max(shut.minZ, open.minZ) + Math.min(shut.maxZ, open.maxZ)) / 2;
			// Direction gond -> bout de la porte, fermée puis ouverte
			double cx = (shut.minX + shut.maxX) / 2 - x, cz = (shut.minZ + shut.maxZ) / 2 - z;
			double ox = (open.minX + open.maxX) / 2 - x, oz = (open.minZ + open.maxZ) / 2 - z;
			// Une rotation de +90° autour de Y envoie (x, z) sur (z, -x)
			float sign = cz * ox - cx * oz >= 0 ? 1 : -1;
			return new Hinge(x, z, sign);
		}
	}

	@Override
	public AABB getRenderBoundingBox(ArmoredDoorBlockEntity be) {
		BlockPos pos = be.getBlockPos();
		return new AABB(pos).inflate(1, 0, 1).expandTowards(0, 1, 0);
	}
}
