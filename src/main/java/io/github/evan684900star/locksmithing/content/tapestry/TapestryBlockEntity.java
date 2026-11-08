package io.github.evan684900star.locksmithing.content.tapestry;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import io.github.evan684900star.locksmithing.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Les motifs de la tapisserie (même format que les bannières, donc le métier à tisser marche tel quel). */
public class TapestryBlockEntity extends SyncedBlockEntity {
	private BannerPatternLayers patterns = BannerPatternLayers.EMPTY;
	@Nullable
	private Component name;

	public TapestryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public BannerPatternLayers getPatterns() {
		return patterns;
	}

	public ItemStack getItem() {
		ItemStack stack = ModBlocks.TAPESTRY.asStack();
		stack.applyComponents(collectComponents());
		return stack;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!patterns.equals(BannerPatternLayers.EMPTY))
			tag.put("patterns", BannerPatternLayers.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),
				patterns).getOrThrow());
		if (name != null)
			tag.putString("CustomName", Component.Serializer.toJson(name, registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		name = tag.contains("CustomName", 8) ? parseCustomNameSafe(tag.getString("CustomName"), registries) : null;
		patterns = BannerPatternLayers.EMPTY;
		if (tag.contains("patterns"))
			BannerPatternLayers.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("patterns"))
				.result()
				.ifPresent(p -> patterns = p);
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		patterns = input.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		name = input.get(DataComponents.CUSTOM_NAME);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(DataComponents.BANNER_PATTERNS, patterns);
		components.set(DataComponents.CUSTOM_NAME, name);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void removeComponentsFromTag(CompoundTag tag) {
		tag.remove("patterns");
		tag.remove("CustomName");
	}
}
