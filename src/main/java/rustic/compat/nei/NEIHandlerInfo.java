package rustic.compat.nei;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.event.FMLInterModComms;
import rustic.core.Rustic;

/** Sends NEI the tab icon and size of Rustic's handlers over IMC. Has no NEI dependency; harmless without NEI. */
public final class NEIHandlerInfo {

    private NEIHandlerInfo() {}

    public static void send() {
        sendInfo("rustic.crushing_tub", "rustic:crushing_tub", 68);
        sendInfo("rustic.evaporating", "rustic:evaporating_basin", 68);
        sendInfo("rustic.brewing", "rustic:brewing_barrel", 84);
        sendInfo("rustic.alchemy_simple", "rustic:condenser", 83);
        sendInfo("rustic.alchemy_advanced", "rustic:condenser_advanced", 83);
    }

    private static void sendInfo(String handler, String item, int height) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("handler", handler);
        tag.setString("modName", Rustic.NAME);
        tag.setString("modId", Rustic.MODID);
        tag.setBoolean("modRequired", true);
        tag.setString("itemName", item);
        tag.setInteger("handlerHeight", height);
        tag.setInteger("handlerWidth", 166);
        tag.setInteger("maxRecipesPerPage", 2);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", tag);
    }

}
