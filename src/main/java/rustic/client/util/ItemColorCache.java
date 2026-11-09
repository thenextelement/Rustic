package rustic.client.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.ItemStack;

public class ItemColorCache implements IResourceManagerReloadListener {

    public static final ItemColorCache INSTANCE = new ItemColorCache();

    protected final Map<String, Integer> itemColors = new HashMap<>();

    public int getColor(ItemStack stack) {
        String itemKey = stack.getItem()
            .getUnlocalizedName() + "@"
            + stack.getItemDamage();
        Integer color = itemColors.get(itemKey);
        if (color == null) {
            color = ClientUtils.getItemColor(stack);
            itemColors.put(itemKey, color);
        }
        return color;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        itemColors.clear();
        ClientUtils.clearColorCaches();
    }

}
