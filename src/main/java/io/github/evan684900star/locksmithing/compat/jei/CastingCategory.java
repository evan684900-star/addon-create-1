package io.github.evan684900star.locksmithing.compat.jei;

import java.util.Arrays;

import io.github.evan684900star.locksmithing.recipe.CastingRecipe;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Enclume de moulage : métal en fusion -> barre. */
class CastingCategory extends SimpleCategory<RecipeHolder<CastingRecipe>> {
	CastingCategory(IGuiHelper gui) {
		super(gui, LocksmithingJeiPlugin.CASTING, Component.translatable("block.create_locksmithing.casting_anvil"),
			ModBlocks.CASTING_ANVIL.asStack());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CastingRecipe> holder, IFocusGroup focuses) {
		CastingRecipe recipe = holder.value();
		int amount = recipe.fluid().amount();
		builder.addSlot(RecipeIngredientRole.INPUT, IN_X, SLOT_Y)
			.setStandardSlotBackground()
			.setFluidRenderer(amount, false, 16, 16)
			.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.fluid().getFluids()));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X, SLOT_Y)
			.setStandardSlotBackground()
			.addItemStack(recipe.result());
	}

	@Override
	public void draw(RecipeHolder<CastingRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX,
		double mouseY) {
		drawArrow(graphics);
		drawNote(graphics, seconds(holder.value().time()), 0);
	}
}
