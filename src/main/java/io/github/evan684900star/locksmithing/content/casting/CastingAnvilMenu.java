package io.github.evan684900star.locksmithing.content.casting;

import javax.annotation.Nullable;

import io.github.evan684900star.locksmithing.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CastingAnvilMenu extends AbstractContainerMenu {
	public static final int CONTAINER_X = 35, CONTAINER_Y = 35;
	public static final int OUTPUT_X = 120, OUTPUT_Y = 35;

	@Nullable
	public final CastingAnvilBlockEntity be;
	private final ContainerData data;
	private final ContainerLevelAccess access;

	/** Côté client. */
	public CastingAnvilMenu(MenuType<?> type, int containerId, Inventory playerInventory, @Nullable RegistryFriendlyByteBuf buf) {
		this(type, containerId, playerInventory,
			buf == null ? null : playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof CastingAnvilBlockEntity c ? c : null,
			new SimpleContainerData(4));
	}

	public CastingAnvilMenu(MenuType<?> type, int containerId, Inventory playerInventory,
		@Nullable CastingAnvilBlockEntity be, ContainerData data) {
		super(type, containerId);
		this.be = be;
		this.data = data;
		this.access = be == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

		ItemStackHandler inventory = be == null ? new ItemStackHandler(2) : be.inventory;
		addSlot(new SlotItemHandler(inventory, CastingAnvilBlockEntity.SLOT_CONTAINER, CONTAINER_X, CONTAINER_Y));
		addSlot(new SlotItemHandler(inventory, CastingAnvilBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 9; col++)
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
		for (int col = 0; col < 9; col++)
			addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));

		addDataSlots(data);
	}

	public int getProgress() {
		return data.get(0);
	}

	public int getMaxProgress() {
		return Math.max(1, data.get(1));
	}

	public int getFluidAmount() {
		return data.get(2);
	}

	public Fluid getFluid() {
		int id = data.get(3);
		return id < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(id);
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.CASTING_ANVIL.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem())
			return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		int machineSlots = 2;
		int end = slots.size();
		if (index < machineSlots) {
			if (!moveItemStackTo(stack, machineSlots, end, true))
				return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, 0, 1, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty())
			slot.setByPlayer(ItemStack.EMPTY);
		else
			slot.setChanged();
		return original;
	}
}
