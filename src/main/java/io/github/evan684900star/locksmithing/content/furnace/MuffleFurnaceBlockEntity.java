package io.github.evan684900star.locksmithing.content.furnace;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import io.github.evan684900star.locksmithing.Locksmithing;
import io.github.evan684900star.locksmithing.content.fluid.FluidContainers;
import io.github.evan684900star.locksmithing.recipe.MeltingRecipe;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
import io.github.evan684900star.locksmithing.registry.ModSounds;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class MuffleFurnaceBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
	public static final int CAPACITY = 4000;

	private final ItemStackHandler inventory = new ItemStackHandler(1) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return canMelt(stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			changed();
		}
	};

	private final FluidTank tank = new FluidTank(CAPACITY) {
		@Override
		protected void onContentsChanged() {
			changed();
		}
	};

	private final IItemHandler itemCapability = new InsertOnlyItemHandler(inventory);
	private final IFluidHandler fluidCapability = new DrainOnlyFluidHandler(tank);

	private int progress;

	public MuffleFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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

	public ItemStack getIngots() {
		return inventory.getStackInSlot(0);
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide)
			sendData();
	}

	public HeatLevel getHeat() {
		if (level == null)
			return HeatLevel.NONE;
		return BlazeBurnerBlock.getHeatLevelOf(level.getBlockState(worldPosition.below()));
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide)
			return;

		HeatLevel heat = getHeat();
		boolean heated = HeatCondition.HEATED.testBlazeBurner(heat);
		BlockState state = getBlockState();
		if (state.getValue(MuffleFurnaceBlock.LIT) != heated)
			level.setBlock(worldPosition, state.setValue(MuffleFurnaceBlock.LIT, heated), 3);

		ItemStack input = inventory.getStackInSlot(0);
		Optional<MeltingRecipe> recipe = heated ? ModRecipeTypes.findMelting(level, input) : Optional.empty();
		if (recipe.isEmpty() || !canAccept(recipe.get().result())) {
			progress = 0;
			return;
		}

		// Superchauffé (gâteau de blaze) : deux fois plus rapide.
		progress += heat == HeatLevel.SEETHING ? 2 : 1;
		if (progress < recipe.get().time())
			return;

		progress = 0;
		FluidStack result = recipe.get().result().copy();
		if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(), result))
			tank.setFluid(FluidStack.EMPTY); // reste de l'ancien métal (moins d'une barre) : perdu
		tank.fill(result, IFluidHandler.FluidAction.EXECUTE);
		inventory.extractItem(0, 1, false);
		level.playSound(null, worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.6f, 0.8f);
	}

	/** Le réservoir peut-il recevoir ce métal ? Un reste de moins d'une barre d'un autre métal est remplacé. */
	private boolean canAccept(FluidStack result) {
		if (tank.isEmpty())
			return true;
		if (FluidStack.isSameFluidSameComponents(tank.getFluid(), result))
			return tank.getSpace() >= result.getAmount();
		return tank.getFluidAmount() < FluidContainers.INGOT;
	}

	public boolean canMelt(ItemStack stack) {
		if (level == null)
			return false;
		Optional<MeltingRecipe> recipe = ModRecipeTypes.findMelting(level, stack);
		if (recipe.isEmpty())
			return false;
		ItemStack current = inventory.getStackInSlot(0);
		if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack))
			return false;
		// On refuse un autre métal tant qu'il reste au moins une barre de l'ancien.
		return tank.isEmpty() || FluidStack.isSameFluidSameComponents(tank.getFluid(), recipe.get().result())
			|| tank.getFluidAmount() < FluidContainers.INGOT;
	}

	/* Interactions */

	public ItemInteractionResult onUse(Player player, InteractionHand hand, ItemStack stack) {
		int capacity = FluidContainers.capacityOf(stack);
		if (capacity > 0) {
			if (tank.getFluidAmount() < capacity)
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			ItemStack filled = FluidContainers.fill(stack, tank.getFluid().getFluid());
			if (filled.isEmpty())
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (level != null && !level.isClientSide) {
				tank.drain(capacity, IFluidHandler.FluidAction.EXECUTE);
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
				level.playSound(null, worldPosition,
					stack.is(net.minecraft.world.item.Items.BUCKET) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BOTTLE_FILL,
					SoundSource.BLOCKS, 1f, 1f);
			}
			return ItemInteractionResult.sidedSuccess(level != null && level.isClientSide);
		}

		if (canMelt(stack)) {
			if (level != null && !level.isClientSide) {
				ItemStack remainder = inventory.insertItem(0, stack.copy(), false);
				if (!player.getAbilities().instabuild)
					player.setItemInHand(hand, remainder);
				level.playSound(null, worldPosition, ModSounds.FURNACE_LOAD.get(), SoundSource.BLOCKS, 0.7f, 1f);
			}
			return ItemInteractionResult.sidedSuccess(level != null && level.isClientSide);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	public InteractionResult takeIngots(Player player) {
		ItemStack ingots = inventory.getStackInSlot(0);
		if (ingots.isEmpty())
			return InteractionResult.PASS;
		if (level != null && !level.isClientSide) {
			player.getInventory().placeItemBackInInventory(ingots.copy());
			inventory.setStackInSlot(0, ItemStack.EMPTY);
		}
		return InteractionResult.sidedSuccess(level != null && level.isClientSide);
	}

	public void dropContents() {
		if (level != null)
			Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
				inventory.getStackInSlot(0));
	}

	/* Lunettes d'ingénieur */

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		new LangBuilder(Locksmithing.ID).translate("gui.goggles.muffle_furnace")
			.forGoggles(tooltip);
		boolean heated = HeatCondition.HEATED.testBlazeBurner(getHeat());
		new LangBuilder(Locksmithing.ID).translate(heated ? "gui.goggles.heated" : "gui.goggles.not_heated")
			.style(heated ? ChatFormatting.GOLD : ChatFormatting.RED)
			.forGoggles(tooltip, 1);
		ItemStack ingots = inventory.getStackInSlot(0);
		if (!ingots.isEmpty())
			new LangBuilder(Locksmithing.ID).add(ingots.getHoverName().copy())
				.text(" x" + ingots.getCount())
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip, 1);
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
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
		tank.readFromNBT(registries, tag.getCompound("Tank"));
		progress = tag.getInt("Progress");
	}
}
