package io.github.evan684900star.locksmithing.content.door;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Le mouvement d'une porte blindée qui pivote sur ses gonds : même calcul pour le rendu (client) et pour la forme
 * de la porte (contour et collisions, des deux côtés).
 */
public final class DoorSwing {
	private DoorSwing() {}

	/** Accélère au début, ralentit à la fin (porte lourde). */
	public static float ease(float t) {
		return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
	}

	/** Angle de la porte (en degrés) pour une ouverture de 0 (fermée) à 1 (ouverte), déjà adoucie. */
	public static float angle(Hinge hinge, float opened) {
		return hinge.sign() * 90f * opened;
	}

	/**
	 * Position des gonds et sens de rotation, déduits des formes de la porte de Minecraft :
	 * les gonds sont dans le coin commun à la porte fermée et à la porte ouverte.
	 */
	public record Hinge(float x, float z, float sign) {
		public static Hinge of(BlockState closed) {
			AABB shut = closedBounds(closed);
			AABB open = closed.setValue(DoorBlock.OPEN, true).getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
			float x = (float) (Math.max(shut.minX, open.minX) + Math.min(shut.maxX, open.maxX)) / 2;
			float z = (float) (Math.max(shut.minZ, open.minZ) + Math.min(shut.maxZ, open.maxZ)) / 2;
			// Direction gond -> bout de la porte, fermée puis ouverte
			double cx = (shut.minX + shut.maxX) / 2 - x, cz = (shut.minZ + shut.maxZ) / 2 - z;
			double ox = (open.minX + open.maxX) / 2 - x, oz = (open.minZ + open.maxZ) / 2 - z;
			// Une rotation de +90° autour de Y envoie (x, z) sur (z, -x)
			float sign = cz * ox - cx * oz >= 0 ? 1 : -1;
			return new Hinge(x, z, sign);
		}
	}

	/** La porte fermée (une moitié, dans son bloc). */
	public static AABB closedBounds(BlockState closed) {
		return closed.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
	}

	/* Forme pendant l'animation */

	/** Pas de 3 degrés : assez fin pour suivre la porte, et peu de formes différentes à garder. */
	private static final int STEPS = 30;
	/** Tranches le long de la porte : chacune est une boîte qui suit la porte tournée. */
	private static final int SLICES = 8;
	private static final Map<Integer, VoxelShape> SHAPES = new ConcurrentHashMap<>();

	/**
	 * Forme d'une moitié de porte tournée de {@code opened} (0 fermée, 1 ouverte, déjà adoucie) :
	 * une suite de petites boîtes qui suivent la porte, pour le contour et les collisions.
	 */
	public static VoxelShape shape(BlockState closed, float opened) {
		int step = Math.round(Mth.clamp(opened, 0, 1) * STEPS);
		boolean right = closed.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
		int key = (closed.getValue(DoorBlock.FACING).get2DDataValue() * 2 + (right ? 1 : 0)) * (STEPS + 1) + step;
		return SHAPES.computeIfAbsent(key, k -> build(closed, step / (float) STEPS));
	}

	private static VoxelShape build(BlockState closed, float opened) {
		AABB shut = closedBounds(closed);
		Hinge hinge = Hinge.of(closed);
		double radians = Math.toRadians(angle(hinge, opened));
		double cos = Math.cos(radians), sin = Math.sin(radians);
		boolean thinX = shut.getXsize() < shut.getZsize();

		VoxelShape shape = Shapes.empty();
		for (int i = 0; i < SLICES; i++) {
			double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
			for (int corner = 0; corner < 4; corner++) {
				double along = (i + (corner & 1)) / (double) SLICES;
				double across = (corner >> 1);
				double x = thinX ? Mth.lerp(across, shut.minX, shut.maxX) : Mth.lerp(along, shut.minX, shut.maxX);
				double z = thinX ? Mth.lerp(along, shut.minZ, shut.maxZ) : Mth.lerp(across, shut.minZ, shut.maxZ);
				// Même rotation que le rendu : autour de Y, (x, z) -> (x cos + z sin, -x sin + z cos)
				double dx = x - hinge.x(), dz = z - hinge.z();
				double rx = hinge.x() + dx * cos + dz * sin;
				double rz = hinge.z() - dx * sin + dz * cos;
				minX = Math.min(minX, rx);
				maxX = Math.max(maxX, rx);
				minZ = Math.min(minZ, rz);
				maxZ = Math.max(maxZ, rz);
			}
			minX = Mth.clamp(minX, 0, 1);
			maxX = Mth.clamp(maxX, 0, 1);
			minZ = Mth.clamp(minZ, 0, 1);
			maxZ = Mth.clamp(maxZ, 0, 1);
			if (maxX - minX > 1e-3 && maxZ - minZ > 1e-3)
				shape = Shapes.or(shape, Shapes.box(minX, 0, minZ, maxX, 1, maxZ));
		}
		return shape.optimize();
	}
}
