package rustic.dev;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import rustic.client.json.JsonModel;
import rustic.client.json.JsonModelRegistry;
import rustic.client.json.ModelPart;
import rustic.common.blocks.BlockCondenserBase;
import rustic.common.blocks.IModelBlock;
import rustic.common.blocks.ModBlocks;
import rustic.common.items.ModItems;
import rustic.core.Rustic;

/**
 * Development-only automated smoke test. Inert unless a file named {@code rustic_selftest} exists in the game
 * directory (run/client). It checks the JSON models of every block, calls every NEI handler with every Rustic item,
 * then creates a flat test world, simulates player interactions there, takes screenshots and quits. Results are logged
 * with the prefix "SELFTEST".
 */
public final class SelfTest {

    private static final String WORLD = "rustic_selftest";

    private final Minecraft mc = Minecraft.getMinecraft();
    private final ConcurrentLinkedQueue<Runnable> serverTasks = new ConcurrentLinkedQueue<>();
    private int stage;
    private int timer;
    private int page;
    private List<ItemStack> allItems;
    private volatile boolean worldTestsDone;
    private int serverTicks;

    private SelfTest() {}

    public static void initIfRequested() {
        File flag = new File(Minecraft.getMinecraft().mcDataDir, "rustic_selftest");
        if (!flag.exists()) return;
        SelfTest test = new SelfTest();
        MinecraftForge.EVENT_BUS.register(test);
        FMLCommonHandler.instance()
            .bus()
            .register(test);
        log("enabled");
    }

    static void log(String msg, Object... args) {
        SelfTestLog.log(msg, args);
    }

    static void check(boolean ok, String what, Object... args) {
        SelfTestLog.check(ok, what, args);
    }

    static void fail(String what, Throwable t) {
        SelfTestLog.fail(what, t);
    }

