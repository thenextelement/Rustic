package rustic.common.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import rustic.common.crafting.ICondenserRecipe;
import rustic.common.crafting.Recipes;
import rustic.common.util.ElixirUtils;
import rustic.common.util.RusticUtils;
import rustic.core.Rustic;

public class ItemElixir extends ItemBase {

	@SideOnly(Side.CLIENT)
	private IIcon overlay;

	public ItemElixir() {
		super("elixir");
		setCreativeTab(Rustic.alchemyTab);
		setMaxStackSize(16);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getSubItems(Item item, CreativeTabs tab, List subItems) {
		for (ICondenserRecipe recipe : Recipes.condenserRecipes) {
			ItemStack result = recipe.getResult();
			boolean dupe = false;
			for (Object o : subItems) {
				if (ItemStack.areItemStacksEqual((ItemStack) o, result)) dupe = true;
			}
			if (!dupe) subItems.add(result);
		}
	}

	/** An elixir with instant health, used as the creative tab icon. */
	public ItemStack getDefaultInstance() {
		ItemStack stack = new ItemStack(this);
		ElixirUtils.addEffect(new PotionEffect(Potion.heal.id, 1, 0), stack);
		return stack;
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			for (PotionEffect effect : ElixirUtils.getEffects(stack)) {
				Potion potion = Potion.potionTypes[effect.getPotionID()];
				if (potion == null) continue;
				if (potion.isInstant()) {
					potion.affectEntity(player, player, effect.getAmplifier(), 1.0D);
				} else {
					player.addPotionEffect(new PotionEffect(effect));
				}
			}
		}
		if (!player.capabilities.isCreativeMode) {
			stack.stackSize -= 1;
			if (stack.stackSize <= 0) {
				return new ItemStack(Items.glass_bottle);
			}
			RusticUtils.givePlayerItem(player, new ItemStack(Items.glass_bottle));
		}
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 24;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.drink;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, getMaxItemUseDuration(stack));
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = StatCollector.translateToLocal("rustic.elixir.prefix");
		List<PotionEffect> effects = ElixirUtils.getEffects(stack);
		for (int i = 0; i < effects.size(); i++) {
			name += StatCollector.translateToLocal(effects.get(i).getEffectName());
			if (i < effects.size() - 1) {
				name += StatCollector.translateToLocal("rustic.elixir.separator");
			}
		}
		return name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
		ElixirUtils.addPotionTooltip(stack, tooltip, 1.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		overlay = register.registerIcon(Rustic.MODID + ":elixir_overlay");
		itemIcon = register.registerIcon(Rustic.MODID + ":elixir_bottle");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
		return pass == 0 ? overlay : itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return pass == 0 ? ElixirUtils.getColor(stack) : 0xFFFFFF;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass) {
		return pass == 0 && !ElixirUtils.getEffects(stack).isEmpty();
	}

}
