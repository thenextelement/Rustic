package rustic.common.book.pages;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rustic.client.gui.book.GuiBook;
import rustic.common.book.BookCategory;
import rustic.common.book.BookEntryCategory;
import rustic.common.book.BookManager;
import rustic.common.book.BookPage;

public class BookPageCategories extends BookPage {
	
	public BookPageCategories() {
		super(BookManager.categories_entry);
	}

	@Override
	public void drawScreen(GuiBook gui, int mouseX, int mouseY, float partialTicks) {
		int y = gui.guiTop + 12;
		int x = gui.guiLeft + (GuiBook.WIDTH / 2);
		
		gui.drawCenteredText(EnumChatFormatting.UNDERLINE + "" + EnumChatFormatting.BOLD + StatCollector.translateToLocal(BookManager.categories_entry.getName()), x, y, 0x000000);
	}
	
	@Override
	public void onOpened(GuiBook gui) {
		int y = gui.guiTop + 28;
		int x = gui.guiLeft + (GuiBook.WIDTH / 2) - 16;
		
		for (BookCategory category : BookManager.categories) {
			gui.getButtonList().add(new CategoryButton(gui.nextButtonID(), x, y, 32, gui.getFontRenderer(), category));
			y += 48;
		}
	}
	
	@Override
	public void onClosed(GuiBook gui) {
		super.onClosed(gui);
	}
	
	@Override
	public void actionPerformed(GuiBook gui, GuiButton button) {
		if (button instanceof CategoryButton) {
			gui.goToEntry(((CategoryButton) button).getCategoryEntry());
		}
	}
	
	@SideOnly(Side.CLIENT)
	public class CategoryButton extends GuiButton {

		private int ticksHovered = 0;
		private int frame = 0;
		
		private FontRenderer fontRenderer;
		private BookCategory category;

		public CategoryButton(int buttonId, int x, int y, int width, FontRenderer fontRenderer, BookCategory category) {
			super(buttonId, x, y, width, width, category.getName());
			this.fontRenderer = fontRenderer;
			this.category = category;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			int x = this.xPosition, y = this.yPosition;
			FontRenderer fontRenderer = mc.fontRenderer;
			if (this.visible) {
				boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition
						&& mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

				GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            if (flag) {
	            	ticksHovered++;
	            	if (ticksHovered % 2 == 0 && frame < 7) {
	            		frame++;
	            	}
	            	String text = StatCollector.translateToLocal(this.displayString);
	            	int stringWidth = fontRenderer.getStringWidth(text);
	            	fontRenderer.drawString(text, xPosition + (width / 2) - (stringWidth / 2), yPosition + height, 0x000000);
	            	GL11.glColor4f(44 / 256F, 114 / 256F, 44 / 256F, 1F);
	            } else {
	            	ticksHovered = 0;
	            	if (frame > 0) {
	            		frame--;
	            	}
	            	GL11.glColor4f(0F, 0F, 0F, 1F);
	            }
	            
				mc.getTextureManager().bindTexture(category.getIcon());
				Gui.func_146110_a(xPosition, yPosition, 0, frame * 32F, 32, 32, 32F, 256F);
				
				GL11.glColor4f(1F, 1F, 1F, 1F);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
		
		public BookEntryCategory getCategoryEntry() {
			return this.category.getCategoryEntry();
		}
		
	}

}
