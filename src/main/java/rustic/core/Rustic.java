package rustic.core;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import rustic.common.Config;
import rustic.common.EventHandlerCommon;
import rustic.common.blocks.ModBlocks;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.crafting.Recipes;
import rustic.common.items.ModItems;
import rustic.common.potions.EventHandlerPotions;
import rustic.compat.Compat;

@Mod(modid = Rustic.MODID, name = Rustic.NAME, version = Rustic.VERSION, dependencies = Rustic.DEPENDENCIES)
public class Rustic {

	public static final String MODID = "rustic";
	public static final String NAME = "Rustic";
	public static final String VERSION = Tags.VERSION;
	public static final String DEPENDENCIES = "required-after:Forge;after:Forestry;after:NotEnoughItems";

	@SidedProxy(clientSide = "rustic.core.ClientProxy", serverSide = "rustic.core.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTabs decorTab = new CreativeTabs("rustic.decor") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return Item.getItemFromBlock(ModBlocks.VASE);
		}
	};

	public static CreativeTabs farmingTab = new CreativeTabs("rustic.farming") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ModItems.OLIVES;
		}

		@Override
		@SideOnly(Side.CLIENT)
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void displayAllReleventItems(List list) {
			super.displayAllReleventItems(list);
			for (Fluid fluid : ModFluids.getFluids()) {
				ItemStack bucket = rustic.common.util.FluidHelper.getFilledBucket(new net.minecraftforge.fluids.FluidStack(fluid, 1000));
				if (bucket != null) list.add(bucket);
			}
		}
	};

	public static CreativeTabs alchemyTab = new CreativeTabs("rustic.alchemy") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ModItems.ELIXIR;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getIconItemStack() {
			return ModItems.ELIXIR.getDefaultInstance();
		}
	};

	@Mod.Instance(MODID)
	public static Rustic instance;

	public static final Logger logger = LogManager.getLogger(MODID);

	public static final UUID ERIS_UUID = UUID.fromString("8167f7a5-2db0-42ca-b177-ebc1396dbe55");

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		EventHandlerCommon common = new EventHandlerCommon();
		MinecraftForge.EVENT_BUS.register(common);
		FMLCommonHandler.instance().bus().register(common);
		MinecraftForge.EVENT_BUS.register(new EventHandlerPotions());

		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		Recipes.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
		Recipes.postInit();

		if (Loader.isModLoaded("Forestry") && Config.ENABLE_FORESTRY_COMPAT) {
			Compat.doForestryCompat();
		}

		// Fix for Rustic Thaumaturgy adding recipes directly in the data structure
		Recipes.injectEvaporatingRecipes();
	}

}
