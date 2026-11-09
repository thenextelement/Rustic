package rustic.dev;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import rustic.common.blocks.BlockCondenserBase;
import rustic.common.blocks.ModBlocks;
import rustic.common.items.ModItems;

/** Server-side gameplay checks for {@link SelfTest}, run in a flat creative world next to the player. */
final class SelfTestWorld {

    private SelfTestWorld() {}

    private static WorldServer world;
    /** Test area origin and the spots prepared for the client-click tests. */
    static int[] origin, clientStake, clientBush, clientGround, clientChair;
    private static FakePlayer fake;

    /** @param y the first air block above the ground at (x0, z0) */
    static void run(WorldServer w, int x0, int y, int z0) {
        world = w;
        world.setWorldTime(6000);
        fake = FakePlayerFactory.getMinecraft(world);
        fake.capabilities.isCreativeMode = false;
        SelfTestLog.log("world tests at {},{},{}", x0, y, z0);
        origin = new int[] { x0, y, z0 };
        clientStake = new int[] { x0 - 8, y, z0 };
        clientBush = new int[] { x0 - 5, y, z0 };
        clientGround = new int[] { x0 - 2, y - 1, z0 };
        clientChair = new int[] { x0 - 11, y, z0 };

        stakeSeeds(x0, y, z0);
        berries(x0 + 3, y, z0);
        condenser(x0 + 6, y, z0, ModBlocks.CONDENSER, ModBlocks.RETORT, false);
        condenser(x0 + 12, y, z0, ModBlocks.CONDENSER_ADVANCED, ModBlocks.RETORT_ADVANCED, true);
        showcase(x0, y, z0 + 4);
        bonemeal(x0 + 20, y, z0);
        ironwood(x0 + 40, y, z0 - 40);

        SelfTestLog.log("world tests finished");
    }

    /** Spots for the client-driven tests: a stake on watered farmland, a ripe bush, clear stone ground. */
    static void prepareClientTests() {
        int[] s = clientStake;
        prepareGround(s[0], s[1], s[2], Blocks.farmland);
        world.setBlock(s[0] - 1, s[1] - 1, s[2], Blocks.water);
        world.setBlock(s[0], s[1], s[2], ModBlocks.CROP_STAKE, 0, 3);
        int[] b = clientBush;
        prepareGround(b[0], b[1], b[2], Blocks.grass);
        world.setBlock(b[0], b[1], b[2], ModBlocks.WILDBERRY_BUSH, 5, 3);
        int[] g = clientGround;
        prepareGround(g[0], g[1] + 1, g[2], Blocks.stone);
        int[] c = clientChair;
        prepareGround(c[0], c[1], c[2], Blocks.stone);
        world.setBlock(c[0], c[1], c[2], ModBlocks.CHAIR_OAK, 0, 3);
    }

    /** Right-click with {@code stack} held by the fake player, as the server handles a real click. */
    private static boolean use(ItemStack stack, int x, int y, int z, int side, float yaw) {
        fake.inventory.currentItem = 0;
        fake.inventory.setInventorySlotContents(0, stack);
        fake.rotationYaw = yaw;
        fake.rotationYawHead = yaw;
        return fake.theItemInWorldManager.activateBlockOrUseItem(fake, world, stack, x, y, z, side, 0.5F, 1F, 0.5F);
    }

    private static int countInInventory(Item item) {
        int n = 0;
        for (ItemStack s : fake.inventory.mainInventory) if (s != null && s.getItem() == item) n += s.stackSize;
        return n;
    }

    private static void clearInventory() {
        for (int i = 0; i < fake.inventory.mainInventory.length; i++) fake.inventory.mainInventory[i] = null;
    }

    private static void prepareGround(int x, int y, int z, Block ground) {
        world.setBlock(x, y - 1, z, ground, 0, 3);
        for (int dy = 0; dy < 4; dy++) world.setBlockToAir(x, y + dy, z);
    }

