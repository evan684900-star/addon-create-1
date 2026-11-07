package io.github.evan684900star.locksmithing.registry;

import static io.github.evan684900star.locksmithing.Locksmithing.REGISTRATE;

import com.tterrag.registrate.util.entry.MenuEntry;

import io.github.evan684900star.locksmithing.content.casting.CastingAnvilMenu;
import io.github.evan684900star.locksmithing.content.casting.CastingAnvilScreen;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterMenu;
import io.github.evan684900star.locksmithing.content.keycutter.KeyCutterScreen;

public class ModMenus {
	public static final MenuEntry<CastingAnvilMenu> CASTING_ANVIL =
		REGISTRATE.menu("casting_anvil", CastingAnvilMenu::new, () -> CastingAnvilScreen::new).register();

	public static final MenuEntry<KeyCutterMenu> KEY_CUTTER =
		REGISTRATE.menu("key_cutter", KeyCutterMenu::new, () -> KeyCutterScreen::new).register();

	public static void register() {}
}
