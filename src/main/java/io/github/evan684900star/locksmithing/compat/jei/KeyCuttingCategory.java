package io.github.evan684900star.locksmithing.compat.jei;

import io.github.evan684900star.locksmithing.recipe.KeyCuttingRecipe;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Machine à tailler les clés : clé vierge -> clé. */
class KeyCuttingCategory extends SimpleCategory<RecipeHolder<KeyCuttingRecipe>> {
	KeyCuttingCategory(IGuiHelper gui) {
		super(gui, LocksmithingJeiPlugin.KEY_CUTTING, Component.translatable("block.create_locksmithing.key_cutter"),
			ModBlocks.KEY_CUTTER.asStack());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<KeyCuttingRecipe> holder, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, IN_X, SLOT_Y)
			.setStandardSlotBackground()
			.addIngredients(holder.value().ingredient());
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X, SLOT_Y)
			.setStandardSlotBackground()
			.addItemStack(holder.value().result());
	}

	@Override
	public void draw(RecipeHolder<KeyCuttingRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics,
		double mouseX, double mouseY) {
		drawArrow(graphics);
		drawNote(graphics, Component.translatable("jei.create_locksmithing.rotation"), 0);
	}
}
