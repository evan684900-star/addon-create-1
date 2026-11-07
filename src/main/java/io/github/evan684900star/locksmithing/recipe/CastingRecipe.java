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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Enclume de moulage : du métal en fusion -> une barre. */
public record CastingRecipe(SizedFluidIngredient fluid, ItemStack result, int time) implements Recipe<CastingRecipe.Input> {
	public static final MapCodec<CastingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid").forGetter(CastingRecipe::fluid),
		ItemStack.CODEC.fieldOf("result").forGetter(CastingRecipe::result),
		Codec.INT.optionalFieldOf("time", 60).forGetter(CastingRecipe::time)
	).apply(i, CastingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC = StreamCodec.composite(
		SizedFluidIngredient.STREAM_CODEC, CastingRecipe::fluid,
		ItemStack.STREAM_CODEC, CastingRecipe::result,
		ByteBufCodecs.VAR_INT, CastingRecipe::time,
		CastingRecipe::new);

	/** Le fluide présent dans le moule. */
	public record Input(FluidStack fluid) implements RecipeInput {
		@Override
		public ItemStack getItem(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public int size() {
			return 0;
		}
	}

	/** Vrai si ce fluide (peu importe la quantité) correspond à cette recette. */
	public boolean acceptsFluid(FluidStack stack) {
		return !stack.isEmpty() && fluid.ingredient().test(stack);
	}

	@Override
	public boolean matches(Input input, Level level) {
		return fluid.test(input.fluid());
	}

	@Override
	public ItemStack assemble(Input input, HolderLookup.Provider registries) {
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
		return ModRecipeTypes.CASTING_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipeTypes.CASTING.get();
	}
}