    private static void stakeSeeds(int x, int y, int z) {
        prepareGround(x, y, z, Blocks.farmland);
        world.setBlock(x - 1, y - 1, z, Blocks.water);
        // place the stake like a player would: use it on the top face of the farmland
        use(new ItemStack(ModBlocks.CROP_STAKE), x, y - 1, z, 1, 0);
        SelfTestLog.check(world.getBlock(x, y, z) == ModBlocks.CROP_STAKE, "crop stake not placed on farmland");
        SelfTestLog.check(
            world.getBlock(x, y - 1, z) == Blocks.farmland,
            "farmland under stake turned into {}",
            Block.blockRegistry.getNameForObject(world.getBlock(x, y - 1, z)));
        // plant tomato seeds on the stake
        use(new ItemStack(ModItems.TOMATO_SEEDS, 4), x, y, z, 2, 0);
        SelfTestLog.check(
            world.getBlock(x, y, z) == ModBlocks.TOMATO_CROP,
            "tomato seeds not planted on stake, block is {}",
            Block.blockRegistry.getNameForObject(world.getBlock(x, y, z)));
        if (world.getBlock(x, y, z) == ModBlocks.TOMATO_CROP) {
            Random rand = new Random(1);
            for (int i = 0; i < 3000 && world.getBlockMetadata(x, y, z) < 3; i++) {
                ModBlocks.TOMATO_CROP.updateTick(world, x, y, z, rand);
                if (world.getBlock(x, y, z) != ModBlocks.TOMATO_CROP) break;
            }
            SelfTestLog.check(world.getBlock(x, y, z) == ModBlocks.TOMATO_CROP, "tomato crop popped off its stake");
            SelfTestLog.check(
                world.getBlockMetadata(x, y, z) >= 3,
                "tomato crop did not grow (age {})",
                world.getBlockMetadata(x, y, z));
            // harvest by right click
            clearInventory();
            use(null, x, y, z, 2, 0);
            SelfTestLog.check(countInInventory(ModItems.TOMATO) > 0, "harvesting a grown tomato crop gave no tomatoes");
        }
    }

    private static void berries(int x, int y, int z) {
        prepareGround(x, y, z, Blocks.grass);
        world.setBlock(x, y, z, ModBlocks.WILDBERRY_BUSH, 5, 3);
        SelfTestLog.check(world.getBlock(x, y, z) == ModBlocks.WILDBERRY_BUSH, "wildberry bush not placed");
        clearInventory();
        boolean used = use(null, x, y, z, 2, 0);
        SelfTestLog.check(used, "right-clicking a ripe wildberry bush did nothing");
        SelfTestLog.check(countInInventory(ModItems.WILDBERRIES) > 0, "picking a ripe wildberry bush gave no berries");
        SelfTestLog.check(
            world.getBlockMetadata(x, y, z) == 0,
            "bush not reset after picking (meta {})",
            world.getBlockMetadata(x, y, z));
        // leave a ripe one for the screenshot
        world.setBlock(x + 1, y - 1, z, Blocks.grass);
        world.setBlock(x + 1, y, z, ModBlocks.WILDBERRY_BUSH, 5, 3);
    }

