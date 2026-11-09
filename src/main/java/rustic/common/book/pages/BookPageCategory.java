package rustic.common.book.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.gui.book.GuiBook;
import rustic.common.book.BookCategory;
import rustic.common.book.BookEntry;
import rustic.common.book.BookEntryCategory;
import rustic.common.book.BookPage;

public class BookPageCategory extends BookPage {

	private static final net.minecraft.client.renderer.entity.RenderItem RENDER_ITEM = new net.minecraft.client.renderer.entity.RenderItem();
	
	private final BookCategory category;
	
	protected List<BookEntry> entries = new ArrayList<BookEntry>();
	
	public BookPageCategory(BookCategory category, BookEntryCategory entry) {
		super(entry);
		this.category = category;
	}

	@Override
	public void drawScreen(GuiBook gui, int mouseX, int mouseY, float partialTicks) {
		int y = gui.guiTop + 12;
		int x = gui.guiLeft + (GuiBook.WIDTH / 2);
		
		gui.drawCenteredText(EnumChatFormatting.UNDERLINE + "" + EnumChatFormatting.BOLD + StatCollector.translateToLocal(category.getName()), x, y, 0x000000);
	}
	
	@Override
	public void onOpened(GuiBook gui) {
		gui.addNavButtons();
		
		int x = gui.guiLeft + 14;
		int y = gui.guiTop + 24;
		for (BookEntry entry : this.entries) {
			gui.getButtonList().add(new EntryLinkButton(gui.nextButtonID(), x, y, GuiBook.WIDTH - 16 - 14, gui.getFontRenderer(), entry));
			y += (gui.getFontRenderer().FONT_HEIGHT + 2);
		}
	}
	
	@Override
	public void onClosed(GuiBook gui) {
		super.onClosed(gui);
	}
	
	@Override
	public void actionPerformed(GuiBook gui, GuiButton button) {
		if (button instanceof EntryLinkButton) {
			gui.goToEntry(((EntryLinkButton) button).getEntry());
		}
	}
	
	public BookPageCategory addEntry(BookEntry entry) {
		this.entries.add(entry);
		return this;
	}

	@SideOnly(Side.CLIENT)
	public class EntryLinkButton extends GuiButton {

		private int ticksHovered = 0;
		
		private FontRenderer fontRenderer;
		private final BookEntry entry;
		
		public EntryLinkButton(int buttonId, int x, int y, int width, FontRenderer fontRenderer, BookEntry entry) {
			super(buttonId, x, y, width, fontRenderer.FONT_HEIGHT + 2, entry.getName());
			this.fontRenderer = fontRenderer;
			this.entry = entry;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			int x = this.xPosition, y = this.yPosition;
			FontRenderer fontRenderer = mc.fontRenderer;
			if (this.visible) {
				boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition
						&& mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				if (flag) {
					if (this.ticksHovered < width) {
						this.ticksHovered++;
					}
					int rectWidth = Math.min(ticksHovered * 10, width);
					Gui.drawRect(x, y, x + rectWidth, y + height, 0x40000000);
				} else {
					this.ticksHovered = 0;
				}
				fontRenderer.drawString(StatCollector.translateToLocal(this.displayString), x + fontRenderer.FONT_HEIGHT + 2 + 1, y + 2, 0x000000);
				if (this.entry != null && this.entry.getIcon() != null) {
					float scale = (fontRenderer.FONT_HEIGHT + 2) / 16F;
					
					GL11.glPushMatrix();
					GL11.glScalef(scale, scale, scale);
					GL11.glColor4f(1F, 1F, 1F, 1F);
					RenderHelper.enableGUIStandardItemLighting();
					
					RENDER_ITEM.renderItemIntoGUI(fontRenderer, mc.getTextureManager(), this.entry.getIcon(), (int) ((x) / scale), (int) (y / scale));
					
					RenderHelper.disableStandardItemLighting();
					GL11.glPopMatrix();
				}
			}
		}
		
		public BookEntry getEntry() {
			return this.entry;
		}
		
	}
	
}
