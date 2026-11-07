package io.github.evan684900star.locksmithing.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** Machine à tailler les clés : clé vierge -> clé pour un type de porte (choisie dans l'interface). */
public record KeyCuttingRecipe(Ingredient ingredient, ItemStack result) implements Recipe<SingleRecipeInput> {
	public static final MapCodec<KeyCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(KeyCuttingRecipe::ingredient),
		ItemStack.CODEC.fieldOf("result").forGetter(KeyCuttingRecipe::result)
	).apply(i, KeyCuttingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, KeyCuttingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, KeyCuttingRecipe::ingredient,
		ItemStack.STREAM_CODEC, KeyCuttingRecipe::result,
		KeyCuttingRecipe::new);

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return ingredient.test(input.item());
	}

	@Override
	public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
		return result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return result;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipeTypes.KEY_CUTTING_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipeTypes.KEY_CUTTING.get();
	}
}
