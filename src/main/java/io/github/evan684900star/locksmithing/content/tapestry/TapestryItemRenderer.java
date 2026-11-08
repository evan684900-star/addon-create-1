package io.github.evan684900star.locksmithing.content.tapestry;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

/** Icône de la tapisserie : le panneau de cuir dessiné en direct, avec les motifs ajoutés au métier à tisser. */
public class TapestryItemRenderer extends BlockEntityWithoutLevelRenderer {
	public static final TapestryItemRenderer INSTANCE = new TapestryItemRenderer();

	private TapestryItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		ms.pushPose();
		// Le panneau (1 x 2) réduit de moitié et centré dans la case de l'objet
		ms.translate(0.25f, 0f, 0.5f - TapestryRenderer.THICKNESS / 4);
		ms.scale(0.5f, 0.5f, 0.5f);
		TapestryRenderer.renderPanel(ms.last(), buffer, patterns, light, light, overlay);
		ms.popPose();
	}
}
