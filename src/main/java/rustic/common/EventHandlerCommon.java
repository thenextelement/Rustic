package rustic.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import rustic.common.blocks.ModBlocks;
import rustic.common.crafting.RecipeOliveOil;
import rustic.common.crafting.RecipeVantaOil;
import rustic.common.items.ItemFluidBottle;
import rustic.common.items.ModItems;
import rustic.common.util.ElixirUtils;
import rustic.common.util.RusticUtils;
import rustic.core.Rustic;

public class EventHandlerCommon {

    private final Random rand = new Random();
    private final Random particleRng = new Random();

    private static IRecipe findRecipe(InventoryCrafting inv, World world) {
        for (Object o : CraftingManager.getInstance()
            .getRecipeList()) {
            IRecipe recipe = (IRecipe) o;
            if ((recipe instanceof RecipeOliveOil || recipe instanceof RecipeVantaOil) && recipe.matches(inv, world)) {
                return recipe;
            }
        }
        return null;
    }

    /** Oiling food/weapons gives the empty bottles back. */
    @SubscribeEvent
    public void onOilBottleCraftingEvent(ItemCraftedEvent event) {
        if (event.player == null || event.crafting == null || !(event.craftMatrix instanceof InventoryCrafting)) return;
        InventoryCrafting inv = (InventoryCrafting) event.craftMatrix;
        IRecipe recipe = findRecipe(inv, event.player.worldObj);
        if (recipe instanceof RecipeOliveOil) {
            RusticUtils.givePlayerItem(event.player, new ItemStack(Items.glass_bottle));
        } else if (recipe instanceof RecipeVantaOil) {
            int bottles = RecipeVantaOil.getReturnBottleCount(inv);
            if (bottles > 0) {
                RusticUtils.givePlayerItem(event.player, new ItemStack(Items.glass_bottle, bottles));
            }
        }
    }

    @SubscribeEvent
    public void onVantaWeaponAttackEvent(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) return;
        if (!(event.target instanceof EntityLivingBase)) return;
        EntityLivingBase targetEntity = (EntityLivingBase) event.target;
        if (!targetEntity.canAttackWithItem() || targetEntity.hitByEntity(player)) return;

        NBTTagCompound vantaOilTag = ElixirUtils.getVantaOilTag(stack);
        if (vantaOilTag == null) return;
        PotionEffect vantaEffect = ElixirUtils.getVantaOilEffect(vantaOilTag);
        if (vantaEffect == null) {
            ElixirUtils.setVantaOilTag(stack, null);
            return;
        }
        int totalDuration = vantaEffect.getDuration();
        int amplifier = vantaEffect.getAmplifier();
        Potion potion = Potion.potionTypes[vantaEffect.getPotionID()];

        boolean isInstant = potion.isInstant();
        int duration = isInstant ? 1 : ElixirUtils.getNextVantaHitDuration(totalDuration);
        int effectDur = duration;
        PotionEffect activeEffect = targetEntity.getActivePotionEffect(potion);
        if (activeEffect != null) {
            int activeDur = activeEffect.getDuration();
            if (activeDur <= 0) {
                targetEntity.removePotionEffect(potion.id);
            } else if (activeEffect.getAmplifier() == amplifier) {
                effectDur += activeDur;
            } else if (activeEffect.getAmplifier() > amplifier) {
                effectDur = 0;
            }
        }

        if (effectDur > 0) {
            if (!player.worldObj.isRemote) {
                if (isInstant) {
                    potion.affectEntity(player, targetEntity, amplifier, 1.0);
                } else {
                    targetEntity.addPotionEffect(new PotionEffect(potion.id, effectDur, amplifier));
                }
            } else {
                int color = potion.getLiquidColor();
                double d0 = (color >> 16 & 255) / 255.0D;
                double d1 = (color >> 8 & 255) / 255.0D;
                double d2 = (color & 255) / 255.0D;
                for (int i = 0; i < 20; i++) {
                    player.worldObj.spawnParticle(
                        "mobSpell",
                        targetEntity.posX + ((particleRng.nextDouble() - 0.5) * targetEntity.width),
                        targetEntity.posY - targetEntity.yOffset + (particleRng.nextDouble() * targetEntity.height),
                        targetEntity.posZ + ((particleRng.nextDouble() - 0.5) * targetEntity.width),
                        d0,
                        d1,
                        d2);
                }
            }
        }

