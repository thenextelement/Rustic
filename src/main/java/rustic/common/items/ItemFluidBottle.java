package rustic.common.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.util.ClientUtils;
import rustic.common.blocks.fluids.FluidBooze;
import rustic.common.blocks.fluids.FluidDrinkable;
import rustic.common.blocks.fluids.ModFluids;
import rustic.common.util.RusticUtils;
import rustic.core.Rustic;

/**
 * A glass bottle holding 1000mb of one of Rustic's fluids, stored in NBT (so booze quality is kept). Implements
 * {@link IFluidContainerItem} so tanks from other mods can fill and empty it.
 */
public class ItemFluidBottle extends Item implements IFluidContainerItem {

    protected static final CreativeTabs[] creative_tabs = { Rustic.farmingTab, Rustic.alchemyTab, };
    public static List<Fluid> VALID_FLUIDS = new ArrayList<>();
    public static final String FLUID_NBT_KEY = "Fluid";
    public static final int CAPACITY = 1000;

    public static void addFluid(Fluid fluid) {
        if (fluid != null && !VALID_FLUIDS.contains(fluid)) VALID_FLUIDS.add(fluid);
    }

    @SideOnly(Side.CLIENT)
    private IIcon potionOverlay, potionBottle, boozeOverlay, boozeBottle, boozeLabel;

    public ItemFluidBottle() {
        super();
        setUnlocalizedName(Rustic.MODID + ".fluid_bottle");
        setCreativeTab(Rustic.farmingTab);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, "fluid_bottle");
    }

    public ItemStack getFilledBottle(Fluid fluid) {
        ItemStack bottle = new ItemStack(this);
        if (VALID_FLUIDS.contains(fluid) && FluidRegistry.getFluidName(fluid) != null) {
            setFluid(bottle, new FluidStack(fluid, CAPACITY));
        }
        return bottle;
    }

    private static void setFluid(ItemStack stack, FluidStack fluid) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        if (fluid == null) {
            tag.removeTag(FLUID_NBT_KEY);
        } else {
            FluidStack copy = fluid.copy();
            copy.amount = CAPACITY;
            tag.setTag(FLUID_NBT_KEY, copy.writeToNBT(new NBTTagCompound()));
        }
        stack.setTagCompound(tag);
    }

    /* IFluidContainerItem */

    @Override
    public FluidStack getFluid(ItemStack container) {
        if (container != null && container.hasTagCompound()
            && container.getTagCompound()
                .hasKey(FLUID_NBT_KEY)) {
            return FluidStack.loadFluidStackFromNBT(
                container.getTagCompound()
                    .getCompoundTag(FLUID_NBT_KEY));
        }
        return null;
    }

    @Override
    public int getCapacity(ItemStack container) {
        return CAPACITY;
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount < CAPACITY
            || getFluid(container) != null
            || !VALID_FLUIDS.contains(resource.getFluid())
            || container.stackSize != 1) {
            return 0;
        }
        if (doFill) setFluid(container, resource);
        return CAPACITY;
    }

    /**
     * Draining empties the whole bottle. The caller is expected to replace the (now fluid-less) stack with a glass
     * bottle.
     */
    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
        FluidStack fluid = getFluid(container);
        if (fluid == null || maxDrain < CAPACITY) return null;
        if (doDrain) setFluid(container, null);
        return fluid;
    }

    /* using */

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        FluidStack fluidstack = getFluid(stack);
        Fluid fluid = fluidstack != null ? fluidstack.getFluid() : null;
        if (!world.isRemote && fluid instanceof FluidDrinkable) {
            ((FluidDrinkable) fluid).onDrank(world, player, stack, fluidstack);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
            if (stack.stackSize <= 0) {
                return new ItemStack(Items.glass_bottle);
            }
            RusticUtils.givePlayerItem(player, new ItemStack(Items.glass_bottle));
        }
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (fluid != null && fluid.getFluid() instanceof FluidDrinkable) {
            return EnumAction.drink;
        }
        return EnumAction.none;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (getItemUseAction(stack) == EnumAction.drink) {
            player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }

    // No container item: recipes that use a bottle's contents give the glass bottle back themselves (see
    // EventHandlerCommon), and "bottle emptying" crafts the fluid bottle into a glass bottle.

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack != null && fluidStack.getFluid() != null) {
            return fluidStack.getFluid()
                .getRarity(fluidStack);
        }
        return super.getRarity(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack == null) {
            return new ItemStack(Items.glass_bottle).getDisplayName();
        }
        return StatCollector
            .translateToLocalFormatted(getUnlocalizedName(stack) + ".name", fluidStack.getLocalizedName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack != null && fluidStack.getFluid() instanceof FluidDrinkable) {
            tooltip.add(
                EnumChatFormatting.DARK_GRAY + ""
                    + EnumChatFormatting.ITALIC
                    + StatCollector.translateToLocal("tooltip.rustic.drinkable"));
        }
    }

    @Override
    public CreativeTabs[] getCreativeTabs() {
        return creative_tabs;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List subItems) {
        if (tab == Rustic.farmingTab) {
            for (Fluid fluid : VALID_FLUIDS) {
                ItemStack stack = getFilledBottle(fluid);
                if (fluid instanceof FluidBooze) {
                    FluidStack fs = getFluid(stack);
                    if (fs != null) {
                        if (fs.tag == null) fs.tag = new NBTTagCompound();
                        if (!fs.tag.hasKey(FluidBooze.QUALITY_NBT_KEY))
                            fs.tag.setFloat(FluidBooze.QUALITY_NBT_KEY, 0.75F);
                        setFluid(stack, fs);
                    }
                }
                subItems.add(stack);
            }
        } else if (tab == Rustic.alchemyTab) {
            subItems.add(getFilledBottle(ModFluids.VANTA_OIL));
        }
    }

    /* rendering: liquid overlay tinted with the fluid's colour, bottle, and (for booze) a quality label */

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        potionOverlay = register.registerIcon("potion_overlay");
        potionBottle = register.registerIcon("potion_bottle_drinkable");
        boozeOverlay = register.registerIcon(Rustic.MODID + ":alcohol_overlay");
        boozeBottle = register.registerIcon(Rustic.MODID + ":alcohol_bottle");
        boozeLabel = register.registerIcon(Rustic.MODID + ":alcohol_label");
        itemIcon = potionBottle;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        FluidStack fluid = getFluid(stack);
        boolean booze = fluid != null && fluid.getFluid() instanceof FluidBooze;
        switch (pass) {
            case 0:
                if (fluid == null) return booze ? boozeBottle : potionBottle;
                return booze ? boozeOverlay : potionOverlay;
            case 1:
                return booze ? boozeBottle : potionBottle;
            default:
                return booze ? boozeLabel : (booze ? boozeBottle : potionBottle);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        FluidStack fluid = getFluid(stack);
        if (fluid == null || fluid.getFluid() == null) return 0xFFFFFF;
        if (pass == 0) return ClientUtils.getFluidColor(fluid);
        if (pass == 2 && fluid.getFluid() instanceof FluidBooze) {
            float quality = 0.5F;
            if (fluid.tag != null && fluid.tag.hasKey(FluidBooze.QUALITY_NBT_KEY, 5)) {
                quality = fluid.tag.getFloat(FluidBooze.QUALITY_NBT_KEY);
            }
            return ClientUtils.getQualityLabelColor(quality);
        }
        return 0xFFFFFF;
    }

}
