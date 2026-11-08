package io.github.evan684900star.locksmithing.content.tapestry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.evan684900star.locksmithing.Locksmithing;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;

/**
 * Rendu de la tapisserie : la forme de la bannière murale de Minecraft, en cuir, avec les motifs du métier à tisser.
 */
public class TapestryRenderer implements BlockEntityRenderer<TapestryBlockEntity> {
	/** Texture du cuir, dans l'atlas des bannières (textures/entity/banner/tapestry_leather.png). */
	public static final Material LEATHER = new Material(Sheets.BANNER_SHEET, Locksmithing.asResource("entity/banner/tapestry_leather"));

	private final ModelPart flag;
	private final ModelPart bar;

	public TapestryRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(ModelLayers.BANNER);
		flag = root.getChild("flag");
		bar = root.getChild("bar");
	}

	@Override
	public void render(TapestryBlockEntity be, float partialTick, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		ms.pushPose();
		ms.translate(0.5f, -1 / 6f, 0.5f);
		ms.mulPose(Axis.YP.rotationDegrees(-be.getBlockState().getValue(WallBannerBlock.FACING).toYRot()));
		ms.translate(0f, -0.3125f, -0.4375f);
		// Le cuir est lourd : elle bouge à peine.
		long time = be.getLevel() == null ? 0 : be.getLevel().getGameTime();
		BlockPos pos = be.getBlockPos();
		float phase = (Math.floorMod(pos.getX() * 7L + pos.getY() * 9L + pos.getZ() * 13L + time, 100L) + partialTick) / 100f;
		float sway = (-0.0125f + 0.004f * Mth.cos(Mth.TWO_PI * phase)) * Mth.PI;
		render(ms, buffer, light, overlay, flag, bar, sway, be.getPatterns());
		ms.popPose();
	}

	/** Dessine la barre et le cuir, puis chaque motif teint par-dessus (comme une bannière). */
	public static void render(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ModelPart flag, ModelPart bar,
		float sway, BannerPatternLayers patterns) {
		ms.pushPose();
		ms.scale(2 / 3f, -2 / 3f, -2 / 3f);
		VertexConsumer leather = LEATHER.buffer(buffer, RenderType::entitySolid);
		bar.render(ms, leather, light, overlay);
		flag.xRot = sway;
		flag.y = -32f;
		flag.render(ms, leather, light, overlay);
		for (int i = 0; i < 16 && i < patterns.layers().size(); i++) {
			BannerPatternLayers.Layer layer = patterns.layers().get(i);
			Material material = Sheets.getBannerMaterial(layer.pattern());
			flag.render(ms, material.buffer(buffer, RenderType::entityNoOutline), light, overlay,
				layer.color().getTextureDiffuseColor());
		}
		ms.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(TapestryBlockEntity be) {
		return AABB.encapsulatingFullBlocks(be.getBlockPos(), be.getBlockPos().below());
	}
}
