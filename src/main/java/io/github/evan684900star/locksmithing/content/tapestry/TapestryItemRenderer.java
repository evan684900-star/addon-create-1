package io.github.evan684900star.locksmithing.content.tapestry;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

/** Icône de la tapisserie : dessinée en direct, elle montre les motifs ajoutés au métier à tisser. */
public class TapestryItemRenderer extends BlockEntityWithoutLevelRenderer {
	public static final TapestryItemRenderer INSTANCE = new TapestryItemRenderer();

	private ModelPart flag;
	private ModelPart bar;

	private TapestryItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		flag = null;
		bar = null;
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (flag == null) {
			ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BANNER);
			flag = root.getChild("flag");
			bar = root.getChild("bar");
		}
		BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		float k = 0.58f;
		ms.pushPose();
		// Centre la tapisserie (barre + cuir) dans la case de l'objet
		ms.translate(0.5f, 0.5f - 0.5f * k, 0.5f);
		ms.scale(k, k, k);
		TapestryRenderer.render(ms, buffer, light, overlay, flag, bar, 0f, patterns);
		ms.popPose();
	}
}
