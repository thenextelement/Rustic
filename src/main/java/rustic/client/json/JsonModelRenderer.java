package rustic.client.json;

import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import rustic.common.blocks.IModelBlock;

/**
 * Renders {@link IModelBlock}s from their 1.12-format JSON models.
 */
public class JsonModelRenderer implements ISimpleBlockRenderingHandler {

	public static int RENDER_ID = -1;
	/** Same renderer, but items render as flat sprites (for blocks with an "item/generated" item model). */
	public static int RENDER_ID_FLAT = -1;

	private final boolean item3d;
	private int renderId;

	private JsonModelRenderer(boolean item3d) {
		this.item3d = item3d;
	}

	public static void register() {
		JsonModelRenderer normal = new JsonModelRenderer(true);
		JsonModelRenderer flat = new JsonModelRenderer(false);
		RENDER_ID = normal.renderId = RenderingRegistry.getNextAvailableRenderId();
		RENDER_ID_FLAT = flat.renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(normal);
		RenderingRegistry.registerBlockHandler(flat);
		rustic.common.blocks.ModBlocks.MODEL_RENDER_ID = RENDER_ID;
		rustic.common.blocks.ModBlocks.MODEL_RENDER_ID_FLAT = RENDER_ID_FLAT;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		if (!(block instanceof IModelBlock)) return;
		IModelBlock mb = (IModelBlock) block;
		List<ModelPart> parts = JsonModelRegistry.getParts(mb, mb.getInventoryProperties(metadata), true);
		int color = block.getRenderColor(metadata);
		Tessellator t = Tessellator.instance;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		t.startDrawingQuads();
		for (ModelPart part : parts) {
			renderPart(part, null, block, 0, 0, 0, color, t, renderer, true);
		}
		t.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
			RenderBlocks renderer) {
		if (!(block instanceof IModelBlock)) return false;
		IModelBlock mb = (IModelBlock) block;
		List<ModelPart> parts = JsonModelRegistry.getParts(mb, mb.getRenderProperties(world, x, y, z), false);
		int color = block.colorMultiplier(world, x, y, z);
		Tessellator t = Tessellator.instance;
		boolean drew = false;
		double ox = 0, oz = 0;
		if (mb.hasRandomOffset()) {
			double[] offset = IModelBlock.randomOffset(x, z);
			ox = offset[0];
			oz = offset[1];
			t.addTranslation((float) ox, 0, (float) oz);
		}
		for (ModelPart part : parts) {
			drew |= renderPart(part, world, block, x, y, z, color, t, renderer, false);
		}
		if (ox != 0 || oz != 0) t.addTranslation((float) -ox, 0, (float) -oz);
		return drew;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return item3d;
	}

	@Override
	public int getRenderId() {
		return renderId;
	}

	/* ---------------------------------------------------------------------------------------------------------- */

	// vertex corners per face, in the same order as 1.12's EnumFaceDirection; each entry selects min (0) or max (1)
	// for x, y, z
	private static final int[][][] CORNERS = new int[6][][];
	static {
		CORNERS[ForgeDirection.DOWN.ordinal()] = new int[][] { { 0, 0, 1 }, { 0, 0, 0 }, { 1, 0, 0 }, { 1, 0, 1 } };
		CORNERS[ForgeDirection.UP.ordinal()] = new int[][] { { 0, 1, 0 }, { 0, 1, 1 }, { 1, 1, 1 }, { 1, 1, 0 } };
		CORNERS[ForgeDirection.NORTH.ordinal()] = new int[][] { { 1, 1, 0 }, { 1, 0, 0 }, { 0, 0, 0 }, { 0, 1, 0 } };
		CORNERS[ForgeDirection.SOUTH.ordinal()] = new int[][] { { 0, 1, 1 }, { 0, 0, 1 }, { 1, 0, 1 }, { 1, 1, 1 } };
		CORNERS[ForgeDirection.WEST.ordinal()] = new int[][] { { 0, 1, 0 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 1, 1 } };
		CORNERS[ForgeDirection.EAST.ordinal()] = new int[][] { { 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 0 }, { 1, 1, 0 } };
	}

