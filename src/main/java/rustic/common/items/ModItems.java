package rustic.common.items;

import java.util.List;
import java.util.Random;



import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import net.minecraft.util.DamageSource;



import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.common.Config;
import rustic.common.blocks.ModBlocks;
import rustic.common.entities.EntityTomato;
import rustic.common.potions.PotionsRustic;
import rustic.core.Rustic;

public class ModItems {
	
	public static ItemBook BOOK;

	public static ItemBase BEE;
	public static ItemBase HONEYCOMB;
	public static ItemBase BEESWAX;
	public static ItemBase TALLOW;
	public static ItemFoodBase OLIVES;
	public static ItemFoodBase IRONBERRIES;
	public static ItemFluidBottle FLUID_BOTTLE;
	public static ItemBase IRON_DUST_TINY;
	public static ItemBase GOLD_DUST;
	public static ItemElixir ELIXIR;
	public static ItemFoodBase TOMATO;
	public static ItemStakeCropSeed TOMATO_SEEDS;
	public static ItemFoodBase CHILI_PEPPER;
	public static ItemFoodBase GHOST_PEPPER;
	public static ItemStakeCropSeed CHILI_PEPPER_SEEDS;
	public static ItemFoodBase WILDBERRIES;
	public static ItemFoodBase GRAPES;

	public static void init() {
		BOOK = new ItemBook();
		
		BEE = new ItemBase("bee");
		BEE.setCreativeTab(Rustic.farmingTab);
		HONEYCOMB = new ItemBase("honeycomb");
		HONEYCOMB.setCreativeTab(Rustic.farmingTab);
		BEESWAX = new ItemBase("beeswax");
		TALLOW = new ItemBase("tallow");
		OLIVES = new ItemFoodBase("olives", 1, 0.4F, false) {
			@Override
			public void initFood() {
				setPotionEffect(Potion.confusion.id, 10, 1, 0.95F);
			}

			@Override
			public int getMaxItemUseDuration(ItemStack stack) {
				return 24;
			}
		};
		IRONBERRIES = new ItemFoodBase("ironberries", 2, 0.4F, false) {
			@Override
			public void initFood() {
				setAlwaysEdible();
			}

			@Override
			public int getMaxItemUseDuration(ItemStack stack) {
				return 16;
			}

			@Override
			protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
				if (!worldIn.isRemote) {
					//player.addPotionEffect(new PotionEffect(Potion.resistance.id, 200, 2, false, false));
					//player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 200, 2, false, false));
					//player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 15, false, false));
					//player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 200, 15, false, false));
					//player.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 15, false, false));
					//player.addPotionEffect(new PotionEffect(Potion.jump.id, 200, 250, false, false));
					int duration = 15 * 20;
					PotionEffect effect = player.getActivePotionEffect(PotionsRustic.FULLMETAL_POTION);
					if (effect == null) {
						player.addPotionEffect(new PotionEffect(PotionsRustic.FULLMETAL_POTION.id, duration, 0, false));
					} else {
						player.addPotionEffect(new PotionEffect(PotionsRustic.FULLMETAL_POTION.id, effect.getDuration() + duration, 0, false));
					}
				}
			}
		};
		FLUID_BOTTLE = new ItemFluidBottle();
		IRON_DUST_TINY = new ItemBase("dust_tiny_iron");
		IRON_DUST_TINY.setCreativeTab(Rustic.farmingTab);
		GOLD_DUST = new ItemBase("dust_gold");
		GOLD_DUST.setCreativeTab(Rustic.farmingTab);
		ELIXIR = new ItemElixir();
		TOMATO = new ItemFoodBase("tomato", 4, 0.4F, false) {
			@Override
			public ItemStack onItemRightClick(ItemStack itemstack, World worldIn, EntityPlayer playerIn) {
				if (playerIn.isSneaking()) {
					if (!playerIn.capabilities.isCreativeMode) {
						itemstack.stackSize -= 1;
					}
					worldIn.playSoundAtEntity(playerIn, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
					if (!worldIn.isRemote) {
						EntityTomato entitytomato = new EntityTomato(worldIn, playerIn);
						worldIn.spawnEntityInWorld(entitytomato);
					}
					return itemstack;
				}
				return super.onItemRightClick(itemstack, worldIn, playerIn);
			}
		};
		TOMATO_SEEDS = new ItemStakeCropSeed("tomato_seeds", ModBlocks.TOMATO_CROP);
		if (Config.ENABLE_SEED_DROPS) {
			MinecraftForge.addGrassSeed(new ItemStack(TOMATO_SEEDS), Config.SEED_DROP_RATE);
		}
		CHILI_PEPPER = new ItemFoodBase("chili_pepper", 3, 0.4F, false) {
			@Override
			public void initFood() {
				setAlwaysEdible();
			}
			private Random rand = new Random();
			@Override
			protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
				if (!worldIn.isRemote) {
					player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 400));
					if (rand.nextInt(24) == 0) {
						player.attackEntityFrom(DamageSource.onFire, 1.0F);
					}
				}
			}
		};
		GHOST_PEPPER = new ItemFoodBase("ghost_pepper", 4, 0.7F, false) {
			@Override
			public void initFood() {
				setAlwaysEdible();
			}

			@Override
			protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
				if (!worldIn.isRemote) {
					player.attackEntityFrom(DamageSource.onFire, 2.0F);
					//player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 1200, 0));
					player.addPotionEffect(new PotionEffect(PotionsRustic.FIRE_POWER_POTION.id, 1200, 0));
				}
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
				tooltip.add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("tooltip.rustic.ghost_pepper"));
			}
		};
		CHILI_PEPPER_SEEDS = new ItemStakeCropSeed("chili_pepper_seeds", ModBlocks.CHILI_CROP);
		if (Config.ENABLE_SEED_DROPS) {
			MinecraftForge.addGrassSeed(new ItemStack(CHILI_PEPPER_SEEDS), Config.SEED_DROP_RATE);
		}
		WILDBERRIES = new ItemFoodBase("wildberries", 2, 0.5F, false) {
			@Override
			public int getMaxItemUseDuration(ItemStack stack) {
				return 16;
			}
		};
		GRAPES = new ItemFoodBase("grapes", 3, 0.3F, false) {
			@Override
			public int getMaxItemUseDuration(ItemStack stack) {
				return 16;
			}
		};
	}

}
