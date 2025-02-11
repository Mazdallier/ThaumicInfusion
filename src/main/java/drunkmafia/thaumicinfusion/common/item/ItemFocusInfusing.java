package drunkmafia.thaumicinfusion.common.item;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.tiles.TileJarFillable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean on 04/04/2015.
 */
public class ItemFocusInfusing extends ItemFocusBasic {

    public IIcon iconOrnament;
    IIcon depthIcon = null;

    public ItemFocusInfusing(){
        this.setCreativeTab(ThaumicInfusion.instance.tab);
    }

    public String getSortingHelper(ItemStack itemstack) {
        return "BWI" + super.getSortingHelper(itemstack);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.depthIcon = ir.registerIcon(ModInfo.MODID + ":focus_infusion_depth");
        this.icon = ir.registerIcon(ModInfo.MODID + ":focus_infusion");
        this.iconOrnament = ir.registerIcon(ModInfo.MODID + ":focus_infusion_orn");
    }

    public IIcon getFocusDepthLayerIcon(ItemStack itemstack) {
        return this.depthIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int par1, int renderPass) {
        return renderPass == 1?this.icon:this.iconOrnament;
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    public IIcon getOrnament(ItemStack itemstack) {
        return this.iconOrnament;
    }

    public int getFocusColor(ItemStack itemstack) {
        NBTTagCompound wandNBT = itemstack.getTagCompound();
        if(wandNBT != null)
            return Aspect.getAspect(wandNBT.getString("InfusionAspect")).getColor();
        return 16771535;
    }

    @Override
    public void addFocusInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
        NBTTagCompound wandNBT = itemstack.getTagCompound();
        if (wandNBT != null) {
            Aspect aspect = Aspect.getAspect(wandNBT.getString("InfusionAspect"));
            if (aspect != null)
                list.add("Infusing Aspect: " + aspect.getName());
        }
        super.addFocusInformation(itemstack, player, list, par4);
    }

    public AspectList getVisCost(ItemStack itemstack) {
        return new AspectList();
    }

