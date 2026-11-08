package io.github.evan684900star.locksmithing.content.door;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Les métaux des portes blindées et de leurs clés, du minerai le plus courant au plus rare.
 * Plus le minerai est rare, plus la porte est longue à casser (temps avec la hache minimum pour la récupérer :
 * en fer, ou en diamant pour la netherite).
 */
public enum DoorMetal {
	COPPER("copper", 42, false, MapColor.COLOR_ORANGE, SoundType.COPPER, true),
	IRON("iron", 56, false, MapColor.METAL, SoundType.METAL, false),
	ZINC("zinc", 70, false, MapColor.GLOW_LICHEN, SoundType.METAL, false),
	GOLD("gold", 84, false, MapColor.GOLD, SoundType.METAL, false),
	/** Améliorée à la table de forgeron ; hache en diamant ; ne brûle pas. */
	NETHERITE("netherite", 120, true, MapColor.COLOR_BLACK, SoundType.NETHERITE_BLOCK, false);

	private final String id;
	private final int breakSeconds;
	private final boolean needsDiamond;
	private final MapColor mapColor;
	private final SoundType sound;
	private final boolean oxidizes;

	DoorMetal(String id, int breakSeconds, boolean needsDiamond, MapColor mapColor, SoundType sound, boolean oxidizes) {
		this.id = id;
		this.breakSeconds = breakSeconds;
		this.needsDiamond = needsDiamond;
		this.mapColor = mapColor;
		this.sound = sound;
		this.oxidizes = oxidizes;
	}

	public String id() {
		return id;
	}

	/** Secondes pour casser la porte avec la hache minimum ({@link #minimumAxe()}), sans enchantement. */
	public int breakSeconds() {
		return breakSeconds;
	}

	/** La hache la moins bonne qui récupère la porte : en fer, ou en diamant pour la netherite. */
	public Item minimumAxe() {
		return needsDiamond ? Items.DIAMOND_AXE : Items.IRON_AXE;
	}

	public boolean needsDiamond() {
		return needsDiamond;
	}

	/**
	 * Dureté qui donne ce temps : une hache de vitesse v retire v / dureté / 30 par tick,
	 * donc dureté = secondes x 20 x v / 30 (hache en fer : v = 6, en diamant : v = 8).
	 */
	public float hardness() {
		float speed = needsDiamond ? 8f : 6f;
		return breakSeconds * 20f * speed / 30f;
	}

	/** Comme l'équipement en netherite, ces objets ne brûlent pas dans la lave. */
	public boolean fireResistant() {
		return this == NETHERITE;
	}

	public MapColor mapColor() {
		return mapColor;
	}

	public SoundType sound() {
		return sound;
	}

	/** Seul le cuivre s'oxyde (et se cire). */
	public boolean oxidizes() {
		return oxidizes;
	}
}
