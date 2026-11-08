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

	/** Serveur : moment où le modèle fixe doit revenir (fin de l'animation), -1 si rien en cours. */
	private long visibleAt = -1;

	/* Client : suivi de l'animation (en temps de rendu, donc fluide à n'importe quel nombre d'images par seconde) */
	@Nullable
	private Boolean lastOpen;
	private float swingFrom, swingTo, swingStart;

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

	public void startSwing(long visibleAt) {
		this.visibleAt = visibleAt;
	}

	/** Remet le modèle fixe à la fin de l'animation (ou après un rechargement du monde). */
	public void serverTick() {
		if (level == null)
			return;
		BlockState state = getBlockState();
		if (state.getValue(ArmoredDoorBlock.VISIBLE)) {
			visibleAt = -1;
			return;
		}
		if (visibleAt >= 0 && level.getGameTime() < visibleAt)
			return;
		visibleAt = -1;
		level.setBlock(worldPosition, state.setValue(ArmoredDoorBlock.VISIBLE, true), 3);
	}

	/**
	 * Ouverture de 0 (fermée) à 1 (ouverte) au temps de rendu {@code now}, en suivant les changements de l'état OPEN.
	 * Si la porte change de sens en cours de route, elle repart de là où elle en est.
	 */
	public float swingProgress(boolean open, float now) {
		float target = open ? 1 : 0;
		if (lastOpen == null) {
			lastOpen = open;
			swingFrom = swingTo = target;
			swingStart = now;
		} else if (lastOpen != open) {
			float current = progressAt(now);
			lastOpen = open;
			swingFrom = current;
			swingTo = target;
			swingStart = now;
		}
		return progressAt(now);
	}

	private float progressAt(float now) {
		float distance = Math.abs(swingTo - swingFrom);
		if (distance == 0)
			return swingTo;
		float t = (now - swingStart) / (ArmoredDoorBlock.SWING_TICKS * distance);
		t = Math.max(0, Math.min(1, t));
		return swingFrom + (swingTo - swingFrom) * t;
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
