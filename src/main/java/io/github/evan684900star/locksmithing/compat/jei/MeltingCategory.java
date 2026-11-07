package io.github.evan684900star.locksmithing.compat.jei;

import io.github.evan684900star.locksmithing.recipe.MeltingRecipe;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/** Four à moufle : lingot -> métal en fusion. */
class MeltingCategory extends SimpleCategory<RecipeHolder<MeltingRecipe>> {
	MeltingCategory(IGuiHelper gui) {
		super(gui, LocksmithingJeiPlugin.MELTING, Component.translatable("block.create_locksmithing.muffle_furnace"),
			ModBlocks.MUFFLE_FURNACE.asStack());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MeltingRecipe> holder, IFocusGroup focuses) {
		MeltingRecipe recipe = holder.value();
		builder.addSlot(RecipeIngredientRole.INPUT, IN_X, SLOT_Y)
			.setStandardSlotBackground()
			.addIngredients(recipe.ingredient());
		FluidStack result = recipe.result();
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X, SLOT_Y)
			.setStandardSlotBackground()
			.setFluidRenderer(result.getAmount(), false, 16, 16)
			.addFluidStack(result.getFluid(), result.getAmount());
	}

	@Override
	public void draw(RecipeHolder<MeltingRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX,
		double mouseY) {
		drawArrow(graphics);
		drawNote(graphics, Component.translatable("jei.create_locksmithing.heated"), 0);
		drawNote(graphics, Component.translatable("jei.create_locksmithing.seconds", holder.value().time() / 20f), 1);
	}
}
