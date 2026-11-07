package io.github.evan684900star.locksmithing.content.door;

import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Serrure d'une porte blindée (stockée dans la moitié basse).
 * Vide tant qu'aucune clé n'a été insérée ; disparaît quand la porte est cassée.
 */
public class ArmoredDoorBlockEntity extends SyncedBlockEntity {
	@Nullable
	private UUID lockId;

	public ArmoredDoorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Nullable
	public UUID getLockId() {
		return lockId;
	}

	public boolean isBound() {
		return lockId != null;
	}

	public void bind(UUID keyId) {
		lockId = keyId;
		setChanged();
		sendData();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (lockId != null)
			tag.putUUID("LockId", lockId);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		lockId = tag.hasUUID("LockId") ? tag.getUUID("LockId") : null;
	}
}
