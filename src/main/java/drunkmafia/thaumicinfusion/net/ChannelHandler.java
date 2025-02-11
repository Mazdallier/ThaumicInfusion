package drunkmafia.thaumicinfusion.net;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.net.packet.server.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ChannelHandler{

    public static SimpleNetworkWrapper network;

    static int ordinal = 0;

    public static void init() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.CHANNEL);

        Side S = Side.SERVER, C = Side.CLIENT;

        //Server Handled Packets


        //Client Handled Packets
        network.registerMessage(BlockSyncPacketC.Handler.class, BlockSyncPacketC.class, getOrdinal(), C);
        network.registerMessage(TileSyncPacketC.Handler.class, TileSyncPacketC.class, getOrdinal(), C);
        network.registerMessage(EffectSyncPacketC.Handler.class, EffectSyncPacketC.class, getOrdinal(), C);
        network.registerMessage(PlaySoundPacketC.Handler.class, PlaySoundPacketC.class, getOrdinal(), C);
        network.registerMessage(EntitySyncPacketC.Handler.class, EntitySyncPacketC.class, getOrdinal(), C);
        network.registerMessage(DataRemovePacketC.Handler.class, DataRemovePacketC.class, getOrdinal(), C);
    }

    static int getOrdinal(){
        return ordinal++;
    }

    @SideOnly(Side.CLIENT)
    public static World getClientWorld(){
        return FMLClientHandler.instance().getClient().theWorld;
    }

    public static WorldServer getServerWorld(int dim){
        return DimensionManager.getWorld(dim);
    }
}
