package io.github.evan684900star.locksmithing.content.key;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nullable;

import io.github.evan684900star.locksmithing.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Une clé taillée. Chaque clé sortie de la machine à tailler a un identifiant unique :
 * la première clé insérée dans une porte devient la seule qui peut l'ouvrir.
 */
public class KeyItem extends Item {
	public KeyItem(Properties properties) {
		super(properties);
	}

	@Nullable
	public static UUID getId(ItemStack stack) {
		return stack.get(ModDataComponents.KEY_ID.get());
	}

	/** Donne un identifiant à une clé qui n'en a pas encore (ex : clé prise en créatif). */
	public static UUID getOrCreateId(ItemStack stack) {
		UUID id = getId(stack);
		if (id == null) {
			id = UUID.randomUUID();
			stack.set(ModDataComponents.KEY_ID.get(), id);
		}
		return id;
	}

	/** Numéro court affiché dans l'infobulle pour distinguer les clés. */
	public static String shortCode(UUID id) {
		return id.toString().substring(0, 4).toUpperCase(Locale.ROOT);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		UUID id = getId(stack);
		if (id == null)
			tooltip.add(Component.translatable("item.create_locksmithing.key.uncut").withStyle(ChatFormatting.GRAY));
		else
			tooltip.add(Component.translatable("item.create_locksmithing.key.code", shortCode(id))
				.withStyle(ChatFormatting.GRAY));
	}
}
