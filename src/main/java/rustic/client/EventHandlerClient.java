package rustic.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
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

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
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
        if (stack.getItem() instanceof ItemFood && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("oiled")) {
            event.toolTip.add(
                EnumChatFormatting.DARK_GREEN + ""
                    + EnumChatFormatting.ITALIC
                    + I18n.format("tooltip.rustic.olive_oil"));
        }
        PotionEffect vanta = ElixirUtils.getVantaOilEffect(stack);
        if (vanta != null) {
            ElixirUtils.addVantaOilTooltip(stack, vanta, event.toolTip);
        }
        FluidStack fluid = FluidHelper.getFluidContained(stack);
        if (fluid != null && fluid.getFluid() instanceof FluidBooze
            && fluid.tag != null
            && fluid.tag.hasKey(FluidBooze.QUALITY_NBT_KEY, 5)) {
            event.toolTip.add(ClientUtils.getQualityTooltip(fluid.tag.getFloat(FluidBooze.QUALITY_NBT_KEY)));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFullmetalFov(FOVUpdateEvent event) {
        EntityPlayer player = event.entity;
        if (!player.isPotionActive(PotionsRustic.FULLMETAL_POTION)) return;
        float f = 1.0F;
        if (player.isUsingItem() && player.getItemInUse() != null
            && player.getItemInUse()
                .getItem() == Items.bow) {
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
        // Input is read and applied within the player's own tick, so zeroing it here would be too late; wrap the
        // player's MovementInput instead so it is cleared right after each update.
        if (player != null && player.movementInput != null && !(player.movementInput instanceof FullmetalInput)) {
            player.movementInput = new FullmetalInput(player, player.movementInput);
        }
    }

    /** Clears movement input while the player has the fullmetal effect (1.12: InputUpdateEvent). */
    private static class FullmetalInput extends net.minecraft.util.MovementInput {

        private final EntityClientPlayerMP player;
        private final net.minecraft.util.MovementInput delegate;

        FullmetalInput(EntityClientPlayerMP player, net.minecraft.util.MovementInput delegate) {
            this.player = player;
            this.delegate = delegate;
        }

        @Override
        public void updatePlayerMoveState() {
            delegate.updatePlayerMoveState();
            moveStrafe = delegate.moveStrafe;
            moveForward = delegate.moveForward;
            jump = delegate.jump;
            sneak = delegate.sneak;
            if (player.isPotionActive(PotionsRustic.FULLMETAL_POTION)) {
                jump = false;
                moveForward = 0F;
                moveStrafe = 0F;
            }
        }
    }

    private Map<Block, ResourceLocation> getFluidOverlays() {
        if (fluidOverlays == null) {
            fluidOverlays = new HashMap<>();
            fluidOverlays
                .put(ModFluids.BLOCK_OLIVE_OIL, new ResourceLocation(FLUID_OVERLAY_PATH + "olive_oil_overlay.png"));
            fluidOverlays.put(
                ModFluids.BLOCK_IRONBERRY_JUICE,
                new ResourceLocation(FLUID_OVERLAY_PATH + "ironberry_juice_overlay.png"));
            fluidOverlays.put(
                ModFluids.BLOCK_WILDBERRY_JUICE,
                new ResourceLocation(FLUID_OVERLAY_PATH + "wildberry_juice_overlay.png"));
            fluidOverlays
                .put(ModFluids.BLOCK_GRAPE_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "grape_juice_overlay.png"));
            fluidOverlays
                .put(ModFluids.BLOCK_APPLE_JUICE, new ResourceLocation(FLUID_OVERLAY_PATH + "apple_juice_overlay.png"));
            fluidOverlays
                .put(ModFluids.BLOCK_ALE_WORT, new ResourceLocation(FLUID_OVERLAY_PATH + "ale_wort_overlay.png"));
            fluidOverlays.put(ModFluids.BLOCK_HONEY, new ResourceLocation(FLUID_OVERLAY_PATH + "honey_overlay.png"));
            fluidOverlays.put(
                ModFluids.BLOCK_GOLDEN_APPLE_JUICE,
                new ResourceLocation(FLUID_OVERLAY_PATH + "golden_apple_juice_overlay.png"));
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
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(location);
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
    private static final ResourceLocation FULLMETAL_OVERLAY = new ResourceLocation(
        "rustic:textures/misc/fullmetal_overlay.png");

    /** Metal vignette over the view while under the fullmetal effect (1.12 drew it in the hand pass). */
    @SubscribeEvent
    public void onRenderFullmetalOverlayEvent(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HELMET) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.gameSettings.thirdPersonView != 0) return;
        PotionEffect effect = mc.thePlayer.getActivePotionEffect(PotionsRustic.FULLMETAL_POTION);
        if (effect == null) return;

        float brightness = mc.thePlayer.getBrightness(event.partialTicks);
        float durationFade = 1.0F;
        if (effect.getDuration() < 100) {
            float dur = (effect.getDuration() + event.partialTicks) * 2F;
            float j1 = 10 - (dur / 20);
            durationFade = 0.5F + MathHelper.clamp_float(dur / 400F, 0.0F, 0.9375F - 0.5F)
                + MathHelper.cos((200 - dur) * (float) Math.PI / 200F * 9F)
                    * MathHelper.clamp_float(j1 / 10.0F * 0.25F, 0.0625F, 0.25F);
            durationFade = MathHelper.sqrt_float(durationFade);
            if (dur < 20) durationFade *= MathHelper.sqrt_float(dur / 20F);
        }

        int w = event.resolution.getScaledWidth();
        int h = event.resolution.getScaledHeight();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        net.minecraft.client.renderer.OpenGlHelper
            .glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glColor4f(brightness, brightness, brightness, 0.9625F * durationFade);
        mc.getTextureManager()
            .bindTexture(FULLMETAL_OVERLAY);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(0, h, -90, -0.5, 1.5);
        t.addVertexWithUV(w, h, -90, 1.5, 1.5);
        t.addVertexWithUV(w, 0, -90, 1.5, -0.5);
        t.addVertexWithUV(0, 0, -90, -0.5, -0.5);
        t.draw();
        GL11.glDepthMask(true);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopAttrib();
    }

    /** Shows the diagonal a rotatable-placement block (diagonal clay wall) will be placed along. */
    @SubscribeEvent
    public void onBlockHighlight(net.minecraftforge.client.event.DrawBlockHighlightEvent event) {
        ItemStack stack = event.currentItem;
        net.minecraft.util.MovingObjectPosition target = event.target;
        if (stack == null || target == null
            || target.typeOfHit != net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK
            || (target.sideHit != 0 && target.sideHit != 1)
            || !(Block.getBlockFromItem(stack.getItem()) instanceof rustic.common.blocks.IAdvancedRotationPlacement))
            return;
        EntityPlayer player = event.player;
        net.minecraft.world.World world = player.worldObj;
        int x = target.blockX, y = target.blockY, z = target.blockZ;
        Block block = world.getBlock(x, y, z);
        if (block.getMaterial() == net.minecraft.block.material.Material.air) return;
        block.setBlockBoundsBasedOnState(world, x, y, z);
        double minX = block.getBlockBoundsMinX(), maxX = block.getBlockBoundsMaxX();
        double minZ = block.getBlockBoundsMinZ(), maxZ = block.getBlockBoundsMaxZ();
        // only for boxes centred on the block in X/Z
        if (Math.abs((minX + maxX) / 2 - 0.5) > 1.0E-4 || Math.abs((minZ + maxZ) / 2 - 0.5) > 1.0E-4) return;
        double grow = 0.002;
        double minXZ = Math.max(minX, minZ) - grow, maxXZ = Math.min(maxX, maxZ) + grow;
        double yy = (target.sideHit == 1 ? block.getBlockBoundsMaxY() + grow : block.getBlockBoundsMinY() - grow) + y;

        float pt = event.partialTicks;
        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * pt;
        double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * pt;
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * pt;

        GL11.glEnable(GL11.GL_BLEND);
        net.minecraft.client.renderer.OpenGlHelper
            .glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_LINE_STRIP);
        t.setTranslation(-px, -py, -pz);
        vertex(t, minXZ + x, yy, minXZ + z, 0F);
        vertex(t, minXZ + x, yy, minXZ + z, 0.4F);
        vertex(t, maxXZ + x, yy, maxXZ + z, 0.4F);
        vertex(t, minXZ + x, yy, maxXZ + z, 0F);
        vertex(t, maxXZ + x, yy, minXZ + z, 0.4F);
        vertex(t, maxXZ + x, yy, minXZ + z, 0F);
        t.draw();
        t.setTranslation(0, 0, 0);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void vertex(Tessellator t, double x, double y, double z, float alpha) {
        t.setColorRGBA_F(0F, 0F, 0F, alpha);
        t.addVertex(x, y, z);
    }

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
        mc.getTextureManager()
            .bindTexture(Gui.icons);
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

    /** Players sitting on chairs face the way the chair does (1.12's EntityChair.applyOrientationToEntity). */
    @SubscribeEvent
    public void onUpdateSittingEntity(RenderLivingEvent.Pre event) {
        EntityLivingBase entity = event.entity;
        if (entity.ridingEntity instanceof BlockChair.EntityChair) {
            entity.renderYawOffset = entity.ridingEntity.rotationYaw;
            entity.prevRenderYawOffset = entity.ridingEntity.rotationYaw;
        }
    }

    /** While seated, the player can look at most 105 degrees away from the way the chair faces, as in 1.12. */
    @SubscribeEvent
    public void onSittingPlayerTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null || !(player.ridingEntity instanceof BlockChair.EntityChair)) return;
        float chairYaw = player.ridingEntity.rotationYaw;
        player.renderYawOffset = chairYaw;
        float f = MathHelper.wrapAngleTo180_float(player.rotationYaw - chairYaw);
        float clamped = MathHelper.clamp_float(f, -105F, 105F);
        player.prevRotationYaw += clamped - f;
        player.rotationYaw += clamped - f;
        player.rotationYawHead = player.rotationYaw;
    }

}
