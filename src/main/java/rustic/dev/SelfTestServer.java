package rustic.dev;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Dedicated-server variant of {@link SelfTest}: if a file named {@code rustic_selftest} exists in the server directory,
 * runs the gameplay checks at spawn (no player needed), logs the result and stops the server.
 */
public final class SelfTestServer {

    private int ticks;
    private boolean done;

    private SelfTestServer() {}

    /** Call from FMLServerStartedEvent. */
    public static void initIfRequested() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || !server.isDedicatedServer() || !new File("rustic_selftest").exists()) return;
        FMLCommonHandler.instance()
            .bus()
            .register(new SelfTestServer());
        SelfTestLog.log("enabled (dedicated server)");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || done || ++ticks < 40) return;
        done = true;
        MinecraftServer server = MinecraftServer.getServer();
        try {
            WorldServer world = server.worldServerForDimension(0);
            int x = world.getSpawnPoint().posX, z = world.getSpawnPoint().posZ;
            int y = world.getTopSolidOrLiquidBlock(x, z);
            SelfTestWorld.run(world, x, y, z);
        } catch (Throwable t) {
            SelfTestLog.fail("world tests", t);
        }
        SelfTestLog.done();
        server.initiateShutdown();
    }
}