    public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer player, MovingObjectPosition mop) {
        player.swingItem();

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            NBTTagCompound wandNBT = itemstack.getTagCompound() != null ? itemstack.getTagCompound() : new NBTTagCompound();
            TileEntity tile = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
            if (tile != null && tile instanceof TileJarFillable) {
                Aspect aspect = ((TileJarFillable) tile).getAspects().getAspects()[0];
                if (aspect != null)
                    wandNBT.setString("InfusionAspect", aspect.getTag());
            } else if (wandNBT.hasKey("InfusionAspect") && !world.isRemote) {
                Aspect aspect = Aspect.getAspect(wandNBT.getString("InfusionAspect"));
                if (aspect != null && Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), aspect)) {
                    placeAspect(player, new WorldCoord(mop.blockX, mop.blockY, mop.blockZ), aspect);
                    world.playSoundEffect((double) mop.blockX + 0.5D, (double) mop.blockY + 0.5D, (double) mop.blockZ + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
                }
            }

            itemstack.setTagCompound(wandNBT);
        }
        return itemstack;
    }

    public void placeAspect(EntityPlayer player, WorldCoord pos, Aspect aspect){
        if(aspect != null) {
            World world = player.worldObj;
            TIWorldData worldData = TIWorldData.getWorldData(world);
            WorldCoord coords = new WorldCoord(pos.x, pos.y, pos.z);
            if (player.isSneaking()) {
                BlockData data = worldData.getBlock(BlockData.class, pos);
                if (data != null) {
                    AspectList list = new AspectList();
                    for (Aspect currentAspect : data.getAspects())
                        list.add(currentAspect, AspectHandler.getCostOfEffect(aspect));
                    refillJars(player, list);

                    worldData.removeData(BlockData.class, pos, true);
                }
            } else {
                BlockData data = worldData.getBlock(BlockData.class, coords);
                if (data == null) {
                    Class c = AspectHandler.getEffectFromAspect(aspect);
                    if(c == null)
                        return;
                    if (drainAspects(player, aspect))
                        worldData.addBlock(new BlockData(coords, new Class[]{c}), true, true);
                }else{
                    for(Aspect dataAspect : data.getAspects()){
                        if(dataAspect == aspect){
                            ArrayList<Class> newAspects = new ArrayList<Class>();
                            for(Aspect dataAspect2 : data.getAspects()){
                                if(dataAspect2 != aspect)
                                    newAspects.add(AspectHandler.getEffectFromAspect(dataAspect2));
                            }

                            if(newAspects.size() == 0)
                                worldData.removeData(BlockData.class, pos, true);
                            else if(drainAspects(player, aspect)){
                                worldData.removeData(BlockData.class, pos, true);
                                worldData.addBlock(new BlockData(coords, newAspects.toArray(new Class[newAspects.size()])), true, true);
                            }
                            return;
                        }
                    }
                    if(drainAspects(player, aspect)) {
                        ArrayList<Class> newAspects = new ArrayList<Class>();
                        newAspects.add(AspectHandler.getEffectFromAspect(aspect));
                        for (Aspect dataAspect : data.getAspects())
                            newAspects.add(AspectHandler.getEffectFromAspect(dataAspect));

                        worldData.removeData(BlockData.class, pos, true);
                        worldData.addBlock(new BlockData(coords, newAspects.toArray(new Class[newAspects.size()])), true, true);
                    }
                }
                PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(pos.x, pos.y, pos.z, 16556032), new NetworkRegistry.TargetPoint(player.worldObj.provider.dimensionId, (double) pos.x, (double) pos.y, (double) pos.z, 32.0D));
            }
        }
    }

    public boolean drainAspects(EntityPlayer player, Aspect aspect){
        if(player.capabilities.isCreativeMode)
            return true;

        int cost = AspectHandler.getCostOfEffect(aspect);
        for(int x = (int) (player.posX - 10); x < player.posX + 10; x++){
            for(int y = (int) (player.posY - 10); y < player.posY + 10; y++){
                for(int z = (int) (player.posZ - 10); z < player.posZ + 10; z++){
                    TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
                    if(tileEntity instanceof IAspectSource){
                        IAspectSource source = (IAspectSource) tileEntity;
                        if (source.doesContainerContainAmount(aspect, cost)) {
                            source.takeFromContainer(aspect, cost);
                            player.worldObj.playSound((double) ((float) tileEntity.xCoord + 0.5F), (double) ((float) tileEntity.yCoord + 0.5F), (double) ((float) tileEntity.zCoord + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.3F, false);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean refillJars(EntityPlayer player, AspectList aspectList) {
        if (player.capabilities.isCreativeMode)
            return true;

        int filled = 0;
        for (int i = 0; i < aspectList.size(); i++) {
            boolean foundJar = false;
            Aspect currentAspect = aspectList.getAspects()[i];
            for (int x = (int) (player.posX - 10); x < player.posX + 10; x++) {
                for (int y = (int) (player.posY - 10); y < player.posY + 10; y++) {
                    for (int z = (int) (player.posZ - 10); z < player.posZ + 10; z++) {
                        TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
                        if (tileEntity instanceof IAspectSource) {
                            IAspectSource source = (IAspectSource) tileEntity;
                            if (source.doesContainerAccept(currentAspect)) {
                                source.addToContainer(currentAspect, AspectHandler.getCostOfEffect(currentAspect));
                                filled++;
                                foundJar = true;
                                player.worldObj.playSound((double) ((float) tileEntity.xCoord + 0.5F), (double) ((float) tileEntity.yCoord + 0.5F), (double) ((float) tileEntity.zCoord + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.3F, false);
                                break;
                            }
                        }
                    }
                    if (foundJar) break;
                }
                if (foundJar) break;
            }
        }

        return filled == aspectList.size();
    }
}
