package rustic.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import rustic.common.blocks.ModBlocks;

public class MessageDismountChair implements IMessage {

	public MessageDismountChair() {}
	
	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}
	
	public static class MessageHolder implements IMessageHandler<MessageDismountChair, IMessage> {
		
		@Override
		public IMessage onMessage(final MessageDismountChair message, final MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
							if (player.ridingEntity != null) player.mountEntity(null);
			return null;
		}
		
	}

}
