package com.vulpes.upg;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Mine {
    BlockPos minePos;
    EnumFacing mineFacing;
    int delayRemaining;

    static void log(String msg) {
        UnderpantsGnomes.logger.info(msg);
    }

    Mine(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state != null) {
            Block block = state.getBlock();
            if (block == UnderpantsGnomes.Thing.wall_sign) {
                if (block.hasTileEntity(state)) {
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity instanceof TileEntitySign) {
                        TileEntitySign sign = (TileEntitySign) tileEntity;
                        if (sign.signText != null && sign.signText.length > 0) {
                            log("setting mine options from sign!");
                            // FIXME set non-default values from sign
                        }
                    }
                }
            }
        }

        minePos = pos.add(0, -1, 0);
        mineFacing = state.getValue(BlockWallSign.FACING);
        delayRemaining = 0;

        // FIXME construct ArrayList<Branch>
    }

    static public boolean isPresent(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state != null) {
            Block block = state.getBlock();
            if (block == UnderpantsGnomes.Thing.wall_sign) {
                if (block.hasTileEntity(state)) {
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity instanceof TileEntitySign) {
                        TileEntitySign sign = (TileEntitySign)tileEntity;
                        if (sign.signText != null && sign.signText.length > 0) {
                            // FIXME handle case when mine is finished by updating
                            // the sign so it is no longer a valid mine sign.
                            log("sign reads: " + sign.signText[0].getUnformattedText());
                            return true;
                        }
                        else {
                            // FIXME if someone punches a sign and it looks sort of like
                            // they intended it to be a Gnome mine, give them a hint.
                        }
                    }
                }
            }
        }
        return false;
    }

    public void expand(World world) {
        if (--delayRemaining <= 0) {
            log("mining!");
            // FIXME actually expand mine!
            delayRemaining = 20;
        }
    }

    void digTest(World world) {
        // FIXME code currently assumes mineFacing == north
        log("remote? " + world.isRemote + " facing " + mineFacing);
        NonNullList<ItemStack> drops = NonNullList.create();

        for (int digOffset = -5; digOffset < 6; ++digOffset) {
            BlockPos branchPos = minePos.add(digOffset, 0, 2);
            for (int dz = 0; dz < 4; ++dz) {
                for (int dy = 0; dy < 3; ++dy) {
                    digBlockInMine(world, branchPos, drops, dz, dy);
                }
            }
        }

        for (ItemStack drop : drops) {
            log("dropping " + drop);
            // dropping an item from the entity animates it
            //player.dropItem(drop, true);

            // dropping an item from a block as if it were an entity
            Block.spawnAsEntity(world, minePos, drop);
        }
    }

    void digBlockInMine(World world, BlockPos minePos, NonNullList<ItemStack> drops, int dz, int dy) {
        if (dz != 0 || dy != 0) {
            minePos = minePos.add(0, dy, dz);
        }
        IBlockState state = world.getBlockState(minePos);
        Block block = state.getBlock();
        block.getDrops(drops, world, minePos, state, 0);
        world.setBlockToAir(minePos);
    }
}