    private static void condenser(int x, int y, int z, BlockCondenserBase condenser, Block retort, boolean back) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) prepareGround(x + dx, y, z + dz, Blocks.stone);
        // player standing south of the condenser, looking north at it
        use(new ItemStack(condenser), x, y - 1, z, 1, 180F);
        String name = Block.blockRegistry.getNameForObject(condenser);
        SelfTestLog.check(world.getBlock(x, y, z) == condenser, "{} bottom not placed", name);
        SelfTestLog.check(world.getBlock(x, y + 1, z) == condenser, "{} top half not placed", name);
        int bottomMeta = world.getBlockMetadata(x, y, z), topMeta = world.getBlockMetadata(x, y + 1, z);
        SelfTestLog
            .check(BlockCondenserBase.isBottom(bottomMeta), "{} bottom meta {} not marked bottom", name, bottomMeta);
        SelfTestLog.check(!BlockCondenserBase.isBottom(topMeta), "{} top meta {} marked bottom", name, topMeta);
        SelfTestLog
            .log("{} facing {} (meta {}/{})", name, BlockCondenserBase.getFacing(bottomMeta), bottomMeta, topMeta);

        // retorts placed by a player standing in front of the condenser (looking north): they must still turn to face
        // away from it
        use(new ItemStack(retort), x + 1, y - 1, z, 1, 180F);
        use(new ItemStack(retort), x - 1, y - 1, z, 1, 180F);
        if (back) use(new ItemStack(retort), x, y - 1, z - 1, 1, 180F);
        SelfTestLog.check(world.getBlock(x + 1, y, z) == retort, "{} east retort not placed", name);
        SelfTestLog.check(world.getBlock(x - 1, y, z) == retort, "{} west retort not placed", name);
        SelfTestLog.check(condenser.hasRetorts(world, x, y, z), "{} does not detect its retorts", name);
    }

    private static int countBushesAround(int x, int y, int z) {
        int n = 0;
        for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++)
            if ((i != 0 || j != 0) && world.getBlock(x + i, y, z + j) == ModBlocks.WILDBERRY_BUSH) n++;
        return n;
    }

    private static void bonemeal(int x, int y, int z) {
        for (int i = -2; i <= 2; i++) for (int j = -2; j <= 2; j++) prepareGround(x + i, y, z + j, Blocks.grass);
        world.setBlock(x, y, z, ModBlocks.WILDBERRY_BUSH, 0, 3);
        use(new ItemStack(net.minecraft.init.Items.dye, 64, 15), x, y, z, 1, 0);
        SelfTestLog.check(world.getBlockMetadata(x, y, z) > 0, "bonemeal did not grow a young wildberry bush");
        for (int i = 0; i < 40; i++) use(new ItemStack(net.minecraft.init.Items.dye, 64, 15), x, y, z, 1, 0);
        SelfTestLog.check(
            world.getBlockMetadata(x, y, z) == 5,
            "bonemeal did not ripen a wildberry bush (age {})",
            world.getBlockMetadata(x, y, z));
        int around = countBushesAround(x, y, z);
        SelfTestLog.check(around >= 1, "bonemeal on a ripe bush never spread it");
        SelfTestLog.check(
            around <= rustic.common.blocks.crops.BlockBerryBush.MAX_NEIGHBOUR_BUSHES,
            "bush spread to {} of its 8 neighbours",
            around);
        SelfTestLog.log("bonemealed bush has {} neighbouring bushes", around);
    }

    /** Forces the ironwood generator (100% chance) over a plains area and counts the trees. */
    private static void ironwood(int x, int y, int z) {
        float chance = rustic.common.Config.IRONWOOD_GEN_CHANCE, olive = rustic.common.Config.OLIVE_GEN_CHANCE;
        rustic.common.Config.IRONWOOD_GEN_CHANCE = 1F;
        rustic.common.Config.OLIVE_GEN_CHANCE = 0F;
        Random rand = new Random(42);
        rustic.common.world.WorldGenAllTrees trees = new rustic.common.world.WorldGenAllTrees();
        int chunks = 0, generated = 0;
        try {
            for (int cx = 0; cx < 4; cx++) for (int cz = 0; cz < 4; cz++) {
                chunks++;
                if (trees.generate(world, rand, x + cx * 16, y, z + cz * 16)) generated++;
            }
        } finally {
            rustic.common.Config.IRONWOOD_GEN_CHANCE = chance;
            rustic.common.Config.OLIVE_GEN_CHANCE = olive;
        }
        int logs = 0;
        for (int dx = -8; dx < 72; dx++) for (int dz = -8; dz < 72; dz++) for (int dy = 0; dy < 14; dy++) {
            if (world.getBlock(x + dx, y + dy, z + dz) == ModBlocks.LOG
                && (world.getBlockMetadata(x + dx, y + dy, z + dz) & 3) == 1) logs++;
        }
        SelfTestLog
            .log("forced ironwood generation: {}/{} chunks got trees, {} ironwood logs", generated, chunks, logs);
        SelfTestLog.check(generated > 0 && logs > 0, "ironwood trees never generate even at 100% chance");
        ironwoodSpot = new int[] { x, y, z };
    }

    static int[] ironwoodSpot;

    /** A row of decorative / special-model blocks for the world screenshot. */
    private static void showcase(int x, int y, int z) {
        Block[] blocks = { ModBlocks.CABINET, ModBlocks.BREWING_BARREL, ModBlocks.CROP_STAKE, ModBlocks.ROPE };
        for (int i = 0; i < blocks.length; i++) {
            prepareGround(x + i * 2, y, z, Blocks.stone);
            use(new ItemStack(blocks[i]), x + i * 2, y - 1, z, 1, 180F);
        }
    }
}
