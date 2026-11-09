package rustic.common.blocks.fluids;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import rustic.common.potions.PotionsRustic;
import rustic.common.util.RusticUtils;

public class ModFluids {

    public static Fluid OLIVE_OIL;
    public static Fluid IRONBERRY_JUICE;
    public static Fluid WILDBERRY_JUICE;
    public static Fluid GRAPE_JUICE;
    public static Fluid APPLE_JUICE;
    public static Fluid ALE_WORT;
    public static Fluid HONEY;
    public static Fluid GOLDEN_APPLE_JUICE;

    public static Fluid VANTA_OIL;

    public static Fluid ALE;
    public static Fluid CIDER;
    public static Fluid IRON_WINE;
    public static Fluid MEAD;
    public static Fluid WILDBERRY_WINE;
    public static Fluid WINE;
    public static Fluid AMBROSIA;

    private static List<Fluid> FLUIDS = new ArrayList<Fluid>();

    public static BlockFluidRustic BLOCK_OLIVE_OIL;
    public static BlockFluidRustic BLOCK_IRONBERRY_JUICE;
    public static BlockFluidRustic BLOCK_WILDBERRY_JUICE;
    public static BlockFluidRustic BLOCK_GRAPE_JUICE;
    public static BlockFluidRustic BLOCK_APPLE_JUICE;
    public static BlockFluidRustic BLOCK_ALE_WORT;
    public static BlockFluidRustic BLOCK_HONEY;
    public static BlockFluidRustic BLOCK_GOLDEN_APPLE_JUICE;

    public static void init() {
        OLIVE_OIL = new FluidDrinkable("oliveoil", "rustic:fluids/olive_oil_still", "rustic:fluids/olive_oil_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 0.4F);
                player.addPotionEffect(new PotionEffect(Potion.confusion.id, 600, 1));
            }
        }.setDensity(920)
            .setViscosity(2000);
        register(OLIVE_OIL);