        totalDuration -= duration;
        if (totalDuration <= 0) {
            vantaOilTag = null;
        } else {
            vantaOilTag = (NBTTagCompound) vantaOilTag.copy();
            vantaOilTag.setInteger("Duration", totalDuration);
        }
        if (stack.stackSize > 1) {
            ItemStack remainingStack = stack.splitStack(stack.stackSize - 1);
            ElixirUtils.setVantaOilTag(stack, vantaOilTag);
            RusticUtils.givePlayerItem(player, remainingStack);
        } else {
            ElixirUtils.setVantaOilTag(stack, vantaOilTag);
        }
    }

    @SubscribeEvent
    public void onNameFormatEvent(PlayerEvent.NameFormat event) {
        if (event.entityPlayer != null && event.entityPlayer.getUniqueID()
            .equals(Rustic.ERIS_UUID)) {
            event.displayname = EnumChatFormatting.DARK_RED + "Mommy Eris" + EnumChatFormatting.RESET;
        }
    }

    @SubscribeEvent
    public void onVineDropEvent(BlockEvent.HarvestDropsEvent event) {
        if (event.block != Blocks.vine) return;
        boolean tryDrop = false;
        if (Config.ENABLE_SEED_DROPS) {
            if (!Config.GRAPE_DROP_NEEDS_TOOL) {
                tryDrop = true;
            } else if (event.harvester != null && event.harvester.getCurrentEquippedItem() != null) {
                String itemName = RusticUtils.getRegistryName(
                    event.harvester.getCurrentEquippedItem()
                        .getItem());
                if (Config.GRAPE_TOOL_WHITELIST.contains(itemName)) tryDrop = true;
            }
        }
        if (tryDrop && rand.nextInt(10) == 0) {
            event.drops.add(new ItemStack(ModBlocks.GRAPE_STEM));
        }
    }

    /** Olive-oiled soups/food are more filling. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseTick(PlayerUseItemEvent.Tick event) {
        ItemStack stack = event.item;
        if (stack != null && stack.getItem() instanceof ItemSoup
            && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("oiled")
            && event.duration == 1) {
            event.entityPlayer.getFoodStats()
                .addStats(2, 0.3F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseFinish(PlayerUseItemEvent.Finish event) {
        ItemStack stack = event.item;
        if (stack != null && stack.getItem() instanceof ItemFood
            && !(stack.getItem() instanceof ItemSoup)
            && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("oiled")) {
            event.entityPlayer.getFoodStats()
                .addStats(2, 0.3F);
        }
    }

    /** Glass bottles can be filled from Rustic fluid blocks and from tanks. */
    @SubscribeEvent
    public void onPlayerUseGlassBottle(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != Items.glass_bottle) return;
        World world = player.worldObj;

        MovingObjectPosition hit = RusticUtils.rayTrace(world, player, true);
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = world.getBlock(hit.blockX, hit.blockY, hit.blockZ);
            if (block instanceof IFluidBlock
                && player.canPlayerEdit(hit.blockX, hit.blockY, hit.blockZ, hit.sideHit, stack)) {
                IFluidBlock fluidBlock = (IFluidBlock) block;
                if (ItemFluidBottle.VALID_FLUIDS.contains(fluidBlock.getFluid())
                    && fluidBlock.getFilledPercentage(world, hit.blockX, hit.blockY, hit.blockZ) == 1) {
                    if (!world.isRemote) {
                        world.setBlockToAir(hit.blockX, hit.blockY, hit.blockZ);
                        world.playSoundAtEntity(player, "game.neutral.swim.splash", 0.5F, 1.0F);
                        consumeOne(player, stack);
                        RusticUtils
                            .givePlayerItem(player, ModItems.FLUID_BOTTLE.getFilledBottle(fluidBlock.getFluid()));
                    }
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        TileEntity tile = world.getTileEntity(event.x, event.y, event.z);
        if (!(tile instanceof IFluidHandler)) return;
        ForgeDirection face = ForgeDirection.getOrientation(event.face);
        if (world.getBlock(event.x, event.y, event.z) == ModBlocks.BREWING_BARREL && face != ForgeDirection.DOWN)
            return;
        IFluidHandler tank = (IFluidHandler) tile;
        FluidStack available = tank.drain(face, 1000, false);
        if (available == null || available.getFluid() == null || available.amount < 1000) return;
        if (ItemFluidBottle.VALID_FLUIDS.contains(available.getFluid())) {
            if (!world.isRemote) {
                FluidStack fill = tank.drain(face, 1000, true);
                world.playSoundAtEntity(player, "game.neutral.swim.splash", 0.5F, 1.0F);
                consumeOne(player, stack);
                ItemStack bottle = new ItemStack(ModItems.FLUID_BOTTLE);
                ModItems.FLUID_BOTTLE.fill(bottle, fill, true);
                RusticUtils.givePlayerItem(player, bottle);
            }
            event.setCanceled(true);
        } else if (available.getFluid() == FluidRegistry.WATER) {
            if (!world.isRemote) {
                tank.drain(face, 1000, true);
                world.playSoundAtEntity(player, "game.neutral.swim.splash", 0.5F, 1.0F);
                consumeOne(player, stack);
                RusticUtils.givePlayerItem(player, new ItemStack(Items.potionitem, 1, 0));
            }
            event.useBlock = Result.DENY;
            event.setCanceled(true);
        }
    }

    private static void consumeOne(EntityPlayer player, ItemStack stack) {
        if (player.capabilities.isCreativeMode) return;
        stack.stackSize -= 1;
        if (stack.stackSize <= 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
    }

    /** Chickens can be bred with Rustic's seeds. */
    @SubscribeEvent
    public void onSeedInteractEvent(EntityInteractEvent event) {
        if (!(event.target instanceof EntityChicken)) return;
        ItemStack equippedItem = event.entityPlayer.getCurrentEquippedItem();
        EntityAnimal targetAnimal = (EntityAnimal) event.target;
        if (equippedItem != null
            && (equippedItem.getItem() == ModItems.TOMATO_SEEDS
                || equippedItem.getItem() == ModItems.CHILI_PEPPER_SEEDS)
            && targetAnimal.getGrowingAge() >= 0
            && !targetAnimal.isInLove()) {
            if (!event.entityPlayer.capabilities.isCreativeMode) {
                equippedItem.stackSize -= 1;
            }
            targetAnimal.func_146082_f(event.entityPlayer);
        }
    }

}
