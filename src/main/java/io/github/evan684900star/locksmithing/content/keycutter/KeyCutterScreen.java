package io.github.evan684900star.locksmithing.content.keycutter;

import java.util.List;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.recipe.KeyCuttingRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Interface façon tailleur de pierre : on choisit la clé à tailler dans la liste. */
public class KeyCutterScreen extends AbstractContainerScreen<KeyCutterMenu> {
	private static final ResourceLocation TEXTURE = Locksmithing.asResource("textures/gui/key_cutter.png");
	private static final int GRID_X = 52, GRID_Y = 15, COLUMNS = 4, ROWS = 2, BUTTON_W = 16, BUTTON_H = 18;
	/** Le titre est un peu plus haut et plus petit que d'habitude (il est long). */
	private static final int TITLE_Y = 4;
	private static final float TITLE_SCALE = 0.8f;
	private static final int ARROW_X = 118, ARROW_Y = 34, ARROW_W = 20, ARROW_H = 15;

	public KeyCutterScreen(KeyCutterMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		imageWidth = 176;
		imageHeight = 166;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderRecipeTooltip(graphics, mouseX, mouseY);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

		List<RecipeHolder<KeyCuttingRecipe>> recipes = menu.getRecipes();
		int selected = menu.getSelected();
		for (int i = 0; i < Math.min(recipes.size(), COLUMNS * ROWS); i++) {
			int bx = x + GRID_X + (i % COLUMNS) * BUTTON_W;
			int by = y + GRID_Y + (i / COLUMNS) * BUTTON_H;
			boolean hovered = mouseX >= bx && mouseY >= by && mouseX < bx + BUTTON_W && mouseY < by + BUTTON_H;
			int v = i == selected ? 18 : hovered ? 36 : 0;
			graphics.blit(TEXTURE, bx, by, 176, v, BUTTON_W, BUTTON_H);
			ItemStack result = recipes.get(i).value().result();
			graphics.renderItem(result, bx, by + 1);
		}

		int progress = menu.getProgressPercent() * ARROW_W / 100;
		if (progress > 0)
			graphics.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, 192, 0, progress, ARROW_H);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.pose().pushPose();
		graphics.pose().translate(titleLabelX, TITLE_Y, 0);
		graphics.pose().scale(TITLE_SCALE, TITLE_SCALE, 1);
		graphics.drawString(font, title, 0, 0, 0x404040, false);
		graphics.pose().popPose();
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
		// Sous la liste des clés, loin du titre "Inventaire"
		if (!menu.isPowered())
			graphics.drawString(font, Component.translatable("gui.create_locksmithing.key_cutter.no_rotation"),
				GRID_X, GRID_Y + ROWS * BUTTON_H + 4, 0xB02E26, false);
	}

	private void renderRecipeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		List<RecipeHolder<KeyCuttingRecipe>> recipes = menu.getRecipes();
		for (int i = 0; i < Math.min(recipes.size(), COLUMNS * ROWS); i++) {
			int bx = leftPos + GRID_X + (i % COLUMNS) * BUTTON_W;
			int by = topPos + GRID_Y + (i / COLUMNS) * BUTTON_H;
			if (mouseX >= bx && mouseY >= by && mouseX < bx + BUTTON_W && mouseY < by + BUTTON_H)
				graphics.renderTooltip(font, recipes.get(i).value().result(), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		List<RecipeHolder<KeyCuttingRecipe>> recipes = menu.getRecipes();
		for (int i = 0; i < Math.min(recipes.size(), COLUMNS * ROWS); i++) {
			int bx = leftPos + GRID_X + (i % COLUMNS) * BUTTON_W;
			int by = topPos + GRID_Y + (i / COLUMNS) * BUTTON_H;
			if (mouseX >= bx && mouseY >= by && mouseX < bx + BUTTON_W && mouseY < by + BUTTON_H
				&& menu.clickMenuButton(minecraft.player, i)) {
				minecraft.getSoundManager()
					.play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
						net.minecraft.sounds.SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
				minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
