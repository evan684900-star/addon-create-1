package io.github.evan684900star.locksmithing.content.tapestry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.evan684900star.locksmithing.Locksmithing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

/**
 * Peint du cuir de tapisserie avec les motifs du métier à tisser, comme une bannière : le cuir d'abord, puis chaque
 * motif teint par-dessus, sur la même forme. Sert à la tapisserie au mur, à son icône et à la porte blindée.
 */
public final class TapestryPaint {
	/** Texture du cuir, dans l'atlas des bannières (textures/entity/banner/tapestry_leather.png). */
	public static final Material LEATHER = new Material(Sheets.BANNER_SHEET,
		Locksmithing.asResource("entity/banner/tapestry_leather"));

	/** Face avant du drapeau dans les textures de bannière (64 x 64) : de (1, 1) à (21, 41). */
	public static final float U_LEFT = 1 / 64f, U_RIGHT = 21 / 64f, V_TOP = 1 / 64f, V_MIDDLE = 21 / 64f,
		V_BOTTOM = 41 / 64f;

	private TapestryPaint() {}

	/** Dessine une couche (le cuir ou un motif) avec la couleur donnée. */
	@FunctionalInterface
	public interface Layer {
		void draw(VertexConsumer consumer, int color);
	}

	public static void paint(MultiBufferSource buffer, BannerPatternLayers patterns, Layer layer) {
		layer.draw(LEATHER.buffer(buffer, RenderType::entitySolid), -1);
		for (int i = 0; i < 16 && i < patterns.layers().size(); i++) {
			BannerPatternLayers.Layer pattern = patterns.layers().get(i);
			layer.draw(Sheets.getBannerMaterial(pattern.pattern()).buffer(buffer, RenderType::entityNoOutline),
				pattern.color().getTextureDiffuseColor());
		}
	}

	/** Un sommet ; u et v sont en fraction de la texture de bannière (le matériau les place dans l'atlas). */
	public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v,
		int color, int light, int overlay, float nx, float ny, float nz) {
		consumer.addVertex(pose, x, y, z)
			.setColor(color)
			.setUv(u, v)
			.setOverlay(overlay)
			.setLight(light)
			.setNormal(pose, nx, ny, nz);
	}
}
