package io.github.evan684900star.locksmithing.content.casting;

import java.util.List;

import io.github.evan684900star.locksmithing.Locksmithing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class CastingAnvilScreen extends AbstractContainerScreen<CastingAnvilMenu> {
	private static final ResourceLocation TEXTURE = Locksmithing.asResource("textures/gui/casting_anvil.png");
	// Jauge du moule (fluide) et flèche de progression dans la texture
	private static final int GAUGE_X = 58, GAUGE_Y = 18, GAUGE_W = 14, GAUGE_H = 50;
	private static final int ARROW_X = 82, ARROW_Y = 34, ARROW_W = 24, ARROW_H = 17;

	public CastingAnvilScreen(CastingAnvilMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		imageWidth = 176;
		imageHeight = 166;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
		if (isHovering(GAUGE_X, GAUGE_Y, GAUGE_W, GAUGE_H, mouseX, mouseY))
			graphics.renderComponentTooltip(font, gaugeTooltip(), mouseX, mouseY);
	}

	private List<Component> gaugeTooltip() {
		Fluid fluid = menu.getFluid();
		int amount = menu.getFluidAmount();
		if (fluid == Fluids.EMPTY || amount <= 0)
			return List.of(Component.translatable("gui.create_locksmithing.casting_anvil.empty"));
		return List.of(new FluidStack(fluid, 1).getHoverName(),
			Component.literal(amount + " / " + CastingAnvilBlockEntity.CAPACITY + " mB").withStyle(ChatFormatting.GRAY));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

		// Métal en fusion dans le moule
		Fluid fluid = menu.getFluid();
		int amount = menu.getFluidAmount();
		if (fluid != Fluids.EMPTY && amount > 0) {
			int height = Math.max(1, amount * GAUGE_H / CastingAnvilBlockEntity.CAPACITY);
			renderFluid(graphics, fluid, x + GAUGE_X, y + GAUGE_Y + GAUGE_H - height, GAUGE_W, height);
		}
		// Graduations par-dessus le fluide
		graphics.blit(TEXTURE, x + GAUGE_X, y + GAUGE_Y, 176, 17, GAUGE_W, GAUGE_H);

		// Flèche de progression
		int progress = menu.getProgress() * ARROW_W / menu.getMaxProgress();
		if (progress > 0)
			graphics.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, 176, 0, progress, ARROW_H);
	}

	private void renderFluid(GuiGraphics graphics, Fluid fluid, int x, int y, int width, int height) {
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid);
		TextureAtlasSprite sprite = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(ext.getStillTexture());
		int color = ext.getTintColor();
		graphics.setColor((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, 1f);
		for (int dy = 0; dy < height; dy += 16) {
			int h = Math.min(16, height - dy);
			for (int dx = 0; dx < width; dx += 16) {
				int w = Math.min(16, width - dx);
				graphics.blit(x + dx, y + height - dy - h, 0, w, h, sprite);
			}
		}
		graphics.setColor(1f, 1f, 1f, 1f);
	}
}