	private static float[] defaultUV(ForgeDirection face, float[] from, float[] to) {
		switch (face) {
			case DOWN: return new float[] { from[0], 16 - to[2], to[0], 16 - from[2] };
			case UP: return new float[] { from[0], from[2], to[0], to[2] };
			case NORTH: return new float[] { 16 - to[0], 16 - to[1], 16 - from[0], 16 - from[1] };
			case SOUTH: return new float[] { from[0], 16 - to[1], to[0], 16 - from[1] };
			case WEST: return new float[] { from[2], 16 - to[1], to[2], 16 - from[1] };
			case EAST: return new float[] { 16 - to[2], 16 - to[1], 16 - from[2], 16 - from[1] };
			default: return new float[] { 0, 0, 16, 16 };
		}
	}

	/** Rotate a point (in block units 0..16) in place around an axis through origin. */
	private static void rotate(float[] p, char axis, float degrees, float[] origin) {
		if (degrees == 0) return;
		double rad = Math.toRadians(degrees);
		float cos = (float) Math.cos(rad), sin = (float) Math.sin(rad);
		float x = p[0] - origin[0], y = p[1] - origin[1], z = p[2] - origin[2];
		switch (axis) {
			case 'x':
				p[1] = y * cos - z * sin + origin[1];
				p[2] = y * sin + z * cos + origin[2];
				break;
			case 'y':
				p[0] = x * cos + z * sin + origin[0];
				p[2] = -x * sin + z * cos + origin[2];
				break;
			case 'z':
				p[0] = x * cos - y * sin + origin[0];
				p[1] = x * sin + y * cos + origin[1];
				break;
		}
	}

	private static final float[] CENTER = { 8, 8, 8 };

