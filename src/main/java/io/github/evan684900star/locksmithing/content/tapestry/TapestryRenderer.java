package io.github.evan684900star.locksmithing.content.tapestry;

import static io.github.evan684900star.locksmithing.content.tapestry.TapestryPaint.U_LEFT;
import static io.github.evan684900star.locksmithing.content.tapestry.TapestryPaint.U_RIGHT;
import static io.github.evan684900star.locksmithing.content.tapestry.TapestryPaint.V_BOTTOM;
import static io.github.evan684900star.locksmithing.content.tapestry.TapestryPaint.V_MIDDLE;
import static io.github.evan684900star.locksmithing.content.tapestry.TapestryPaint.V_TOP;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

/**
 * Rendu de la tapisserie : un panneau de cuir de 1 x 2 blocs collé à plat contre le mur, comme du papier peint,
 * avec les motifs du métier à tisser.
 */
public class TapestryRenderer implements BlockEntityRenderer<TapestryBlockEntity> {
	/** Épaisseur du cuir (1 pixel). */
	public static final float THICKNESS = 1 / 16f;

	public TapestryRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public void render(TapestryBlockEntity be, float partialTick, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		BlockState state = be.getBlockState();
		if (!(state.getBlock() instanceof TapestryBlock) || state.getValue(TapestryBlock.HALF) != DoubleBlockHalf.LOWER)
			return;
		int upperLight = be.getLevel() == null ? light : LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
		ms.pushPose();
		ms.translate(0.5f, 0, 0.5f);
		ms.mulPose(Axis.YP.rotationDegrees(-state.getValue(TapestryBlock.FACING).toYRot()));
		ms.translate(-0.5f, 0, -0.5f);
		renderPanel(ms.last(), buffer, be.getPatterns(), light, upperLight, overlay);
		ms.popPose();
	}

	/**
	 * Le panneau : x de 0 à 1 (de gauche à droite vu de devant), y de 0 à 2, collé contre z = 0, face avant vers +z.
	 * Chaque moitié prend la lumière de son bloc.
	 */
	public static void renderPanel(PoseStack.Pose pose, MultiBufferSource buffer, BannerPatternLayers patterns,
		int lowerLight, int upperLight, int overlay) {
		float t = THICKNESS;
		TapestryPaint.paint(buffer, patterns, (c, color) -> {
			for (int half = 0; half < 2; half++) {
				float y0 = half, y1 = half + 1;
				int light = half == 0 ? lowerLight : upperLight;
				float vb = half == 0 ? V_BOTTOM : V_MIDDLE, vt = half == 0 ? V_MIDDLE : V_TOP;
				Quad q = new Quad(c, pose, color, light, overlay);
				q.draw(0, 0, 1, new float[] { 0, y0, t, U_LEFT, vb, 1, y0, t, U_RIGHT, vb, 1, y1, t, U_RIGHT, vt, 0, y1, t, U_LEFT, vt });
				q.draw(-1, 0, 0, new float[] { 0, y0, 0, 0, vb, 0, y0, t, U_LEFT, vb, 0, y1, t, U_LEFT, vt, 0, y1, 0, 0, vt });
				q.draw(1, 0, 0, new float[] { 1, y0, t, U_RIGHT, vb, 1, y0, 0, 22 / 64f, vb, 1, y1, 0, 22 / 64f, vt, 1, y1, t, U_RIGHT, vt });
			}
			float top = 1 / 64f;
			new Quad(c, pose, color, upperLight, overlay)
				.draw(0, 1, 0, new float[] { 0, 2, t, U_LEFT, top, 1, 2, t, U_RIGHT, top, 1, 2, 0, U_RIGHT, 0, 0, 2, 0, U_LEFT, 0 });
			new Quad(c, pose, color, lowerLight, overlay)
				.draw(0, -1, 0, new float[] { 0, 0, 0, 21 / 64f, 0, 1, 0, 0, 41 / 64f, 0, 1, 0, t, 41 / 64f, top, 0, 0, t, 21 / 64f, top });
		});
	}

	/** Quatre sommets (x, y, z, u, v) dans le sens inverse des aiguilles d'une montre vus de l'extérieur. */
	private record Quad(VertexConsumer consumer, PoseStack.Pose pose, int color, int light, int overlay) {
		void draw(float nx, float ny, float nz, float[] v) {
			for (int i = 0; i < 4; i++) {
				int k = i * 5;
				TapestryPaint.vertex(consumer, pose, v[k], v[k + 1], v[k + 2], v[k + 3], v[k + 4], color, light, overlay,
					nx, ny, nz);
			}
		}
	}

	@Override
	public AABB getRenderBoundingBox(TapestryBlockEntity be) {
		return new AABB(be.getBlockPos()).expandTowards(0, 1, 0);
	}
}
