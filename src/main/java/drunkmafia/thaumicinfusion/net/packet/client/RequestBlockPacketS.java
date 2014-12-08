package drunkmafia.thaumicinfusion.net.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.util.BlockHelper;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.CooldownPacket;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

import java.util.HashMap;

/**
 * Created by DrunkMafia on 28/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class RequestBlockPacketS extends CooldownPacket {

    public RequestBlockPacketS() {}

    private ChunkCoordinates coordinates;
    private Class<? extends BlockSavable> type;

    public RequestBlockPacketS(Class<? extends BlockSavable> type, ChunkCoordinates coordinates) {
        if(!canSend(coordinates))
            return;
        this.coordinates = coordinates;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if(buf.readByte() == 1){
            coordinates = new ChunkCoordinates(buf.readInt(), buf.readInt(), buf.readInt());
            try {
                byte[] typeBytes = new byte[buf.readInt()];
                buf.readBytes(typeBytes);
                type = (Class<? extends BlockSavable>) Class.forName(new String(typeBytes));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if(coordinates != null) {
            buf.writeByte(1);
            buf.writeInt(coordinates.posX);
            buf.writeInt(coordinates.posY);
            buf.writeInt(coordinates.posZ);
            byte[] typeBytes = type.getCanonicalName().getBytes();
            buf.writeInt(typeBytes.length);
            buf.writeBytes(typeBytes);
        }else buf.writeByte(0);
    }

    public static class Handler implements IMessageHandler<RequestBlockPacketS, IMessage> {
        @Override
        public IMessage onMessage(RequestBlockPacketS message, MessageContext ctx) {
            if (message.coordinates == null || ctx.side.isClient()) return null;
            BlockSavable data = BlockHelper.getData(message.type, ChannelHandler.getPlayer(ctx).worldObj, message.coordinates);
            if (data != null)
                return new BlockSyncPacketC(data);
            return null;
        }
    }
}