	/** Apply a blockstate rotation to a direction. */
	private static ForgeDirection rotateDir(ForgeDirection dir, int rx, int ry) {
		float[] v = { dir.offsetX, dir.offsetY, dir.offsetZ };
		float[] zero = { 0, 0, 0 };
		rotate(v, 'x', -rx, zero);
		rotate(v, 'y', -ry, zero);
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			if (Math.round(v[0]) == d.offsetX && Math.round(v[1]) == d.offsetY && Math.round(v[2]) == d.offsetZ) {
				return d;
			}
		}
		return dir;
	}

	private boolean renderPart(ModelPart part, IBlockAccess world, Block block, int bx, int by, int bz, int color,
			Tessellator t, RenderBlocks renderer, boolean inventory) {
		boolean drew = false;
		float tr = (color >> 16 & 255) / 255F, tg = (color >> 8 & 255) / 255F, tb = (color & 255) / 255F;
		int ownBrightness = world == null ? 0xF000F0 : block.getMixedBrightnessForBlock(world, bx, by, bz);
		List<JsonModel.Element> elements = part.model.elements;

		for (int e = 0; e < elements.size(); e++) {
			JsonModel.Element el = elements.get(e);
			float[] min = { Math.min(el.from[0], el.to[0]), Math.min(el.from[1], el.to[1]), Math.min(el.from[2], el.to[2]) };
			float[] max = { Math.max(el.from[0], el.to[0]), Math.max(el.from[1], el.to[1]), Math.max(el.from[2], el.to[2]) };

			for (int f = 0; f < 6; f++) {
				JsonModel.Face face = el.faces[f];
				IIcon icon = part.icons[e][f];
				if (face == null || icon == null) continue;
				ForgeDirection dir = ForgeDirection.getOrientation(f);

				ForgeDirection cull = face.cullface == null ? null : rotateDir(face.cullface, part.x, part.y);
				if (world != null && cull != null && renderer != null && !renderer.renderAllFaces
						&& !block.shouldSideBeRendered(world, bx + cull.offsetX, by + cull.offsetY, bz + cull.offsetZ, cull.ordinal())) {
					continue;
				}

				float[] uv = face.uv != null ? face.uv : defaultUV(dir, el.from, el.to);
				float[][] pos = new float[4][];
				for (int v = 0; v < 4; v++) {
					int[] c = CORNERS[f][v];
					float[] p = { c[0] == 0 ? min[0] : max[0], c[1] == 0 ? min[1] : max[1], c[2] == 0 ? min[2] : max[2] };
					if (el.rotAxis != 0) {
						rotate(p, el.rotAxis, el.rotAngle, el.rotOrigin);
						if (el.rotRescale) rescale(p, el.rotAxis, el.rotAngle, el.rotOrigin);
					}
					rotate(p, 'x', -part.x, CENTER);
					rotate(p, 'y', -part.y, CENTER);
					pos[v] = p;
				}

				// normal from the transformed quad
				float ax = pos[1][0] - pos[0][0], ay = pos[1][1] - pos[0][1], az = pos[1][2] - pos[0][2];
				float cx = pos[2][0] - pos[1][0], cy = pos[2][1] - pos[1][1], cz = pos[2][2] - pos[1][2];
				float nx = ay * cz - az * cy, ny = az * cx - ax * cz, nz = ax * cy - ay * cx;
				float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (len > 1e-6F) {
					nx /= len;
					ny /= len;
					nz /= len;
				} else {
					ForgeDirection rd = rotateDir(dir, part.x, part.y);
					nx = rd.offsetX;
					ny = rd.offsetY;
					nz = rd.offsetZ;
				}

				float shade = 1F;
				if (!inventory && el.shade) {
					shade = Math.min(nx * nx * 0.6F + ny * ny * ((3F + ny) / 4F) + nz * nz * 0.8F, 1F);
				}
				float r = shade, g = shade, b = shade;
				if (face.tintIndex >= 0) {
					r *= tr;
					g *= tg;
					b *= tb;
				}

				if (inventory) {
					t.setNormal(nx, ny, nz);
				} else {
					int brightness = ownBrightness;
					// faces on the block boundary take the light of the block they face
					ForgeDirection out = cull != null ? cull : boundaryDir(pos, nx, ny, nz);
					if (out != null) {
						brightness = block.getMixedBrightnessForBlock(world, bx + out.offsetX, by + out.offsetY, bz + out.offsetZ);
					}
					t.setBrightness(brightness);
				}
				t.setColorOpaque_F(r, g, b);

				for (int v = 0; v < 4; v++) {
					int idx = (v + face.rotation / 90) % 4;
					float u = (idx == 0 || idx == 1) ? uv[0] : uv[2];
					float vv = (idx == 0 || idx == 3) ? uv[1] : uv[3];
					t.addVertexWithUV(bx + pos[v][0] / 16D, by + pos[v][1] / 16D, bz + pos[v][2] / 16D,
							icon.getInterpolatedU(u), icon.getInterpolatedV(vv));
				}
				drew = true;
			}
		}
		return drew;
	}

	/** If the whole quad lies on a face of the unit block, the direction of that face. */
	private static ForgeDirection boundaryDir(float[][] pos, float nx, float ny, float nz) {
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			if (Math.abs(nx - d.offsetX) > 0.01F || Math.abs(ny - d.offsetY) > 0.01F || Math.abs(nz - d.offsetZ) > 0.01F) continue;
			int axis = d.offsetX != 0 ? 0 : d.offsetY != 0 ? 1 : 2;
			float edge = (d.offsetX + d.offsetY + d.offsetZ) > 0 ? 16F : 0F;
			for (float[] p : pos) {
				if (Math.abs(p[axis] - edge) > 0.01F) return null;
			}
			return d;
		}
		return null;
	}

	private static void rescale(float[] p, char axis, float angle, float[] origin) {
		float scale = Math.abs(angle) == 22.5F ? 1F / (float) Math.cos(Math.PI / 8D) : 1F / (float) Math.cos(Math.PI / 4D);
		for (int i = 0; i < 3; i++) {
			if ((axis == 'x' && i == 0) || (axis == 'y' && i == 1) || (axis == 'z' && i == 2)) continue;
			p[i] = (p[i] - origin[i]) * scale + origin[i];
		}
	}

}
