package io.github.evan684900star.locksmithing.content.casting;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.fluid.FluidContainers;
import io.github.evan684900star.locksmithing.recipe.CastingRecipe;
import io.github.evan684900star.locksmithing.registry.ModMenus;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CastingAnvilBlockEntity extends SmartBlockEntity implements MenuProvider, IHaveGoggleInformation {
	public static final int CAPACITY = 2000;
	public static final int SLOT_CONTAINER = 0;
	public static final int SLOT_OUTPUT = 1;

	final ItemStackHandler inventory = new ItemStackHandler(2) {
		@Override
		public int getSlotLimit(int slot) {
			return slot == SLOT_CONTAINER ? 1 : 64;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == SLOT_CONTAINER && isCastableContainer(stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			changed();
		}
	};

	private final FluidTank tank = new FluidTank(CAPACITY) {
		@Override
		public boolean isFluidValid(FluidStack stack) {
			return canAccept(stack);
		}

		@Override
		protected void onContentsChanged() {
			changed();
		}
	};

	private final IItemHandler itemCapability = new CastingItemHandler(this);
	private final IFluidHandler fluidCapability = new CastingFluidHandler(this);

	private int progress;
	private int maxProgress = 60;

	/** Valeurs envoyées à l'interface : progression, durée, quantité de fluide, id du fluide. */
	final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> progress;
				case 1 -> maxProgress;
				case 2 -> tank.getFluidAmount();
				case 3 -> tank.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {}

		@Override
		public int getCount() {
			return 4;
		}
	};

	public CastingAnvilBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	public IItemHandler getItemCapability() {
		return itemCapability;
	}

	public IFluidHandler getFluidCapability() {
		return fluidCapability;
	}

	public FluidStack getFluid() {
		return tank.getFluid();
	}

	public ItemStack getOutput() {
		return inventory.getStackInSlot(SLOT_OUTPUT);
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide)
			sendData();
	}

	/* Règles du moule */

	/** Un seau ou une fiole remplie d'un métal que le moule sait couler. */
	public boolean isCastableContainer(ItemStack stack) {
		FluidStack contained = FluidContainers.getContained(stack);
		return !contained.isEmpty() && level != null && ModRecipeTypes.findCasting(level, contained).isPresent();
	}

	/**
	 * Le moule accepte ce métal s'il sait le couler et s'il est vide, contient déjà ce métal,
	 * ou ne contient qu'un reste de moins d'une barre d'un autre métal (ce reste est alors perdu).
	 */
	public boolean canAccept(FluidStack stack) {
		if (stack.isEmpty() || level == null || ModRecipeTypes.findCasting(level, stack).isEmpty())
			return false;
		return tank.isEmpty() || FluidStack.isSameFluidSameComponents(tank.getFluid(), stack)
			|| tank.getFluidAmount() < FluidContainers.INGOT;
	}

	/** Remplit le moule (utilisé par les tuyaux et par le seau/la fiole). Renvoie la quantité acceptée. */
	public int fill(FluidStack stack, IFluidHandler.FluidAction action) {
		if (!canAccept(stack))
			return 0;
		if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(), stack)) {
			if (action.simulate())
				return Math.min(stack.getAmount(), tank.getCapacity());
			tank.setFluid(FluidStack.EMPTY);
		}
		return tank.fill(stack, action);
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;
		pourContainer();
		cast();
	}

	/** Vide le seau / la fiole posé(e) dans l'emplacement de gauche. */
	private void pourContainer() {
		ItemStack container = inventory.getStackInSlot(SLOT_CONTAINER);
		FluidStack contained = FluidContainers.getContained(container);
		if (contained.isEmpty())
			return;
		if (fill(contained, IFluidHandler.FluidAction.SIMULATE) < contained.getAmount())
			return;
		fill(contained, IFluidHandler.FluidAction.EXECUTE);
		inventory.setStackInSlot(SLOT_CONTAINER, FluidContainers.getEmptied(container));
		level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 0.7f, 1f);
	}

	private void cast() {
		Optional<CastingRecipe> recipe = ModRecipeTypes.findCasting(level, tank.getFluid());
		if (recipe.isEmpty() || tank.getFluidAmount() < recipe.get().fluid().amount()) {
			progress = 0;
			return;
		}
		ItemStack result = recipe.get().result();
		ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
		boolean fits = output.isEmpty()
			|| ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
		if (!fits) {
			progress = 0;
			return;
		}

		maxProgress = Math.max(1, recipe.get().time());
		if (++progress < maxProgress)
			return;

		progress = 0;
		tank.drain(recipe.get().fluid().amount(), IFluidHandler.FluidAction.EXECUTE);
		if (output.isEmpty())
			inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
		else {
			ItemStack grown = output.copy();
			grown.grow(result.getCount());
			inventory.setStackInSlot(SLOT_OUTPUT, grown);
		}
		level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.4f);
	}

	/** Pour les tutos Ponder : affiche directement un contenu dans le moule. */
	public void ponderShow(FluidStack fluid, ItemStack output) {
		tank.setFluid(fluid.copy());
		inventory.setStackInSlot(SLOT_OUTPUT, output.copy());
	}

	public void dropContents() {
		if (level == null)
			return;
		for (int i = 0; i < inventory.getSlots(); i++)
			Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
				inventory.getStackInSlot(i));
	}

	/* Interface */

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.create_locksmithing.casting_anvil");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new CastingAnvilMenu(ModMenus.CASTING_ANVIL.get(), containerId, playerInventory, this, dataAccess);
	}

	/* Lunettes d'ingénieur */

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		new LangBuilder(Locksmithing.ID).translate("gui.goggles.casting_anvil")
			.forGoggles(tooltip);
		containedFluidTooltip(tooltip, isPlayerSneaking, fluidCapability);
		return true;
	}

	/* Sauvegarde */

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.put("Inventory", inventory.serializeNBT(registries));
		tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
		tag.putInt("Progress", progress);
		tag.putInt("MaxProgress", maxProgress);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
		tank.readFromNBT(registries, tag.getCompound("Tank"));
		progress = tag.getInt("Progress");
		maxProgress = Math.max(1, tag.getInt("MaxProgress"));
	}

	/** Pour les entonnoirs/trémies : insérer des seaux/fioles pleins, sortir les barres et les récipients vides. */
	private record CastingItemHandler(CastingAnvilBlockEntity be) implements IItemHandler {
		@Override
		public int getSlots() {
			return 2;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return be.inventory.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != SLOT_CONTAINER)
				return stack;
			return be.inventory.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot == SLOT_CONTAINER && !FluidContainers.isEmptyContainer(be.inventory.getStackInSlot(slot)))
				return ItemStack.EMPTY;
			return be.inventory.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return be.inventory.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return be.inventory.isItemValid(slot, stack);
		}
	}

	/** Pour les tuyaux de Create : remplir le moule (pas de pompage). */
	private record CastingFluidHandler(CastingAnvilBlockEntity be) implements IFluidHandler {
		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return be.tank.getFluid();
		}

		@Override
		public int getTankCapacity(int tank) {
			return CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return be.canAccept(stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return be.fill(resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return FluidStack.EMPTY;
		}
	}
}