        IRONBERRY_JUICE = new FluidDrinkable(
            "ironberryjuice",
            "rustic:fluids/ironberry_juice_still",
            "rustic:fluids/ironberry_juice_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 0.8F);
                // player.addPotionEffect(new PotionEffect(Potion.resistance.id, 600, 2, false, false));
                // player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 600, 2, false, false));
                // player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 600, 15, false, false));
                // player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 600, 15, false, false));
                // player.addPotionEffect(new PotionEffect(Potion.weakness.id, 600, 15, false, false));
                // player.addPotionEffect(new PotionEffect(Potion.jump.id, 600, 250, false, false));
                int duration = 30 * 20;
                PotionEffect effect = player.getActivePotionEffect(PotionsRustic.FULLMETAL_POTION);
                if (effect == null) {
                    player.addPotionEffect(new PotionEffect(PotionsRustic.FULLMETAL_POTION.id, duration, 0, false));
                } else {
                    player.addPotionEffect(
                        new PotionEffect(PotionsRustic.FULLMETAL_POTION.id, effect.getDuration() + duration, 0, false));
                }
            }
        }.setDensity(1100)
            .setViscosity(1100);
        register(IRONBERRY_JUICE);

        WILDBERRY_JUICE = new FluidDrinkable(
            "wildberryjuice",
            "rustic:fluids/wildberry_juice_still",
            "rustic:fluids/wildberry_juice_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 1F);
                if (player.getRNG()
                    .nextFloat() < 0.2F) {
                    player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 75));
                }
                if (player.getRNG()
                    .nextFloat() < 0.1F) {
                    player.addPotionEffect(new PotionEffect(Potion.poison.id, 200));
                }
            }
        }.setDensity(1070)
            .setViscosity(1100);
        register(WILDBERRY_JUICE);

        GRAPE_JUICE = new FluidDrinkable(
            "grapejuice",
            "rustic:fluids/grape_juice_still",
            "rustic:fluids/grape_juice_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 0.9F);
            }
        }.setDensity(1070)
            .setViscosity(1100);
        register(GRAPE_JUICE);

        APPLE_JUICE = new FluidDrinkable(
            "applejuice",
            "rustic:fluids/apple_juice_still",
            "rustic:fluids/apple_juice_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 1.2F);
            }
        }.setDensity(1050)
            .setViscosity(1100);
        register(APPLE_JUICE);

        ALE_WORT = new FluidDrinkable("alewort", "rustic:fluids/ale_wort_still", "rustic:fluids/ale_wort_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 2F);
                player.addPotionEffect(new PotionEffect(Potion.confusion.id, 400, 1));
            }
        }.setDensity(1004)
            .setViscosity(2000);
        register(ALE_WORT);

        HONEY = new FluidDrinkable("honey", "rustic:fluids/honey_still", "rustic:fluids/honey_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(3, 0.4F);
                if (player.getRNG()
                    .nextFloat() < 0.6F) {
                    player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 75));
                }
            }
        }.setDensity(1433)
            .setViscosity(5500);
        register(HONEY);

        GOLDEN_APPLE_JUICE = new FluidDrinkable(
            "goldenapplejuice",
            "rustic:fluids/golden_apple_juice_still",
            "rustic:fluids/golden_apple_juice_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(3, 1.8F);
                player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 400, 1));
                player.addPotionEffect(new PotionEffect(Potion.resistance.id, 6000, 0));
                player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 6000, 0));
                player.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 2400, 3));
            }

            @Override
            public EnumRarity getRarity() {
                return EnumRarity.epic;
            }
        }.setDensity(1050)
            .setViscosity(1100);
        register(GOLDEN_APPLE_JUICE);

        VANTA_OIL = new FluidDrinkable("vantaoil", "rustic:fluids/vanta_oil_still", "rustic:fluids/vanta_oil_flow") {

            @Override
            public void onDrank(World world, EntityPlayer player, ItemStack stack, FluidStack fluid) {
                player.getFoodStats()
                    .addStats(1, 0.4F);
                player.addPotionEffect(new PotionEffect(Potion.blindness.id, 600, 0));
            }
        }.setDensity(920)
            .setViscosity(2000);
        register(VANTA_OIL);

        ALE = new FluidBooze("ale", "rustic:fluids/booze/ale_still", "rustic:fluids/booze/ale_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 4F * quality;
                    player.getFoodStats()
                        .addStats(2, saturation);
                    int duration = 1200 + ((int) (10800 * (Math.max(Math.abs((quality - 0.5F) * 2F), 0F))));
                    player.addPotionEffect(new PotionEffect(PotionsRustic.FULL_POTION.id, duration));
                } else {
                    int duration = (int) (6000 * Math.max(1 - quality, 0.25));
                    player.addPotionEffect(new PotionEffect(Potion.hunger.id, duration));
                    player.addPotionEffect(new PotionEffect(Potion.confusion.id, duration));
                }
            }
        }.setInebriationChance(0.5F)
            .setDensity(1004)
            .setViscosity(1016);
        register(ALE, false);

        CIDER = new FluidBooze("cider", "rustic:fluids/booze/cider_still", "rustic:fluids/booze/cider_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 2F * quality;
                    player.getFoodStats()
                        .addStats(1, saturation);
                    int duration = 1200 + ((int) (10800 * (Math.max(Math.abs((quality - 0.5F) * 2F), 0F))));
                    player.addPotionEffect(new PotionEffect(PotionsRustic.MAGIC_RESISTANCE_POTION.id, duration));
                } else {
                    int duration = (int) (1200 * Math.max(1 - quality, 0.25F));
                    player.addPotionEffect(new PotionEffect(Potion.poison.id, duration));
                    duration = (int) (6000 * Math.max(1 - quality, 0.25F));
                    player.addPotionEffect(new PotionEffect(Potion.confusion.id, duration));
                }
            }
        }.setInebriationChance(0.5F)
            .setDensity(1004)
            .setViscosity(1400);
        register(CIDER, false);

        IRON_WINE = new FluidBooze(
            "ironwine",
            "rustic:fluids/booze/iron_wine_still",
            "rustic:fluids/booze/iron_wine_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 2F * quality;
                    float absorption = 10F * (Math.max((quality - 0.5F) * 2F, 0F));
                    player.getFoodStats()
                        .addStats(1, saturation);
                    player.setAbsorptionAmount(
                        Math.max(
                            Math.min(player.getAbsorptionAmount() + absorption, 20F),
                            player.getAbsorptionAmount()));
                } else {
                    int duration = (int) (6000 * Math.max(1 - quality, 0.25));
                    float damage = 10F * (Math.max(Math.abs(quality - 0.5F) + 0.1F, 0.25F));
                    player.attackEntityFrom(DamageSource.magic, damage);
                    player.addPotionEffect(new PotionEffect(Potion.confusion.id, duration));
                }
            }
        }.setInebriationChance(0.5F)
            .setDensity(1034)
            .setViscosity(1400);
        register(IRON_WINE, false);

        MEAD = new FluidBooze("mead", "rustic:fluids/booze/mead_still", "rustic:fluids/booze/mead_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 2F * quality;
                    player.getFoodStats()
                        .addStats(1, saturation);
                    int duration = 1200 + ((int) (6000 * (Math.max(Math.abs((quality - 0.5F) * 2F), 0F))));
                    player.addPotionEffect(new PotionEffect(PotionsRustic.WITHER_WARD_POTION.id, duration));
                } else {
                    int duration = (int) (800 * Math.max(1 - quality, 0.25));
                    player.addPotionEffect(new PotionEffect(Potion.wither.id, duration));
                    duration = (int) (6000 * Math.max(1 - quality, 0.25));
                    player.addPotionEffect(new PotionEffect(Potion.confusion.id, duration));
                }
            }
        }.setInebriationChance(0.5F)
            .setDensity(1034)
            .setViscosity(1500);
        register(MEAD, false);

        WILDBERRY_WINE = new FluidBooze(
            "wildberrywine",
            "rustic:fluids/booze/wildberry_wine_still",
            "rustic:fluids/booze/wildberry_wine_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 2F * quality;
                    player.getFoodStats()
                        .addStats(1, saturation);
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        if (!Potion.potionTypes[effect.getPotionID()].isBadEffect() && effect.getAmplifier() < 2) {
                            player.addPotionEffect(
                                new PotionEffect(
                                    effect.getPotionID(),
                                    effect.getDuration(),
                                    effect.getAmplifier() + 1,
                                    effect.getIsAmbient()));
                        }
                    }
                } else {
                    PotionEffect[] effects = player.getActivePotionEffects()
                        .toArray(new PotionEffect[0]);
                    for (int i = 0; i < effects.length; i++) {
                        PotionEffect effect = effects[i];
                        if (!Potion.potionTypes[effect.getPotionID()].isBadEffect()) {
                            if (effect.getAmplifier() > 0) {
                                player.removePotionEffect(effect.getPotionID());
                                player.addPotionEffect(
                                    new PotionEffect(
                                        effect.getPotionID(),
                                        effect.getDuration(),
                                        effect.getAmplifier() - 1,
                                        effect.getIsAmbient()));
                            } else if (effect.getAmplifier() == 0) {
                                player.removePotionEffect(effect.getPotionID());
                            }
                        }
                    }
                    int duration = (int) (6000 * Math.max(1 - quality, 0.25F));
                    player.addPotionEffect(new PotionEffect(Potion.confusion.id, duration));
                }
            }
        }.setInebriationChance(0.85F)
            .setDensity(1034)
            .setViscosity(1500);
        register(WILDBERRY_WINE, false);

        WINE = new FluidBooze("wine", "rustic:fluids/booze/wine_still", "rustic:fluids/booze/wine_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 2F * quality;
                    player.getFoodStats()
                        .addStats(1, saturation);
                    int durationIncrease = 600 + ((int) (2400 * ((quality - 0.5F) * 2F)));
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        if (!Potion.potionTypes[effect.getPotionID()].isBadEffect() && effect.getDuration() < 12000) {
                            int duration = Math
                                .max(Math.min(effect.getDuration() + durationIncrease, 12000), effect.getDuration());
                            player.addPotionEffect(
                                new PotionEffect(
                                    effect.getPotionID(),
                                    duration,
                                    effect.getAmplifier(),
                                    effect.getIsAmbient()));
                        }
                    }
                } else {
                    PotionEffect[] effects = player.getActivePotionEffects()
                        .toArray(new PotionEffect[0]);
                    for (int i = 0; i < effects.length; i++) {
                        PotionEffect effect = effects[i];
                        int durationDecrease = (int) (2400 * (Math.abs(quality - 0.6)));
                        if (!Potion.potionTypes[effect.getPotionID()].isBadEffect()) {
                            int duration = effect.getDuration() - durationDecrease;
                            if (duration > 0) {
                                player.removePotionEffect(effect.getPotionID());
                                player.addPotionEffect(
                                    new PotionEffect(
                                        effect.getPotionID(),
                                        duration,
                                        effect.getAmplifier(),
                                        effect.getIsAmbient()));
                            } else {
                                player.removePotionEffect(effect.getPotionID());
                            }
                        }
                    }
                    int duration = (int) (6000 * Math.max(1 - quality, 0));
                    player.addPotionEffect(new PotionEffect(Potion.confusion.id, duration));
                }
            }
        }.setInebriationChance(0.5F)
            .setDensity(1034)
            .setViscosity(1500);
        register(WINE, false);

        AMBROSIA = new FluidBooze(
            "ambrosia",
            "rustic:fluids/booze/ambrosia_still",
            "rustic:fluids/booze/ambrosia_flow") {

            @Override
            protected void affectPlayer(World world, EntityPlayer player, float quality) {
                if (quality >= 0.5F) {
                    float saturation = 2F * quality;
                    player.getFoodStats()
                        .addStats(2, saturation);
                    int duration;
                    if (quality > 0.99F) {
                        duration = 999999;// Short.MAX_VALUE;
                        player.removePotionEffect(PotionsRustic.UNDYING_POTION.id);
                    } else {
                        duration = (20 * 300) + ((int) (18000 * (Math.max(Math.abs((quality - 0.5F) * 2F), 0.00F))));
                    }
                    player.addPotionEffect(new PotionEffect(PotionsRustic.UNDYING_POTION.id, duration, 0, false));
                } else {
                    player.attackEntityFrom(RusticUtils.BAD_AMBROSIA_DAMAGE, Float.MAX_VALUE);
                }
            }

            @Override
            public EnumRarity getRarity() {
                return EnumRarity.epic;
            }

            @Override
            protected void inebriate(World world, EntityPlayer player, float quality) {
                int duration = (quality >= 0.5f) ? ((int) (12000 * (Math.max(1 - Math.abs(quality - 0.75F), 0.5F))))
                    : ((int) (12000 * (Math.max(1 - (quality * 0.5F), 0.5F))));
                PotionEffect tipsyEffect = player.getActivePotionEffect(PotionsRustic.TIPSY);
                if (world.rand.nextFloat() < this.inebriationChance) {
                    if (tipsyEffect == null) {
                        int amp = (quality > 0.99F) ? 1 : 0;
                        player.addPotionEffect(new PotionEffect(PotionsRustic.TIPSY.id, duration, amp, false));
                    } else if (tipsyEffect.getAmplifier() < 3) {
                        int newAmp = Math.min(tipsyEffect.getAmplifier() + ((quality > 0.99F) ? 2 : 1), 3);
                        player.addPotionEffect(
                            new PotionEffect(
                                PotionsRustic.TIPSY.id,
                                Math.max(duration, tipsyEffect.getDuration()),
                                newAmp,
                                false));
                    }
                }
            }
        }.setInebriationChance(1.0F)
            .setDensity(1004)
            .setViscosity(1400);
        register(AMBROSIA, false);

        BLOCK_OLIVE_OIL = new BlockFluidRustic("olive_oil", OLIVE_OIL, Material.water);
        BLOCK_OLIVE_OIL.setQuantaPerBlock(4);

        BLOCK_IRONBERRY_JUICE = new BlockFluidRustic("ironberry_juice", IRONBERRY_JUICE, Material.water);
        BLOCK_IRONBERRY_JUICE.setQuantaPerBlock(6);

        BLOCK_WILDBERRY_JUICE = new BlockFluidRustic("wildberry_juice", WILDBERRY_JUICE, Material.water);
        BLOCK_WILDBERRY_JUICE.setQuantaPerBlock(6);

        BLOCK_GRAPE_JUICE = new BlockFluidRustic("grape_juice", GRAPE_JUICE, Material.water);
        BLOCK_GRAPE_JUICE.setQuantaPerBlock(6);

        BLOCK_APPLE_JUICE = new BlockFluidRustic("apple_juice", APPLE_JUICE, Material.water);
        BLOCK_APPLE_JUICE.setQuantaPerBlock(6);

        BLOCK_ALE_WORT = new BlockFluidRustic("ale_wort", ALE_WORT, Material.water);
        BLOCK_ALE_WORT.setQuantaPerBlock(4);

        BLOCK_HONEY = new BlockFluidRustic("honey", HONEY, Material.water) {

            @Override
            public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
                entity.motionX *= 0.5F;
                entity.motionY *= 0.5F;
                entity.motionZ *= 0.5F;
            }
        };
        BLOCK_HONEY.setQuantaPerBlock(2);

        BLOCK_GOLDEN_APPLE_JUICE = new BlockFluidRustic("golden_apple_juice", GOLDEN_APPLE_JUICE, Material.water) {

            @Override
            public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
                if ((entity != null) && (entity instanceof EntityLivingBase) && ((entity.ticksExisted % 10) == 0)) {
                    EntityLivingBase e = (EntityLivingBase) entity;
                    e.addPotionEffect(new PotionEffect(Potion.regeneration.id, 100, 0, true));
                }
            }
        };
        BLOCK_GOLDEN_APPLE_JUICE.setQuantaPerBlock(6);
    }

    private static void register(Fluid fluid) {
        if (!FluidRegistry.registerFluid(fluid)) {
            fluid = FluidRegistry.getFluid(fluid.getName());
        }
        FLUIDS.add(fluid);
    }

    private static void register(Fluid fluid, boolean addBucket) {
        if (!FluidRegistry.registerFluid(fluid)) {
            fluid = FluidRegistry.getFluid(fluid.getName());
        }
        FLUIDS.add(fluid);
    }

    public static ArrayList<Fluid> getFluids() {
        return new ArrayList<Fluid>(FLUIDS);
    }

}
