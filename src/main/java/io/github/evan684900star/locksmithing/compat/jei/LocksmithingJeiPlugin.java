package io.github.evan684900star.locksmithing.compat.jei;

import com.simibubi.create.AllBlocks;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.recipe.CastingRecipe;
import io.github.evan684900star.locksmithing.recipe.KeyCuttingRecipe;
import io.github.evan684900star.locksmithing.recipe.MeltingRecipe;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModItems;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Compatibilité JEI (chargée seulement si JEI est installé). Les recettes de presse, compactage, bec verseur
 * et égouttoir apparaissent déjà dans les catégories de Create.
 */
@JeiPlugin
public class LocksmithingJeiPlugin implements IModPlugin {
	public static final RecipeType<RecipeHolder<MeltingRecipe>> MELTING =
		RecipeType.createRecipeHolderType(Locksmithing.asResource("melting"));
	public static final RecipeType<RecipeHolder<CastingRecipe>> CASTING =
		RecipeType.createRecipeHolderType(Locksmithing.asResource("casting"));
	public static final RecipeType<RecipeHolder<KeyCuttingRecipe>> KEY_CUTTING =
		RecipeType.createRecipeHolderType(Locksmithing.asResource("key_cutting"));

	/** Accès à JEI une fois chargé (utilisé par la visite automatique avec captures). */
	public static IJeiRuntime runtime;

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return Locksmithing.asResource("jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new MeltingCategory(gui), new CastingCategory(gui), new KeyCuttingCategory(gui));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (Minecraft.getInstance().level == null)
			return;
		RecipeManager recipes = Minecraft.getInstance().level.getRecipeManager();
		registration.addRecipes(MELTING, recipes.getAllRecipesFor(ModRecipeTypes.MELTING.get()));
		registration.addRecipes(CASTING, recipes.getAllRecipesFor(ModRecipeTypes.CASTING.get()));
		registration.addRecipes(KEY_CUTTING, recipes.getAllRecipesFor(ModRecipeTypes.KEY_CUTTING.get()));

		for (var door : ModBlocks.ARMORED_DOORS) {
			var metal = door.get().getMetal();
			Component info = Component.translatable("jei.create_locksmithing.info.armored_door", metal.breakSeconds(),
				Component.translatable(metal.minimumAxe().getDescriptionId()));
			if (metal.oxidizes())
				registration.addItemStackInfo(door.asStack(), info,
					Component.translatable("jei.create_locksmithing.info.armored_door.oxidizes"));
			else
				registration.addItemStackInfo(door.asStack(), info);
			registration.addItemStackInfo(ModItems.keyFor(metal).asStack(),
				Component.translatable("jei.create_locksmithing.info.key"));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(ModBlocks.MUFFLE_FURNACE.asStack(), MELTING);
		registration.addRecipeCatalyst(AllBlocks.BLAZE_BURNER.asStack(), MELTING);
		registration.addRecipeCatalyst(ModBlocks.CASTING_ANVIL.asStack(), CASTING);
		registration.addRecipeCatalyst(ModBlocks.KEY_CUTTER.asStack(), KEY_CUTTING);
	}
}
