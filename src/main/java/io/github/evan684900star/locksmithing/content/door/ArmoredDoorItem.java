package io.github.evan684900star.locksmithing.content.door;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

/** L'objet porte : garde l'oxydation et la cire quand on la casse puis la repose ; dit combien de temps il faut pour la casser. */
public class ArmoredDoorItem extends DoubleHighBlockItem {
	public ArmoredDoorItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if (getBlock() instanceof ArmoredDoorBlock door) {
			DoorMetal metal = door.getMetal();
			tooltip.add(Component.translatable("item.create_locksmithing.armored_door.break_time", metal.breakSeconds(),
				Component.translatable(metal.minimumAxe().getDescriptionId())).withStyle(ChatFormatting.GRAY));
			if (metal.fireResistant())
				tooltip.add(Component.translatable("item.create_locksmithing.armored_door.fire_resistant")
					.withStyle(ChatFormatting.DARK_RED));
		}
		BlockItemStateProperties props = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		WeatherState oxidation = props.get(ArmoredDoorBlock.OXIDATION);
		Boolean waxed = props.get(ArmoredDoorBlock.WAXED);
		if (oxidation != null && oxidation != WeatherState.UNAFFECTED)
			tooltip.add(Component.translatable("item.create_locksmithing.armored_door.oxidation." + oxidation.getSerializedName())
				.withStyle(ChatFormatting.GRAY));
		if (waxed != null && waxed)
			tooltip.add(Component.translatable("item.create_locksmithing.armored_door.waxed")
				.withStyle(ChatFormatting.GOLD));
	}
}