    /* ------------------------------------------------------------------------------------------------------------ */

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (stage == 0 && event.gui instanceof GuiMainMenu) stage = 1;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        try {
            clientTick();
        } catch (Throwable t) {
            fail("client stage " + stage, t);
            stage = 99;
        }
    }

    private void clientTick() {
        switch (stage) {
            case 1:
                allItems = collectRusticItems();
                checkModels();
                page = 0;
                mc.displayGuiScreen(new ItemGrid());
                timer = 0;
                stage = 2;
                break;
            case 2: // item grid screenshots are taken from the render tick
                break;
            case 3:
                mc.displayGuiScreen(null);
                mc.getSaveLoader()
                    .flushCache();
                mc.getSaveLoader()
                    .deleteWorldDirectory(WORLD);
                WorldSettings settings = new WorldSettings(
                    1234L,
                    WorldSettings.GameType.CREATIVE,
                    false,
                    false,
                    WorldType.FLAT);
                settings.enableCommands();
                mc.gameSettings.pauseOnLostFocus = false; // the server must keep ticking in the background
                mc.launchIntegratedServer(WORLD, WORLD, settings);
                stage = 4;
                break;
            case 4:
                if (worldTestsDone && mc.thePlayer != null && mc.theWorld != null) {
                    timer = 0;
                    mc.gameSettings.thirdPersonView = 0;
                    stage = 5;
                }
                break;
            case 98:
                if (++timer > 10) {
                    SelfTestLog.done();
                    mc.shutdown();
                    stage = 100;
                }
                break;
            case 99:
                log("aborted");
                timer = 0;
                stage = 98;
                break;
            default:
                break;
        }
    }

    /** Stages 5+ (world screenshots), driven from the render tick so the frame is complete. */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        try {
            renderTick();
        } catch (Throwable t) {
            fail("render stage " + stage, t);
            stage = 99;
        }
    }

    private void renderTick() {
        if (stage == 2 && mc.currentScreen instanceof ItemGrid) {
            if (++timer == 5) {
                screenshot("items_" + page);
                timer = 0;
                if ((page + 1) * ItemGrid.PER_PAGE >= allItems.size()) stage = 3;
                else page++;
            }
            return;
        }
        if (stage < 5 || stage >= 98) return;
        if (script == null) script = buildScript();
        if (step >= script.size()) {
            timer = 0;
            stage = 98;
            return;
        }
        Step st = script.get(step);
        if (timer++ == 0 && st.setup != null) st.setup.run();
        if (timer >= st.frames) {
            if (st.finish != null) st.finish.run();
            timer = 0;
            step++;
        }
    }

    private static final class Step {

        final Runnable setup, finish;
        final int frames;

        Step(Runnable setup, int frames, Runnable finish) {
            this.setup = setup;
            this.frames = frames;
            this.finish = finish;
        }
    }

    private List<Step> script;
    private int step;

    private Step shot(String name, int frames, Runnable setup) {
        return new Step(setup, frames, () -> screenshot(name));
    }

    private Step server(Runnable task, int frames) {
        return new Step(() -> serverTasks.add(task), frames, null);
    }

    private void teleport(double x, double y, double z, float yaw, float pitch) {
        serverTasks.add(() -> {
            EntityPlayerMP p = realPlayer();
            if (p != null) p.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
        });
    }

    /** Right-click a block through the real client controller (sends the same packet as a player's click). */
    private void clientClick(int[] pos, int side) {
        ItemStack held = mc.thePlayer.getHeldItem();
        mc.playerController.onPlayerRightClick(
            mc.thePlayer,
            mc.theWorld,
            held,
            pos[0],
            pos[1],
            pos[2],
            side,
            net.minecraft.util.Vec3.createVectorHelper(pos[0] + 0.5, pos[1] + 0.9, pos[2] + 0.5));
    }

    private static int count(ItemStack[] inv, Item item) {
        int n = 0;
        for (ItemStack s : inv) if (s != null && s.getItem() == item) n += s.stackSize;
        return n;
    }

    private List<Step> buildScript() {
        int[] o = SelfTestWorld.origin;
        int x0 = o[0], y = o[1], z0 = o[2];
        List<Step> l = new ArrayList<>();
        l.add(shot("world", 150, () -> {
            mc.gameSettings.hideGUI = true;
            teleport(x0 + 7.5, y + 3, z0 + 9.5, 180F, 25F);
        }));
        l.add(shot("condensers", 60, () -> teleport(x0 + 9.5, y + 1, z0 + 4.5, 180F, 10F)));
        l.add(shot("crops", 60, () -> teleport(x0 + 2.5, y + 1, z0 + 3.5, 180F, 25F)));

        // interactions through the real client, in survival
        int[] stake = SelfTestWorld.clientStake, bush = SelfTestWorld.clientBush, ground = SelfTestWorld.clientGround;
        l.add(server(() -> {
            realPlayer().setGameType(WorldSettings.GameType.SURVIVAL);
            SelfTestWorld.prepareClientTests();
        }, 20));
        l.add(new Step(() -> {
            teleport(stake[0] + 0.5, stake[1], stake[2] + 2.5, 180F, 30F);
            hold(new ItemStack(ModItems.TOMATO_SEEDS, 8));
        }, 30, () -> clientClick(stake, 3)));
        l.add(new Step(null, 30, () -> {
            check(
                mc.theWorld.getBlock(stake[0], stake[1], stake[2]) == ModBlocks.TOMATO_CROP,
                "client click: tomato crop missing on client");
            serverTasks.add(
                () -> check(
                    realPlayer().worldObj.getBlock(stake[0], stake[1], stake[2]) == ModBlocks.TOMATO_CROP,
                    "client click: tomato seeds not planted on stake (server)"));
        }));
        l.add(new Step(() -> {
            teleport(bush[0] + 0.5, bush[1], bush[2] + 2.5, 180F, 30F);
            hold(null);
        }, 30, () -> clientClick(bush, 3)));
        l.add(new Step(null, 30, () -> {
            check(
                count(mc.thePlayer.inventory.mainInventory, ModItems.WILDBERRIES) > 0,
                "client click: no berries in the client inventory");
            serverTasks.add(
                () -> check(
                    count(realPlayer().inventory.mainInventory, ModItems.WILDBERRIES) > 0,
                    "client click: picking a ripe bush gave no berries (server inventory)"));
        }));
        l.add(new Step(() -> {
            teleport(ground[0] + 0.5, ground[1] + 1, ground[2] + 2.5, 180F, 30F);
            hold(new ItemStack(ModBlocks.CONDENSER));
        }, 30, () -> clientClick(ground, 1)));
        l.add(new Step(null, 30, () -> {
            check(
                mc.theWorld.getBlock(ground[0], ground[1] + 2, ground[2]) == ModBlocks.CONDENSER,
                "client click: condenser top half missing on client");
            serverTasks.add(
                () -> check(
                    realPlayer().worldObj.getBlock(ground[0], ground[1] + 2, ground[2]) == ModBlocks.CONDENSER,
                    "client click: condenser top half missing on server"));
        }));
        int[] chair = SelfTestWorld.clientChair;
        l.add(new Step(() -> {
            teleport(chair[0] + 0.5, chair[1], chair[2] + 1.8, 180F, 20F);
            hold(null);
        }, 30, () -> clientClick(chair, 3)));
        l.add(new Step(null, 90, () -> {
            boolean riding = mc.thePlayer.ridingEntity instanceof rustic.common.blocks.BlockChair.EntityChair;
            int clientChairs = 0;
            for (Object e : mc.theWorld.loadedEntityList)
                if (e instanceof rustic.common.blocks.BlockChair.EntityChair) clientChairs++;
            log("chair: client riding={} client chair entities={}", riding, clientChairs);
            serverTasks.add(() -> {
                EntityPlayerMP p = realPlayer();
                int chairs = 0;
                for (Object e : p.worldObj.loadedEntityList)
                    if (e instanceof rustic.common.blocks.BlockChair.EntityChair) chairs++;
                log("chair: server riding={} server chair entities={}", p.ridingEntity, chairs);
            });
            check(riding, "client click: player is not sitting on the chair");
            if (riding) {
                double feet = mc.thePlayer.boundingBox.minY - chair[1];
                log("seated player's feet are {} above the chair block (1.12: 0.05)", String.format("%.3f", feet));
                check(
                    Math.abs(feet - 0.05) < 0.03,
                    "seated player at the wrong height: {} above the chair block",
                    String.format("%.3f", feet));
            }
        }));
        l.add(shot("chair_sitting", 20, () -> mc.gameSettings.thirdPersonView = 2));
        l.add(new Step(() -> {
            mc.gameSettings.thirdPersonView = 0;
            serverTasks.add(() -> {
                EntityPlayerMP p = realPlayer();
                if (p.ridingEntity != null) p.mountEntity(null);
            });
        }, 10, null));
        l.add(shot("client_tests", 40, () -> teleport(stake[0] + 3.5, stake[1] + 1, stake[2] + 4.5, 180F, 20F)));

        // held items
        l.add(server(() -> realPlayer().setGameType(WorldSettings.GameType.CREATIVE), 5));
        l.add(shot("hand_book", 40, () -> {
            mc.gameSettings.hideGUI = false;
            hold(new ItemStack(ModItems.BOOK));
        }));
        l.add(shot("third_book", 40, () -> mc.gameSettings.thirdPersonView = 1));
        l.add(shot("front_book", 40, () -> mc.gameSettings.thirdPersonView = 2));
        l.add(shot("hand_cabinet", 40, () -> {
            mc.gameSettings.thirdPersonView = 0;
            hold(new ItemStack(ModBlocks.CABINET));
        }));
        l.add(shot("slots", 10, () -> mc.displayGuiScreen(new BigSlots())));
        int[] iw = SelfTestWorld.ironwoodSpot;
        if (iw != null) {
            l.add(new Step(() -> mc.displayGuiScreen(null), 2, null));
            l.add(shot("ironwood", 80, () -> {
                mc.gameSettings.hideGUI = true;
                teleport(iw[0] + 24, iw[1] + 6, iw[2] + 80, 180F, 15F);
            }));
            l.add(new Step(() -> mc.gameSettings.hideGUI = false, 2, null));
        }
        l.add(new Step(() -> mc.displayGuiScreen(new rustic.client.gui.book.GuiBook()), 5, null));
        l.add(shot("almanac_decoration", 10, () -> {
            if (mc.currentScreen instanceof rustic.client.gui.book.GuiBook)
                ((rustic.client.gui.book.GuiBook) mc.currentScreen)
                    .goToEntry(rustic.common.book.BookManager.decoration.getCategoryEntry());
        }));
        if (Loader.isModLoaded("NotEnoughItems")) {
            // NEI loads its plugins (and so Rustic's handlers) only once a world is joined
            l.add(new Step(null, 2, () -> SelfTestNEI.run(allItems)));
        }
        if (Loader.isModLoaded("NotEnoughItems")) {
            // survival inventory: narrow enough to leave room for NEI's item panel
            l.add(server(() -> realPlayer().setGameType(WorldSettings.GameType.SURVIVAL), 10));
            l.add(
                new Step(
                    () -> mc.displayGuiScreen(new net.minecraft.client.gui.inventory.GuiInventory(mc.thePlayer)),
                    5,
                    () -> SelfTestNEI.search("@Rustic")));
            l.add(shot("nei_panel", 40, null));
            for (String id : new String[] { "rustic.crushing_tub", "rustic.evaporating", "rustic.brewing",
                "rustic.alchemy_simple", "rustic.alchemy_advanced" }) {
                l.add(shot("nei_" + id.substring(7), 30, () -> SelfTestNEI.openHandler(id)));
            }
            l.add(shot("nei_cabinet_recipe", 40, () -> SelfTestNEI.openRecipes(new ItemStack(ModBlocks.CABINET))));
            l.add(
                shot(
                    "nei_vanta_usage",
                    40,
                    () -> SelfTestNEI.openUsages(
                        ModItems.FLUID_BOTTLE.getFilledBottle(rustic.common.blocks.fluids.ModFluids.VANTA_OIL))));
            l.add(shot("nei_vanta_usage_later", 50, null));
        }
        l.add(new Step(() -> mc.displayGuiScreen(null), 2, null));
        return l;
    }

    /** A few special items drawn at 4x scale over slot backgrounds, to check they stay inside their slot. */
    private class BigSlots extends GuiScreen {

        private final RenderItem render = new RenderItem();

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawRect(0, 0, width, height, 0xFF404040);
            ItemStack[] stacks = { new ItemStack(ModBlocks.CABINET), new ItemStack(ModItems.BOOK),
                new ItemStack(ModBlocks.CONDENSER), new ItemStack(ModBlocks.LIQUID_BARREL),
                new ItemStack(ModBlocks.BREWING_BARREL), new ItemStack(net.minecraft.init.Blocks.stonebrick),
                new ItemStack(ModBlocks.PLANKS), new ItemStack(ModBlocks.CHAIR_OAK) };
            GL11.glPushMatrix();
            GL11.glScalef(2.5F, 2.5F, 1F);
            for (int i = 0; i < stacks.length; i++) drawRect(4 + i * 20, 4, 20 + i * 20, 20, 0xFF8B8B8B);
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            for (int i = 0; i < stacks.length; i++) {
                render.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stacks[i], 4 + i * 20, 4);
            }
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            // second row: the same stacks through NEI's own drawing path
            if (Loader.isModLoaded("NotEnoughItems")) {
                for (int i = 0; i < stacks.length; i++) drawRect(4 + i * 20, 28, 20 + i * 20, 44, 0xFF8B8B8B);
                RenderHelper.enableGUIStandardItemLighting(); // as NEI's panel does before drawing its items
                for (int i = 0; i < stacks.length; i++) SelfTestNEI.drawLikeNEI(4 + i * 20, 28, stacks[i]);
                RenderHelper.disableStandardItemLighting();
            }
            GL11.glPopMatrix();
        }
    }

    private void hold(ItemStack stack) {
        serverTasks.add(() -> {
            EntityPlayerMP p = realPlayer();
            if (p == null) return;
            p.inventory.setInventorySlotContents(p.inventory.currentItem, stack);
            p.inventoryContainer.detectAndSendChanges();
        });
    }

    private void screenshot(String name) {
        ScreenShotHelper.saveScreenshot(
            mc.mcDataDir,
            "selftest_" + name + ".png",
            mc.displayWidth,
            mc.displayHeight,
            mc.getFramebuffer());
        log("screenshot {}", name);
    }

    /* ------------------------------------------------------------------------------------------------------------ */

    static List<ItemStack> collectRusticItems() {
        List<ItemStack> out = new ArrayList<>();
        for (Object o : Item.itemRegistry) {
            Item item = (Item) o;
            String name = Item.itemRegistry.getNameForObject(item);
            if (name == null || !name.startsWith(Rustic.MODID + ":")) continue;
            List<ItemStack> subs = new ArrayList<>();
            try {
                item.getSubItems(item, item.getCreativeTab(), subs);
            } catch (Throwable t) {
                fail("getSubItems " + name, t);
            }
            if (subs.isEmpty()) subs.add(new ItemStack(item));
            out.addAll(subs);
        }
        log("{} rustic item stacks", out.size());
        return out;
    }

    private static void checkModels() {
        int blocks = 0;
        for (Object o : Block.blockRegistry) {
            Block block = (Block) o;
            if (!(block instanceof IModelBlock)) continue;
            if (block.getRenderType() != ModBlocks.MODEL_RENDER_ID
                && block.getRenderType() != ModBlocks.MODEL_RENDER_ID_FLAT) continue;
            blocks++;
            IModelBlock mb = (IModelBlock) block;
            String name = Block.blockRegistry.getNameForObject(block);
            for (int meta = 0; meta < 16; meta++) {
                Map<String, String> props;
                try {
                    props = mb.getInventoryProperties(meta);
                } catch (Throwable t) {
                    continue;
                }
                List<ModelPart> parts = JsonModelRegistry.getParts(mb, props, false);
                if (meta == 0) check(!parts.isEmpty(), "no model parts for {} {}", name, props);
                for (ModelPart part : parts) {
                    for (int e = 0; e < part.icons.length; e++) {
                        for (int f = 0; f < 6; f++) {
                            JsonModel.Face face = part.model.elements.get(e).faces[f];
                            // unresolvable refs like "#-1" mark invisible zero-size faces in exported models
                            boolean hasFace = face != null && part.model.resolveTexture(face.texture) != null;
                            IIcon icon = part.icons[e][f];
                            check(
                                !hasFace || (icon != null && !"missingno".equals(icon.getIconName())),
                                "missing texture in {} {} element {} face {}",
                                name,
                                props,
                                e,
                                f);
                        }
                    }
                }
            }
        }
        log("checked models of {} blocks", blocks);
        // every item shows a translated name
        for (ItemStack stack : collectRusticItems()) {
            String name;
            try {
                name = stack.getDisplayName();
            } catch (Throwable t) {
                fail("display name of " + stack, t);
                continue;
            }
            check(
                name != null && !name.endsWith(".name") && !name.startsWith("tile.") && !name.startsWith("item."),
                "untranslated item name {} ({})",
                name,
                stack);
        }
        // painted wood: one block, 16 colours with their own names
        if (ModBlocks.PAINTED_WOOD != null) {
            java.util.Set<String> names = new java.util.HashSet<>();
            for (int meta = 0; meta < 16; meta++)
                names.add(new ItemStack(ModBlocks.PAINTED_WOOD, 1, meta).getDisplayName());
            check(names.size() == 16, "painted wood colours share names: {}", names);
        }
        // the condenser tops (only reachable through placed-block properties) must have textured models
        for (BlockCondenserBase c : new BlockCondenserBase[] { ModBlocks.CONDENSER, ModBlocks.CONDENSER_ADVANCED }) {
            List<ModelPart> parts = JsonModelRegistry.getParts(c, c.getRenderPropertiesForMeta(2), false);
            check(!parts.isEmpty(), "no model for {} top", c.getBlockStateName());
            for (ModelPart part : parts) {
                for (IIcon[] faces : part.icons) {
                    boolean any = false;
                    for (IIcon icon : faces) any |= icon != null;
                    check(any, "{} top has an element with no textured faces", c.getBlockStateName());
                }
            }
        }
        // the condenser's top half must use the top model whatever its facing
        for (int facing = 0; facing < 4; facing++) {
            check(
                "false".equals(
                    ModBlocks.CONDENSER.getRenderPropertiesForMeta(facing)
                        .get("bottom")),
                "condenser top (meta {}) renders as bottom",
                facing);
        }
    }

    /* ------------------------------------------------------------------------------------------------------------ */

    private static EntityPlayerMP realPlayer() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager().playerEntityList.isEmpty()) return null;
        return (EntityPlayerMP) server.getConfigurationManager().playerEntityList.get(0);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Runnable task;
        while ((task = serverTasks.poll()) != null) task.run();
        if (stage != 4 || worldTestsDone) return;
        EntityPlayerMP player = realPlayer();
        if (player == null) return;
        if (++serverTicks < 40) return;
        try {
            SelfTestWorld.run(
                player.getServerForPlayer(),
                (int) Math.floor(player.posX) + 3,
                (int) Math.floor(player.posY),
                (int) Math.floor(player.posZ) - 6);
        } catch (Throwable t) {
            fail("world tests", t);
        }
        worldTestsDone = true;
    }

    private static final int[] GL_CAPS = { GL11.GL_LIGHTING, GL12.GL_RESCALE_NORMAL, GL11.GL_BLEND, GL11.GL_CULL_FACE,
        GL11.GL_DEPTH_TEST, GL11.GL_ALPHA_TEST, GL11.GL_TEXTURE_2D, GL11.GL_COLOR_MATERIAL, GL11.GL_NORMALIZE };
    private static final String[] GL_CAP_NAMES = { "LIGHTING", "RESCALE_NORMAL", "BLEND", "CULL_FACE", "DEPTH_TEST",
        "ALPHA_TEST", "TEXTURE_2D", "COLOR_MATERIAL", "NORMALIZE" };
    private boolean glLeaksChecked;

    /**
     * Every item must leave the GL state as a vanilla block does: anything left changed (e.g. GL_RESCALE_NORMAL
     * switched off) breaks the lighting of the items drawn after it in NEI's item panel.
     */
    private void checkGlLeaks(RenderItem render, net.minecraft.client.gui.FontRenderer font) {
        // vanilla references: a block, a two-pass item (potion) and a plain item; vanilla itself leaves different
        // states after each, so an item is fine if it ends in the state of any of them
        List<boolean[]> baselines = new ArrayList<>();
        baselines.add(drawAndCapture(render, font, new ItemStack(net.minecraft.init.Blocks.stone)));
        baselines.add(drawAndCapture(render, font, new ItemStack(net.minecraft.init.Items.potionitem, 1, 8193)));
        baselines.add(drawAndCapture(render, font, new ItemStack(net.minecraft.init.Items.apple)));
        // multi-pass item without glint (water bottle): vanilla leaves GL_BLEND on after these
        baselines.add(drawAndCapture(render, font, new ItemStack(net.minecraft.init.Items.potionitem, 1, 0)));
        for (int b = 1; b < baselines.size(); b++) {
            for (int i = 0; i < GL_CAPS.length; i++) {
                if (baselines.get(b)[i] != baselines.get(0)[i]) log(
                    "vanilla baseline {} leaves GL_{} {}",
                    b,
                    GL_CAP_NAMES[i],
                    baselines.get(b)[i] ? "enabled" : "disabled");
            }
        }
        for (ItemStack stack : allItems) {
            boolean[] after = drawAndCapture(render, font, stack);
            boolean ok = false;
            for (boolean[] b : baselines) ok |= java.util.Arrays.equals(after, b);
            check(
                ok,
                "{} leaves GL state {} (vanilla: {})",
                stack.getDisplayName(),
                describe(after),
                describe(baselines.get(0)));
        }
        log("checked GL state leaks of {} items", allItems.size());
    }

    private static String describe(boolean[] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GL_CAPS.length; i++) if (state[i]) sb.append(sb.length() > 0 ? "," : "")
            .append(GL_CAP_NAMES[i]);
        return sb.toString();
    }

    private boolean[] drawAndCapture(RenderItem render, net.minecraft.client.gui.FontRenderer font, ItemStack stack) {
        // the state NEI's item panel draws in
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        try {
            render.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, 0, 0);
        } catch (Throwable t) {
            fail("render item " + stack, t);
        }
        boolean[] out = new boolean[GL_CAPS.length];
        for (int i = 0; i < GL_CAPS.length; i++) out[i] = GL11.glIsEnabled(GL_CAPS[i]);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        return out;
    }

    /** Draws every Rustic item in a grid of slot backgrounds, a page at a time. */
    private class ItemGrid extends GuiScreen {

        static final int COLS = 20, ROWS = 11, PER_PAGE = COLS * ROWS;
        private final RenderItem render = new RenderItem();

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            if (!glLeaksChecked) {
                glLeaksChecked = true;
                checkGlLeaks(render, fontRendererObj);
            }
            drawRect(0, 0, width, height, 0xFF404040);
            int x0 = (width - COLS * 20) / 2;
            for (int i = 0; i < PER_PAGE; i++) {
                int idx = page * PER_PAGE + i;
                if (idx >= allItems.size()) break;
                int x = x0 + (i % COLS) * 20, y = 4 + (i / COLS) * 20;
                drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
            }
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            for (int i = 0; i < PER_PAGE; i++) {
                int idx = page * PER_PAGE + i;
                if (idx >= allItems.size()) break;
                int x = x0 + (i % COLS) * 20, y = 4 + (i / COLS) * 20;
                try {
                    render.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), allItems.get(idx), x, y);
                } catch (Throwable t) {
                    fail("render item " + allItems.get(idx), t);
                }
            }
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            fontRendererObj.drawString("page " + page, 2, height - 10, 0xFFFFFF);
        }
    }
}
