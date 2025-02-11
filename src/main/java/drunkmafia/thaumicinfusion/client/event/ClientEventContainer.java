package drunkmafia.thaumicinfusion.client.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.util.RGB;
import drunkmafia.thaumicinfusion.common.util.helper.MathHelper;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.RenderEventHandler;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockCosmeticOpaque;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;

import java.util.HashMap;

/**
 * Created by DrunkMafia on 27/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@SideOnly(Side.CLIENT)
public class ClientEventContainer {

    static HashMap<WorldCoordinates, IIcon> iconCache = new HashMap();

    private BlockData currentdata, lastDataLookedAt;

//    public static long hasWorldDataTime;
//
//    @SubscribeEvent
//    public void onDrawDebugText(RenderGameOverlayEvent.Text event) {
//        World world = Minecraft.getMinecraft().theWorld;
//        if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
//
//        }
//    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void blockHighlight(DrawBlockHighlightEvent event) {
        MovingObjectPosition target = event.target;
        EntityPlayer player = event.player;

        if (player.isSneaking() && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting) player.getCurrentEquippedItem().getItem();
            if (wand.getFocus(player.getCurrentEquippedItem()) != null && wand.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) {
                if (lastDataLookedAt == null || lastDataLookedAt.getCoords().x != target.blockX || lastDataLookedAt.getCoords().y != target.blockY || lastDataLookedAt.getCoords().z != target.blockZ)
                    lastDataLookedAt = TIWorldData.getWorldData(player.worldObj).getBlock(BlockData.class, new WorldCoord(target.blockX, target.blockY, target.blockZ));

                if (lastDataLookedAt != null) {
                    ForgeDirection dir = MathHelper.sideToDirection(target.sideHit);
                    AspectList list = new AspectList();
                    for (Aspect aspect : lastDataLookedAt.getAspects())
                        list.add(aspect, AspectHandler.getCostOfEffect(aspect));

                    if (RenderEventHandler.tagscale < 0.5F)
                        RenderEventHandler.tagscale += 0.031F - RenderEventHandler.tagscale / 10.0F;

                    Thaumcraft.instance.renderEventHandler.drawTagsOnContainer((double) ((float) target.blockX + (float) dir.offsetX / 2.0F), (double) ((float) target.blockY + (float) dir.offsetY / 2.0F), (double) ((float) target.blockZ + (float) dir.offsetZ / 2.0F), list, 220, dir, event.partialTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void renderLast(RenderWorldLastEvent event) {

        float partialTicks = event.partialTicks;
        if (Minecraft.getMinecraft().renderViewEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().renderViewEntity;
            World world = player.worldObj;

            if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemWandCasting) {
                ItemWandCasting wand = (ItemWandCasting) player.getCurrentEquippedItem().getItem();
                if (wand.getFocus(player.getCurrentEquippedItem()) != null && wand.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) {

                    TIWorldData worldData = TIWorldData.getWorldData(world);
                    if (worldData == null)
                        return;

                    for (BlockSavable savable : worldData.getAllStoredData()) {
                        if(savable instanceof BlockData) {
                            BlockData data = (BlockData)savable;
                            int x = data.getCoords().x, y = data.getCoords().y, z = data.getCoords().z;
                            currentdata = data;

                            double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
                            double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
                            double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

                            GL11.glPushMatrix();
                            GL11.glEnable(3042);
                            GL11.glBlendFunc(770, 1);
                            GL11.glAlphaFunc(516, 0.003921569F);
                            GL11.glTranslated(-iPX + x + 0.5D, -iPY + y, -iPZ + z + 0.5D);

                            RenderBlocks renderBlocks = new RenderBlocks();
                            GL11.glDisable(2896);
                            Tessellator t = Tessellator.instance;
                            renderBlocks.setRenderBounds(-0.0010000000474974513D, -0.0010000000474974513D, -0.0010000000474974513D, 1.0010000467300415D, 1.0010000467300415D, 1.0010000467300415D);
                            Aspect[] aspects = data.getAspects();
                            if (aspects == null)
                                return;

                            new RGB(aspects[0].getColor()).glColor3f();

                            t.startDrawingQuads();
                            t.setBrightness(200);
                            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                            GL11.glTexEnvi(8960, 8704, 260);

                            if (!isConnectedBlock(world, x - Facing.offsetsXForSide[1], y - Facing.offsetsYForSide[1], z - Facing.offsetsZForSide[1]))
                                renderBlocks.renderFaceYNeg(ConfigBlocks.blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 0, player.ticksExisted));

                            if (!isConnectedBlock(world, x - Facing.offsetsXForSide[0], y - Facing.offsetsYForSide[0], z - Facing.offsetsZForSide[0]))
                                renderBlocks.renderFaceYPos(ConfigBlocks.blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 1, player.ticksExisted));

                            if (!isConnectedBlock(world, x - Facing.offsetsXForSide[3], y - Facing.offsetsYForSide[3], z - Facing.offsetsZForSide[3]))
                                renderBlocks.renderFaceZNeg(ConfigBlocks.blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 2, player.ticksExisted));

                            if (!isConnectedBlock(world, x - Facing.offsetsXForSide[2], y - Facing.offsetsYForSide[2], z - Facing.offsetsZForSide[2]))
                                renderBlocks.renderFaceZPos(ConfigBlocks.blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 3, player.ticksExisted));

                            if (!isConnectedBlock(world, x - Facing.offsetsXForSide[5], y - Facing.offsetsYForSide[5], z - Facing.offsetsZForSide[5]))
                                renderBlocks.renderFaceXNeg(ConfigBlocks.blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 4, player.ticksExisted));

                            if (!isConnectedBlock(world, x - Facing.offsetsXForSide[4], y - Facing.offsetsYForSide[4], z - Facing.offsetsZForSide[4]))
                                renderBlocks.renderFaceXPos(ConfigBlocks.blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 5, player.ticksExisted));

                            t.draw();
                            GL11.glTexEnvi(8960, 8704, 8448);
                            GL11.glEnable(2896);
                            GL11.glAlphaFunc(516, 0.1F);
                            GL11.glDisable(3042);
                            GL11.glColor3f(1.0F, 1.0F, 1.0F);
                            GL11.glPopMatrix();
                        }
                    }
                }
            }
        }
    }

    private boolean isConnectedBlock(World world, int x, int y, int z) {
        BlockData data = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (data == null)
            return false;

        int same = 0;
        for (Aspect aspect : data.getAspects()) {
            for (Aspect aspect2 : currentdata.getAspects()) {
                if (aspect == aspect2) {
                    same++;
                    break;
                }
            }
        }
        return same == data.getAspects().length;
    }

    private IIcon getIconOnSide(World world, int x, int y, int z, int side, int ticks) {
        WorldCoordinates wc = new WorldCoordinates(x, y, z, side);
        IIcon out = iconCache.get(wc);
        if ((ticks + side) % 10 == 0 || out == null) {
            boolean[] bitMatrix = new boolean[8];
            if (side == 0 || side == 1) {
                bitMatrix[0] = this.isConnectedBlock(world, x - 1, y, z - 1);
                bitMatrix[1] = this.isConnectedBlock(world, x, y, z - 1);
                bitMatrix[2] = this.isConnectedBlock(world, x + 1, y, z - 1);
                bitMatrix[3] = this.isConnectedBlock(world, x - 1, y, z);
                bitMatrix[4] = this.isConnectedBlock(world, x + 1, y, z);
                bitMatrix[5] = this.isConnectedBlock(world, x - 1, y, z + 1);
                bitMatrix[6] = this.isConnectedBlock(world, x, y, z + 1);
                bitMatrix[7] = this.isConnectedBlock(world, x + 1, y, z + 1);
            }

            if (side == 2 || side == 3) {
                bitMatrix[0] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y + 1, z);
                bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z);
                bitMatrix[2] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y + 1, z);
                bitMatrix[3] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y, z);
                bitMatrix[4] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y, z);
                bitMatrix[5] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y - 1, z);
                bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z);
                bitMatrix[7] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y - 1, z);
            }

            if (side == 4 || side == 5) {
                bitMatrix[0] = this.isConnectedBlock(world, x, y + 1, z + (side == 5 ? 1 : -1));
                bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z);
                bitMatrix[2] = this.isConnectedBlock(world, x, y + 1, z + (side == 4 ? 1 : -1));
                bitMatrix[3] = this.isConnectedBlock(world, x, y, z + (side == 5 ? 1 : -1));
                bitMatrix[4] = this.isConnectedBlock(world, x, y, z + (side == 4 ? 1 : -1));
                bitMatrix[5] = this.isConnectedBlock(world, x, y - 1, z + (side == 5 ? 1 : -1));
                bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z);
                bitMatrix[7] = this.isConnectedBlock(world, x, y - 1, z + (side == 4 ? 1 : -1));
            }

            int idBuilder = 0;

            for (int i = 0; i <= 7; ++i)
                idBuilder += bitMatrix[i] ? (i == 0 ? 1 : (i == 1 ? 2 : (i == 2 ? 4 : (i == 3 ? 8 : (i == 4 ? 16 : (i == 5 ? 32 : (i == 6 ? 64 : 128))))))) : 0;


            out = (idBuilder <= 255 && idBuilder >= 0) ? BlockCosmeticOpaque.wardedGlassIcon[UtilsFX.connectedTextureRefByID[idBuilder]] : BlockCosmeticOpaque.wardedGlassIcon[0];
            iconCache.put(wc, out);
        }

        return out;
    }

}
