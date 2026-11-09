package rustic.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import rustic.common.tileentity.ContainerCabinet;
import rustic.common.tileentity.TileEntityCabinet;
import rustic.core.Rustic;

public class GuiCabinet extends GuiContainer {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;

    private static final ResourceLocation background = new ResourceLocation(
        Rustic.MODID,
        "textures/gui/generic_27.png");

    private TileEntityCabinet te;
    private InventoryPlayer playerInv;

    public GuiCabinet(ContainerCabinet container, InventoryPlayer playerInv) {
        super(container);

        this.playerInv = playerInv;
        this.te = container.getTile();
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager()
            .bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(StatCollector.translateToLocal(this.te.getInventoryName()), 8, 6, 4210752);
        this.fontRendererObj
            .drawString(StatCollector.translateToLocal(playerInv.getInventoryName()), 8, this.ySize - 96 + 2, 4210752);
    }

}
