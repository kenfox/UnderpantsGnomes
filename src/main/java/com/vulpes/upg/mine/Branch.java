package com.vulpes.upg.mine;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Branch {
    private Mine mine;
    private EnumFacing tunnelFacing;
    private BlockPos tunnelEnd;
    private boolean complete;

    public Branch(Mine parent, boolean reverse, BlockPos pos) {
        mine = parent;
        tunnelFacing = parent.getFacing(reverse);
        tunnelEnd = pos;
        complete = false;
    }

    boolean expand(World world) {
        // FIXME handle all the mine options
        // FIXME detect adjacent non-stone and dig those too
        // FIXME wall off fluids
        if (!complete) {
            if (mine.digBlock(world, tunnelEnd) &&
                mine.digBlock(world, Mine.upFrom(tunnelEnd, tunnelFacing, 1)))
            {
                tunnelEnd = Mine.forwardFrom(tunnelEnd, tunnelFacing, 1);
                complete = (Mine.distanceBetween(mine.getCenter(), tunnelEnd) > mine.getLength());
            }
        }
        return complete;
    }
}
