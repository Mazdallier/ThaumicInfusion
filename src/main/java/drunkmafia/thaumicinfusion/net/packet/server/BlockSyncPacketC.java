package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 28/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class BlockSyncPacketC implements IMessage {

    private BlockSavable data;

    public BlockSyncPacketC() {
    }

    public BlockSyncPacketC(BlockSavable data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            NBTTagCompound tag = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
            if (tag != null)
                data = SavableHelper.loadDataFromNBT(tag);
        } catch (Exception e) {}
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (data != null)
                new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(data));
            new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(data));

        } catch (Exception e) {}
    }

    public static class Handler implements IMessageHandler<BlockSyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(BlockSyncPacketC message, MessageContext ctx) {
            BlockSavable data = message.data;
            if (data == null || ctx.side.isServer())
                return null;

            World world = ChannelHandler.getClientWorld();
            WorldCoord pos = data.getCoords();
            TIWorldData worldData = TIWorldData.getWorldData(world);

            worldData.removeData(message.data.getClass(), pos, false);
            worldData.addBlock(message.data, true, false);
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);
            return null;
        }
    }
}
