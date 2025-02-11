package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Created by DrunkMafia on 13/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "sano", cost = 1)
public class Sano extends AspectEffect {
    @Override
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if(!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, new Random());
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(15.0D, 15.0D, 15.0D));
        for(EntityPlayer player : players) {
            FoodStats food = player.getFoodStats();
            if (food.getFoodLevel() > 0 && random.nextBoolean())
                player.heal(1F);
        }
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 50);
    }
}
