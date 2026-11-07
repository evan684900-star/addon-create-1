package io.github.evan684900star.locksmithing.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Base commune : une entrée, une flèche, une sortie, et une ligne de texte en dessous. */
abstract class SimpleCategory<T> implements IRecipeCategory<T> {
	protected static final int WIDTH = 116, HEIGHT = 44;
	protected static final int IN_X = 14, OUT_X = 86, SLOT_Y = 6, ARROW_X = 44, ARROW_Y = 6;

	private final RecipeType<T> type;
	private final Component title;
	private final IDrawable icon;
	private final IDrawableStatic arrow;

	protected SimpleCategory(IGuiHelper gui, RecipeType<T> type, Component title, ItemStack icon) {
		this.type = type;
		this.title = title;
		this.icon = gui.createDrawableItemStack(icon);
		this.arrow = gui.getRecipeArrow();
	}

	@Override
	public RecipeType<T> getRecipeType() {
		return type;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	protected void drawArrow(GuiGraphics graphics) {
		arrow.draw(graphics, ARROW_X, ARROW_Y);
	}

	/** Texte gris centré sous la recette. */
	protected void drawNote(GuiGraphics graphics, Component text, int line) {
		var font = Minecraft.getInstance().font;
		graphics.drawString(font, text, (WIDTH - font.width(text)) / 2, 27 + line * 9, 0xFF808080, false);
	}
}
