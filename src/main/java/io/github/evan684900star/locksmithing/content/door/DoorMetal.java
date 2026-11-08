package io.github.evan684900star.locksmithing.content.door;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Les métaux des portes blindées et de leurs clés, du minerai le plus courant au plus rare.
 * Plus le minerai est rare, plus la porte est longue à casser (temps avec une hache en fer, le minimum).
 */
public enum DoorMetal {
	COPPER("copper", 42, MapColor.COLOR_ORANGE, SoundType.COPPER, true),
	IRON("iron", 56, MapColor.METAL, SoundType.METAL, false),
	ZINC("zinc", 70, MapColor.GLOW_LICHEN, SoundType.METAL, false),
	GOLD("gold", 84, MapColor.GOLD, SoundType.METAL, false);

	private final String id;
	private final int breakSeconds;
	private final MapColor mapColor;
	private final SoundType sound;
	private final boolean oxidizes;

	DoorMetal(String id, int breakSeconds, MapColor mapColor, SoundType sound, boolean oxidizes) {
		this.id = id;
		this.breakSeconds = breakSeconds;
		this.mapColor = mapColor;
		this.sound = sound;
		this.oxidizes = oxidizes;
	}

	public String id() {
		return id;
	}

	/** Secondes pour casser la porte avec une hache en fer, sans enchantement. */
	public int breakSeconds() {
		return breakSeconds;
	}

	/**
	 * Dureté qui donne ce temps : une hache en fer (vitesse 6) retire 6 / dureté / 30 par tick,
	 * donc dureté = secondes x 20 x 6 / 30 = secondes x 4.
	 */
	public float hardness() {
		return breakSeconds * 4f;
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
