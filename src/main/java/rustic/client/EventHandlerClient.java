package rustic.client;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import rustic.client.json.JsonModelRegistry;
import rustic.client.util.ClientUtils;
import rustic.common.Config;
import rustic.common.blocks.BlockChair;
import rustic.common.blocks.BlockVase;
import rustic.common.blocks.ModBlocks;
import rustic.common.blocks.fluids.FluidBooze;
import rustic.common.blocks.fluids.FluidRustic;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.network.MessageVaseMeta;
import rustic.common.network.PacketHandler;
import rustic.common.potions.PotionsRustic;
import rustic.common.util.ElixirUtils;
import rustic.common.util.FluidHelper;

public class EventHandlerClient {

	private static final String FLUID_OVERLAY_PATH = "rustic:textures/blocks/fluids/";
	private Map<Block, ResourceLocation> fluidOverlays;

	/** Fluids without a block (the booze) still need their icons for GUIs, tanks and bottles. */
	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		for (FluidRustic fluid : FluidRustic.ALL) {
			if (fluid.getBlock() == null) fluid.registerIcons(event.map);
		}
	}

	@SubscribeEvent
	public void onTextureStitchPost(TextureStitchEvent.Post event) {
		if (event.map.getTextureType() == 0) {
			JsonModelRegistry.onTextureStitchPost();
			ClientUtils.clearColorCaches();
		}
	}

	/** Sneak + scroll changes the held vase's design. */
	@SubscribeEvent
	public void onVaseMouseWheel(MouseEvent event) {
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if (player == null || event.dwheel == 0 || !player.isSneaking()) return;
		ItemStack held = player.getCurrentEquippedItem();
		if (held == null || held.getItem() != Item.getItemFromBlock(ModBlocks.VASE)) return;
		event.setCanceled(true);
		int currentDamage = held.getItemDamage();
		int offset = event.dwheel / 120;
		int currentIndex = -1;
		for (int i = 0; i < BlockVase.ITEM_ORDER.length; i++) {
			if (BlockVase.ITEM_ORDER[i] == currentDamage) {
				currentIndex = i;
				break;
			}
		}
		int damage;
		if (currentIndex >= 0) {
			int n = BlockVase.ITEM_ORDER.length;
			damage = BlockVase.ITEM_ORDER[(((currentIndex + offset) % n) + n) % n];
		} else {
			int n = BlockVase.MAX_VARIANT - BlockVase.MIN_VARIANT + 1;
			damage = ((((currentDamage - BlockVase.MIN_VARIANT + offset) % n) + n) % n) + BlockVase.MIN_VARIANT;
		}
		held.setItemDamage(damage);
		PacketHandler.INSTANCE.sendToServer(new MessageVaseMeta(damage));
	}

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.itemStack;
		if (stack == null) return;
		if (stack.getItem() instanceof ItemFood && stack.hasTagCompound() && stack.getTagCompound().hasKey("oiled")) {
			event.toolTip.add(EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.ITALIC + I18n.format("tooltip.rustic.olive_oil"));
		}
		PotionEffect vanta = ElixirUtils.getVantaOilEffect(stack);
		if (vanta != null) {
			ElixirUtils.addVantaOilTooltip(stack, vanta, event.toolTip);
		}
		FluidStack fluid = FluidHelper.getFluidContained(stack);
		if (fluid != null && fluid.getFluid() instanceof FluidBooze && fluid.tag != null
				&& fluid.tag.hasKey(FluidBooze.QUALITY_NBT_KEY, 5)) {
			event.toolTip.add(ClientUtils.getQualityTooltip(fluid.tag.getFloat(FluidBooze.QUALITY_NBT_KEY)));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onFullmetalFov(FOVUpdateEvent event) {
		EntityPlayer player = event.entity;
		if (!player.isPotionActive(PotionsRustic.FULLMETAL_POTION)) return;
		float f = 1.0F;
		if (player.isUsingItem() && player.getItemInUse() != null && player.getItemInUse().getItem() == Items.bow) {
			float f1 = player.getItemInUseDuration() / 20.0F;
			f1 = f1 > 1.0F ? 1.0F : f1 * f1;
			f *= 1.0F - f1 * 0.15F;
		}
		event.newfov = f;
	}

	/** Fullmetal players can't move by themselves. */
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if (player != null && player.isPotionActive(PotionsRustic.FULLMETAL_POTION) && player.movementInput != null) {
			player.movementInput.jump = false;
			player.movementInput.moveForward = 0F;
			player.movementInput.moveStrafe = 0F;
		}
	}

	private Map<Block, ResourceLocation> getFluidOverlays() {
		if (fluidOverlays == null) {
			fluidOverlays = new HashMap<>();
			fluidOverlays.put(ModFluids.BLOCK_OLIVE_OIL, new ResourceLocation(FLUID_OVERLAY_PATH + "olive_oil_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_IRONBERRY_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "ironberry_juice_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_WILDBERRY_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "wildberry_juice_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_GRAPE_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "grape_juice_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_APPLE_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "apple_juice_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_ALE_WORT, new ResourceLocation(FLUID_OVERLAY_PATH + "ale_wort_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_HONEY, new ResourceLocation(FLUID_OVERLAY_PATH + "honey_overlay.png"));
			fluidOverlays.put(ModFluids.BLOCK_GOLDEN_APPLE_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "golden_apple_juice_overlay.png"));
		}
		return fluidOverlays;
	}

	/** Underwater-style screen overlays for Rustic's fluids. */
	@SubscribeEvent
	public void onRenderBlockOverlayEvent(RenderBlockOverlayEvent event) {
		if (event.overlayType != RenderBlockOverlayEvent.OverlayType.WATER) return;
		EntityPlayer player = event.player;
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY); // client player posY is at eye level
		int z = MathHelper.floor_double(player.posZ);
		Block block = player.worldObj.getBlock(x, y, z);
		ResourceLocation overlay = getFluidOverlays().get(block);
		if (overlay == null) return;
		event.setCanceled(true);
		float brightness = player.getBrightness(event.renderPartialTicks);
		GL11.glColor4f(brightness, brightness, brightness, 0.99F);
		drawBlockOverlay(overlay, player);
		GL11.glColor4f(1, 1, 1, 1);
	}

	private void drawBlockOverlay(ResourceLocation location, EntityPlayer player) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(location);
		Tessellator t = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glPushMatrix();
		float f7 = -player.rotationYaw / 64.0F;
		float f8 = player.rotationPitch / 64.0F;
		t.startDrawingQuads();
		t.addVertexWithUV(-1.0D, -1.0D, -0.5D, 4.0F + f7, 4.0F + f8);
		t.addVertexWithUV(1.0D, -1.0D, -0.5D, 0.0F + f7, 4.0F + f8);
		t.addVertexWithUV(1.0D, 1.0D, -0.5D, 0.0F + f7, 0.0F + f8);
		t.addVertexWithUV(-1.0D, 1.0D, -0.5D, 4.0F + f7, 0.0F + f8);
		t.draw();
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
	}

	/** Lets the armor bar show more than one row (armor above 20). */
	@SubscribeEvent
	public void onRenderArmorOverlayEvent(RenderGameOverlayEvent.Pre event) {
		if (event.type != RenderGameOverlayEvent.ElementType.ARMOR || !Config.EXTRA_ARMOR_HUD) return;
		Minecraft mc = Minecraft.getMinecraft();
		if (!(mc.renderViewEntity instanceof EntityPlayer)) return;
		int armorLevel = ForgeHooks.getTotalArmorValue((EntityPlayer) mc.renderViewEntity);
		if (armorLevel <= 20) return; // vanilla rendering is identical
		event.setCanceled(true);
		int width = event.resolution.getScaledWidth();
		int height = event.resolution.getScaledHeight();
		GuiIngame gui = mc.ingameGUI;
		mc.mcProfiler.startSection("armor");
		mc.getTextureManager().bindTexture(Gui.icons);
		GL11.glEnable(GL11.GL_BLEND);
		int armorRows = MathHelper.ceiling_float_int(armorLevel / 20.0F);
		int rowHeight = Math.min(Math.max(10 - (armorRows - 2), 3), 10);
		int top = (height - GuiIngameForge.left_height) - ((armorRows * rowHeight) - 10);
		for (int i = armorRows - 1; i >= 0; i--) {
			int left = width / 2 - 91;
			for (int j = 1; j < 20; j += 2) {
				if (j + (i * 20) < armorLevel) {
					gui.drawTexturedModalRect(left, top, 34, 9, 9, 9);
				} else if (j + (i * 20) == armorLevel) {
					gui.drawTexturedModalRect(left, top, 25, 9, 9, 9);
				} else {
					gui.drawTexturedModalRect(left, top, 16, 9, 9, 9);
				}
				left += 8;
			}
			top += rowHeight;
			GuiIngameForge.left_height += rowHeight;
		}
		if (rowHeight < 10) GuiIngameForge.left_height += (10 - rowHeight);
		GL11.glDisable(GL11.GL_BLEND);
		mc.mcProfiler.endSection();
	}

	/** Other players sitting on chairs face the way the chair does. */
	@SubscribeEvent
	public void onUpdateSittingEntity(RenderLivingEvent.Pre event) {
		EntityLivingBase entity = event.entity;
		if (entity == Minecraft.getMinecraft().thePlayer) return;
		if (entity.ridingEntity instanceof BlockChair.EntityChair) {
			entity.renderYawOffset = entity.ridingEntity.rotationYaw;
			entity.prevRenderYawOffset = entity.ridingEntity.rotationYaw;
		}
	}

}
