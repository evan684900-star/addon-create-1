package io.github.evan684900star.locksmithing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/** Four à moufle : un lingot -> du métal en fusion. */
public record MeltingRecipe(Ingredient ingredient, FluidStack result, int time) implements Recipe<SingleRecipeInput> {
	public static final MapCodec<MeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MeltingRecipe::ingredient),
		FluidStack.CODEC.fieldOf("result").forGetter(MeltingRecipe::result),
		Codec.INT.optionalFieldOf("time", 100).forGetter(MeltingRecipe::time)
	).apply(i, MeltingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, MeltingRecipe::ingredient,
		FluidStack.STREAM_CODEC, MeltingRecipe::result,
		ByteBufCodecs.VAR_INT, MeltingRecipe::time,
		MeltingRecipe::new);

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return ingredient.test(input.item());
	}

	@Override
	public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipeTypes.MELTING_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipeTypes.MELTING.get();
	}
}
