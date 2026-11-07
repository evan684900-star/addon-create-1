package io.github.evan684900star.locksmithing.content.casting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

/** Affiche le métal en fusion dans le creux du moule, et la barre une fois coulée. */
public class CastingAnvilRenderer extends SafeBlockEntityRenderer<CastingAnvilBlockEntity> {
	// Creux du moule (enclume orientée nord) : x 4..12, z 6..10, fond à y = 13, bord à y = 16
	private static final float FLOOR = 13 / 16f;
	private static final float DEPTH = 2.5f / 16f;

	public CastingAnvilRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(CastingAnvilBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState state = be.getBlockState();
		boolean alongX = state.getValue(HorizontalDirectionalBlock.FACING).getAxis() == Direction.Axis.Z;

		FluidStack fluid = be.getFluid();
		float surface = FLOOR;
		if (!fluid.isEmpty()) {
			// Un seau (1000 mB) remplit le creux
			float fill = Math.min(1f, fluid.getAmount() / 1000f);
			float top = FLOOR + Math.max(1 / 32f, DEPTH * fill);
			surface = top;
			float xMin = alongX ? 4 / 16f : 6 / 16f, xMax = alongX ? 12 / 16f : 10 / 16f;
			float zMin = alongX ? 6 / 16f : 4 / 16f, zMax = alongX ? 10 / 16f : 12 / 16f;
			// Le métal en fusion brille : on le dessine en pleine lumière
			NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, xMin, FLOOR, zMin, xMax, top, zMax, buffer, ms,
				LightTexture.FULL_BRIGHT, false, false);
		}

		ItemStack output = be.getOutput();
		if (!output.isEmpty()) {
			ms.pushPose();
			// La barre refroidie repose sur le métal
			ms.translate(0.5, surface + 1 / 32f, 0.5);
			if (!alongX)
				ms.mulPose(Axis.YP.rotationDegrees(90));
			ms.mulPose(Axis.XP.rotationDegrees(90));
			ms.scale(0.5f, 0.5f, 0.5f);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(output, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
			ms.popPose();
		}
	}
}
