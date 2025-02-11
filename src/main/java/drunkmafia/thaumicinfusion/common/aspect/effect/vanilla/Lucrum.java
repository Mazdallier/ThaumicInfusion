package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "lucrum", cost = 4)
public class Lucrum extends AspectEffect {

    private int range = 10, tickTime = 4;
    private Map<EntityItem, PathEntity> paths = new HashMap<EntityItem, PathEntity>();

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        range = config.getInt("The range that lucrum can find items to pull", "Lucrum", range, 1, 40, "");
        tickTime = config.getInt("Tick Time", "Lucrum", tickTime, 1, 20, "Delay before the effect ticks again");
    }


    @Override
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if(!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        WorldCoord coord = getPos();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(coord.x, coord.y, coord.z, coord.x + 1, coord.y + 1, coord.z + 1).expand(10, 10, 10);
        ArrayList<EntityItem> list = (ArrayList<EntityItem>) world.getEntitiesWithinAABB(EntityItem.class, axisalignedbb);

        double speed = 0.05D;

        for(EntityItem item : list){
            if(!isItemNearBlock(item)){
                item.motionX = item.posX > coord.x ? -speed : speed;
                item.motionZ = item.posZ > coord.z ? -speed : speed;
            }
        }
    }

    boolean isItemNearBlock(EntityItem item){
        return getPos().getDistanceSquared((int)item.posX, (int)item.posY, (int)item.posZ) < 1;
    }
}
