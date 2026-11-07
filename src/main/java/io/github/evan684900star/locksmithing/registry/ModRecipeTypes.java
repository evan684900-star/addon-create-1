package io.github.evan684900star.locksmithing.registry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.recipe.CastingRecipe;
import io.github.evan684900star.locksmithing.recipe.KeyCuttingRecipe;
import io.github.evan684900star.locksmithing.recipe.MeltingRecipe;
import io.github.evan684900star.locksmithing.recipe.SimpleRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {
	private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Locksmithing.ID);
	private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
		DeferredRegister.create(Registries.RECIPE_SERIALIZER, Locksmithing.ID);

	public static final DeferredHolder<RecipeType<?>, RecipeType<MeltingRecipe>> MELTING =
		TYPES.register("melting", () -> RecipeType.simple(Locksmithing.asResource("melting")));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MeltingRecipe>> MELTING_SERIALIZER =
		SERIALIZERS.register("melting", () -> new SimpleRecipeSerializer<>(MeltingRecipe.CODEC, MeltingRecipe.STREAM_CODEC));

	public static final DeferredHolder<RecipeType<?>, RecipeType<CastingRecipe>> CASTING =
		TYPES.register("casting", () -> RecipeType.simple(Locksmithing.asResource("casting")));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CastingRecipe>> CASTING_SERIALIZER =
		SERIALIZERS.register("casting", () -> new SimpleRecipeSerializer<>(CastingRecipe.CODEC, CastingRecipe.STREAM_CODEC));

	public static final DeferredHolder<RecipeType<?>, RecipeType<KeyCuttingRecipe>> KEY_CUTTING =
		TYPES.register("key_cutting", () -> RecipeType.simple(Locksmithing.asResource("key_cutting")));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeyCuttingRecipe>> KEY_CUTTING_SERIALIZER =
		SERIALIZERS.register("key_cutting", () -> new SimpleRecipeSerializer<>(KeyCuttingRecipe.CODEC, KeyCuttingRecipe.STREAM_CODEC));

	public static void register(IEventBus modEventBus) {
		TYPES.register(modEventBus);
		SERIALIZERS.register(modEventBus);
	}

	public static Optional<MeltingRecipe> findMelting(Level level, ItemStack stack) {
		if (stack.isEmpty())
			return Optional.empty();
		return level.getRecipeManager()
			.getAllRecipesFor(MELTING.get())
			.stream()
			.map(RecipeHolder::value)
			.filter(r -> r.ingredient().test(stack))
			.findFirst();
	}

	/** La recette de moulage qui correspond à ce fluide (sans regarder la quantité). */
	public static Optional<CastingRecipe> findCasting(Level level, FluidStack fluid) {
		if (fluid.isEmpty())
			return Optional.empty();
		return level.getRecipeManager()
			.getAllRecipesFor(CASTING.get())
			.stream()
			.map(RecipeHolder::value)
			.filter(r -> r.acceptsFluid(fluid))
			.findFirst();
	}

	/** Les clés que l'on peut tailler dans cet objet, triées pour être identiques côté client et serveur. */
	public static List<RecipeHolder<KeyCuttingRecipe>> findKeyCutting(Level level, ItemStack stack) {
		if (stack.isEmpty())
			return List.of();
		return level.getRecipeManager()
			.getAllRecipesFor(KEY_CUTTING.get())
			.stream()
			.filter(r -> r.value().ingredient().test(stack))
			.sorted(Comparator.comparing(r -> r.id().toString()))
			.toList();
	}
}
