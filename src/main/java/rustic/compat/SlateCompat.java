package rustic.compat;

import net.minecraft.block.Block;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;
import rustic.common.Config;
import rustic.common.blocks.ModBlocks;
import rustic.core.Rustic;

/**
 * Slate integration with Et Futurum Requiem and Chisel: when EFR's deepslate exists, Rustic stops generating slate,
 * and with Chisel the slate blocks share a carving group with deepslate, so slate is made by chiseling deepslate.
 */
public final class SlateCompat {

    public static final String CHISEL_GROUP = "rustic_slate";
    private static Boolean deepslatePresent;

    private SlateCompat() {}

    /** True when Et Futurum Requiem is installed with its deepslate block enabled. */
    public static boolean deepslatePresent() {
        if (deepslatePresent == null) {
            deepslatePresent = Loader.isModLoaded("etfuturum")
                && GameRegistry.findBlock("etfuturum", "deepslate") != null;
            if (deepslatePresent)
                Rustic.logger.info("Et Futurum deepslate found: Rustic slate world generation is disabled");
        }
        return deepslatePresent;
    }

    /** Whether Rustic should generate slate veins. */
    public static boolean generateSlate() {
        return Config.ENABLE_SLATE && !deepslatePresent();
    }

    /** Sends Chisel its carving group; call during init. */
    public static void init() {
        if (!Config.ENABLE_SLATE || !Loader.isModLoaded("chisel")) return;
        if (deepslatePresent()) addVariation("etfuturum:deepslate", 0);
        for (Block block : new Block[] { ModBlocks.SLATE, ModBlocks.SLATE_ROOF, ModBlocks.SLATE_TILE,
            ModBlocks.SLATE_BRICK, ModBlocks.SLATE_CHISELED }) {
            if (block != null) addVariation(Block.blockRegistry.getNameForObject(block), 0);
        }
    }

    private static void addVariation(String block, int meta) {
        FMLInterModComms.sendMessage("chisel", "variation:add", CHISEL_GROUP + "|" + block + "|" + meta);
    }
}
