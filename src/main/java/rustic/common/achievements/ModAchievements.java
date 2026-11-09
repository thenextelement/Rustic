package rustic.common.achievements;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatisticsFile;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import rustic.common.blocks.ModBlocks;
import rustic.common.blocks.fluids.FluidBooze;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.items.ModItems;
import rustic.common.potions.PotionsRustic;

/**
 * 1.7.10 replacement for the 1.12 advancements (assets/rustic/advancements/main/*.json). The criteria are checked
 * from events and a periodic inventory scan, since 1.7.10 has no inventory_changed / effects_changed triggers.
 */
public final class ModAchievements {

    public static Achievement ROOT;
    public static Achievement ALMANAC;
    public static Achievement MAX_DRUNK;
    public static Achievement THE_GOOD_STUFF;
    public static Achievement WINEMAXXING;

    private ModAchievements() {}

    public static void init() {
        ROOT = create("root", 0, 0, new ItemStack(ModBlocks.VASE), null);
        ALMANAC = create("almanac", -2, 2, new ItemStack(ModItems.BOOK), ROOT);
        MAX_DRUNK = create("max_drunk", 0, 2, ModItems.FLUID_BOTTLE.getFilledBottle(ModFluids.ALE), ROOT).setSpecial();
        THE_GOOD_STUFF = create("the_good_stuff", 2, 2, new ItemStack(ModBlocks.BREWING_BARREL), ROOT);
        WINEMAXXING = create("winemaxxing", 2, 4, ModItems.FLUID_BOTTLE.getFilledBottle(ModFluids.WINE), THE_GOOD_STUFF)
            .setSpecial();

        AchievementPage.registerAchievementPage(
            new AchievementPage("Rustic", ROOT, ALMANAC, MAX_DRUNK, THE_GOOD_STUFF, WINEMAXXING));

        Handler handler = new Handler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);
    }

    private static Achievement create(String name, int col, int row, ItemStack icon, Achievement parent) {
        Achievement a = new Achievement("achievement.rustic." + name, "rustic." + name, col, row, icon, parent);
        if (parent == null) a.initIndependentStat();
        return a.registerStat();
    }

    /** Unlocks {@code a} once its parent is unlocked (1.7.10 silently ignores out-of-order unlocks). */
    private static void grant(EntityPlayerMP player, Achievement a) {
        StatisticsFile stats = player.func_147099_x();
        if (stats.hasAchievementUnlocked(a)) return;
        if (a.parentAchievement != null && !stats.hasAchievementUnlocked(a.parentAchievement)) {
            grant(player, a.parentAchievement);
        }
        player.triggerAchievement(a);
    }

    public static class Handler {

        @SubscribeEvent
        public void onItemConsumed(PlayerUseItemEvent.Finish event) {
            if (event.entityPlayer instanceof EntityPlayerMP) {
                grant((EntityPlayerMP) event.entityPlayer, ROOT);
            }
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
            EntityPlayer player = event.player;
            if (player.ticksExisted % 20 != 0) return;
            EntityPlayerMP mp = (EntityPlayerMP) player;
            StatisticsFile stats = mp.func_147099_x();

            PotionEffect tipsy = player.getActivePotionEffect(PotionsRustic.TIPSY);
            if (tipsy != null && tipsy.getAmplifier() >= 3 && !tipsy.getIsAmbient()) {
                grant(mp, MAX_DRUNK);
            }

            boolean needAlmanac = !stats.hasAchievementUnlocked(ALMANAC);
            boolean needGoodStuff = !stats.hasAchievementUnlocked(THE_GOOD_STUFF);
            boolean needWine = !stats.hasAchievementUnlocked(WINEMAXXING);
            if (!needAlmanac && !needGoodStuff && !needWine) return;

            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack == null) continue;
                if (needAlmanac && stack.getItem() == ModItems.BOOK) {
                    grant(mp, ALMANAC);
                    needAlmanac = false;
                }
                if ((needGoodStuff || needWine) && stack.getItem() instanceof IFluidContainerItem) {
                    FluidStack fs = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                    if (fs == null || fs.amount <= 0) continue;
                    Fluid fluid = fs.getFluid();
                    if (!(fluid instanceof FluidBooze)) continue;
                    float quality = ((FluidBooze) fluid).getQuality(fs);
                    if (needGoodStuff && quality >= 0.8999F) {
                        grant(mp, THE_GOOD_STUFF);
                        needGoodStuff = false;
                    }
                    if (needWine && fluid == ModFluids.WINE && quality >= 0.999F) {
                        grant(mp, WINEMAXXING);
                        needWine = false;
                    }
                }
            }
        }
    }

}
