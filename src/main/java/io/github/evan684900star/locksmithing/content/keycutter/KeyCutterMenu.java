package io.github.evan684900star.locksmithing.content.keycutter;

import java.util.List;

import javax.annotation.Nullable;

import io.github.evan684900star.locksmithing.recipe.KeyCuttingRecipe;
import io.github.evan684900star.locksmithing.registry.ModBlocks;
import io.github.evan684900star.locksmithing.registry.ModRecipeTypes;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class KeyCutterMenu extends AbstractContainerMenu {
	public static final int INPUT_X = 20, INPUT_Y = 33;
	public static final int OUTPUT_X = 143, OUTPUT_Y = 33;

	@Nullable
	public final KeyCutterBlockEntity be;
	private final ContainerData data;
	private final ContainerLevelAccess access;
	private final Level level;
	private final ItemStackHandler inventory;

	/** Côté client. */
	public KeyCutterMenu(MenuType<?> type, int containerId, Inventory playerInventory, @Nullable RegistryFriendlyByteBuf buf) {
		this(type, containerId, playerInventory,
			buf == null ? null : playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof KeyCutterBlockEntity k ? k : null,
			new SimpleContainerData(3));
	}

	public KeyCutterMenu(MenuType<?> type, int containerId, Inventory playerInventory,
		@Nullable KeyCutterBlockEntity be, ContainerData data) {
		super(type, containerId);
		this.be = be;
		this.data = data;
		this.level = playerInventory.player.level();
		this.access = be == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
		this.inventory = be == null ? new ItemStackHandler(2) : be.inventory;

		addSlot(new SlotItemHandler(inventory, KeyCutterBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y));
		addSlot(new SlotItemHandler(inventory, KeyCutterBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y) {
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

	/** Les clés possibles pour l'objet posé à gauche (même ordre côté client et serveur). */
	public List<RecipeHolder<KeyCuttingRecipe>> getRecipes() {
		return ModRecipeTypes.findKeyCutting(level, inventory.getStackInSlot(KeyCutterBlockEntity.SLOT_INPUT));
	}

	public int getProgressPercent() {
		return data.get(0);
	}

	public int getSelected() {
		List<RecipeHolder<KeyCuttingRecipe>> recipes = getRecipes();
		return recipes.size() == 1 ? 0 : data.get(1);
	}

	public boolean isPowered() {
		return data.get(2) != 0;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id < 0 || id >= getRecipes().size())
			return false;
		if (be != null)
			be.select(id);
		data.set(1, id);
		return true;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.KEY_CUTTER.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem())
			return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		int machineSlots = 2;
		if (index < machineSlots) {
			if (!moveItemStackTo(stack, machineSlots, slots.size(), true))
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
