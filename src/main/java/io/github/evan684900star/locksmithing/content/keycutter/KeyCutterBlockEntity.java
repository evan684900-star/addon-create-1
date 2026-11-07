package io.github.evan684900star.locksmithing.content.keycutter;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import io.github.evan684900star.locksmithing.recipe.KeyCuttingRecipe;
import io.github.evan684900star.locksmithing.registry.ModDataComponents;
import io.github.evan684900star.locksmithing.registry.ModMenus;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import io.github.evan684900star.locksmithing.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class KeyCutterBlockEntity extends KineticBlockEntity implements MenuProvider {
	public static final int SLOT_INPUT = 0;
	public static final int SLOT_OUTPUT = 1;
	/** Travail nécessaire pour tailler une clé : à 32 tr/min, environ 3,5 secondes. */
	public static final int WORK = 2240;

	final ItemStackHandler inventory = new ItemStackHandler(2) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == SLOT_INPUT && level != null && !ModRecipeTypes.findKeyCutting(level, stack).isEmpty();
		}

		@Override
		protected void onContentsChanged(int slot) {
			if (slot == SLOT_INPUT && getSelectedRecipe() == null)
				progress = 0;
			setChanged();
			if (level != null && !level.isClientSide)
				sendData();
		}
	};

	private final IItemHandler itemCapability = new KeyCutterItemHandler(inventory);

	private int selected = -1;
	private int progress;
	/** Vrai pendant la taille (synchronisé avec le client pour les étincelles). */
	private boolean cutting;

	final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> progress * 100 / WORK;
				case 1 -> selected;
				case 2 -> getSpeed() != 0 ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			if (index == 1)
				selected = value;
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public KeyCutterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public IItemHandler getItemCapability() {
		return itemCapability;
	}

	public List<RecipeHolder<KeyCuttingRecipe>> getAvailableRecipes() {
		return level == null ? List.of() : ModRecipeTypes.findKeyCutting(level, inventory.getStackInSlot(SLOT_INPUT));
	}

	@Nullable
	public RecipeHolder<KeyCuttingRecipe> getSelectedRecipe() {
		List<RecipeHolder<KeyCuttingRecipe>> recipes = getAvailableRecipes();
		if (recipes.isEmpty())
			return null;
		// Une seule clé possible : pas besoin de la choisir.
		if (recipes.size() == 1)
			return recipes.get(0);
		return selected >= 0 && selected < recipes.size() ? recipes.get(selected) : null;
	}

	public void select(int index) {
		if (index == selected)
			return;
		selected = index;
		progress = 0;
		setChanged();
		sendData();
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null)
			return;
		if (level.isClientSide) {
			if (cutting)
				spawnSparks();
			return;
		}

		RecipeHolder<KeyCuttingRecipe> recipe = getSelectedRecipe();
		float speed = Math.abs(getSpeed());
		if (recipe == null || speed == 0 || !canOutput(recipe.value().result())) {
			progress = 0;
			setCutting(false);
			return;
		}

		setCutting(true);
		progress += Mth.clamp((int) speed, 1, 512);
		if (level.getGameTime() % 20 == 0)
			level.playSound(null, worldPosition, ModSounds.KEY_CUTTER_CUT.get(), SoundSource.BLOCKS, 0.5f,
				0.9f + 0.2f * level.random.nextFloat());
		if (progress < WORK)
			return;

		progress = 0;
		ItemStack key = recipe.value().result().copy();
		// Chaque clé taillée est unique.
		key.set(ModDataComponents.KEY_ID.get(), UUID.randomUUID());
		inventory.extractItem(SLOT_INPUT, 1, false);
		ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
		if (output.isEmpty())
			inventory.setStackInSlot(SLOT_OUTPUT, key);
		level.playSound(null, worldPosition, ModSounds.KEY_CUTTER_DONE.get(), SoundSource.BLOCKS, 0.7f, 1.0f);
	}

	private void setCutting(boolean value) {
		if (cutting == value)
			return;
		cutting = value;
		sendData();
	}

	/** Étincelles là où la lame touche la clé. */
	private void spawnSparks() {
		if (level.random.nextFloat() > 0.4f)
			return;
		Direction facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
		Vec3 saw = Vec3.atCenterOf(worldPosition).add(KeyCutterBlock.sawOffset(facing));
		Vec3 contact = saw.add(-facing.getStepX() * 0.3, -0.05, -facing.getStepZ() * 0.3);
		level.addParticle(ParticleTypes.ELECTRIC_SPARK, contact.x, contact.y, contact.z,
			(level.random.nextDouble() - 0.5) * 0.2 - facing.getStepX() * 0.1, 0.1 + level.random.nextDouble() * 0.1,
			(level.random.nextDouble() - 0.5) * 0.2 - facing.getStepZ() * 0.1);
	}

	/** La clé à afficher sur l'enclume : celle en cours de taille, sinon celle qui vient d'être taillée. */
	public ItemStack getDisplayedKey() {
		ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
		return input.isEmpty() ? inventory.getStackInSlot(SLOT_OUTPUT) : input.copyWithCount(1);
	}

	private boolean canOutput(ItemStack result) {
		// Les clés ne s'empilent pas : il faut retirer la précédente.
		return inventory.getStackInSlot(SLOT_OUTPUT).isEmpty() && !result.isEmpty();
	}

	public void dropContents() {
		if (level == null)
			return;
		for (int i = 0; i < inventory.getSlots(); i++)
			Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
				inventory.getStackInSlot(i));
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.create_locksmithing.key_cutter");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new KeyCutterMenu(ModMenus.KEY_CUTTER.get(), containerId, playerInventory, this, dataAccess);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.put("Inventory", inventory.serializeNBT(registries));
		tag.putInt("Selected", selected);
		tag.putInt("Progress", progress);
		tag.putBoolean("Cutting", cutting);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
		selected = tag.contains("Selected") ? tag.getInt("Selected") : -1;
		progress = tag.getInt("Progress");
		cutting = tag.getBoolean("Cutting");
	}

	/** Entonnoirs/trémies : clés vierges dedans, clés taillées dehors. */
	private record KeyCutterItemHandler(ItemStackHandler inner) implements IItemHandler {
		@Override
		public int getSlots() {
			return 2;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return inner.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return slot == SLOT_INPUT ? inner.insertItem(slot, stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return slot == SLOT_OUTPUT ? inner.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return inner.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return inner.isItemValid(slot, stack);
		}
	}
}
