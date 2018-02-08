package com.vulpes.upg.mine;

import com.vulpes.upg.UnderpantsGnomes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

import static java.lang.Math.abs;
import static net.minecraft.util.EnumFacing.*;

public class Mine {
    private BlockPos minePos; // position of center of mine.
    private int mineWidth; // length of the central tunnel (from center).
    private int mineLength; // length of the branch tunnels.
    private EnumFacing mineFacing; // direction mine faces.

    private ArrayList<Branch> branch; // branch tunnels in-progress.

    private BlockPos tunnelEnd; // active digging in the central tunnel.
    private boolean leftComplete;
    private boolean rightComplete;

    private int delayRemaining; // ticks until next mine operation.

    private double happiness; // more happiness is more better.
    private double efficiency; // efficiency influences ores and drops.

    // Resources are taken from the chest @minePos as needed. Items
    // are converted into a balance that mining operations draw down.
    // This state is not kept across saves or chunk loading.
    //
    // A chest is not necessary at the mine position, but if one does
    // not exist, no resources are available to power the mine.

    private double foodRemaining;
    private double woodRemaining;
    private double coalRemaining;
    private double ironRemaining;

    // Options for building the mine are controlled by items placed into
    // the mine chest (the same one used for stocking resources).

    private boolean shouldLightTunnels; // set torches to light mine?
    private boolean shouldBuildFloor; // build floor across gaps?
    private boolean shouldBuildDown; // build ladders to lower levels?
    private boolean shouldBreakBasicOre; // break coal, iron, copper, etc.?
    private boolean shouldBreakFancyOre; // break diamond, redstone, lapis, etc.?
    private boolean shouldBreakEverything; // break everything else?

    private boolean inCreativeMode; // do operations require resources?

    public BlockPos upFrom(BlockPos p, int dist) {
        return upFrom(p, mineFacing, dist);
    }

    public BlockPos downFrom(BlockPos p, int dist) {
        return upFrom(p, mineFacing, -dist);
    }

    public BlockPos leftFrom(BlockPos p, int dist) {
        return leftFrom(p, mineFacing, dist);
    }

    public BlockPos rightFrom(BlockPos p, int dist) {
        return leftFrom(p, mineFacing, -dist);
    }

    public BlockPos forwardFrom(BlockPos p, int dist) {
        return forwardFrom(p, mineFacing, dist);
    }

    public BlockPos backwardFrom(BlockPos p, int dist) {
        return forwardFrom(p, mineFacing, -dist);
    }

    static public BlockPos upFrom(BlockPos p, EnumFacing facing, int dist) {
        if (dist == 0)
            return p;
        return p.add(0, dist, 0);
    }

    static public BlockPos downFrom(BlockPos p, EnumFacing facing, int dist) {
        return upFrom(p, facing, -dist);
    }

    static public BlockPos leftFrom(BlockPos p, EnumFacing facing, int dist) {
        if (dist == 0)
            return p;
        switch (facing) {
            case NORTH:
                return p.add(dist, 0, 0);
            case EAST:
                return p.add(0, 0, dist);
            case WEST:
                return p.add(0, 0, -dist);
            case UP:
            case DOWN:
            case SOUTH:
            default:
                return p.add(-dist, 0, 0);
        }
    }

    static public BlockPos rightFrom(BlockPos p, EnumFacing facing, int dist) {
        return leftFrom(p, facing, -dist);
    }

    static public BlockPos forwardFrom(BlockPos p, EnumFacing facing, int dist) {
        if (dist == 0)
            return p;
        switch (facing) {
            case NORTH:
                return p.add(0, 0, dist);
            case EAST:
                return p.add(-dist, 0, 0);
            case WEST:
                return p.add(dist, 0, 0);
            case UP:
            case DOWN:
            case SOUTH:
            default:
                return p.add(0, 0, -dist);
        }
    }

    static public BlockPos backwardFrom(BlockPos p, EnumFacing facing, int dist) {
        return forwardFrom(p, facing, -dist);
    }

    static public EnumFacing reverseFacing(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case SOUTH:
            default:
                return NORTH;
        }
    }

    public static int distanceBetween(BlockPos branchPos, BlockPos minePos) {
        int x = abs(branchPos.getX() - minePos.getX());
        int z = abs(branchPos.getZ() - minePos.getZ());
        return (x > z) ? x : z;
    }

    EnumFacing getFacing(boolean reverse) {
        return (reverse) ? reverseFacing(mineFacing) : mineFacing;
    }

    public int getWidth() { return mineWidth; }
    public int getLength() { return mineLength; }
    public BlockPos getCenter() { return minePos; }

    static void log(String msg) {
        UnderpantsGnomes.logger.info(msg);
    }

    public Mine(World world, BlockPos pos) {
        minePos = pos.add(0, -1, 0);
        mineWidth = 48;
        mineLength = 48;
        mineFacing = NORTH;
        delayRemaining = 0;

        happiness = 0.0;
        efficiency = 0.0;

        foodRemaining = 0.0;
        woodRemaining = 0.0;
        coalRemaining = 0.0;
        ironRemaining = 0.0;

        shouldLightTunnels = false;
        shouldBuildFloor = false;
        shouldBuildDown = false;
        shouldBreakBasicOre = false;
        shouldBreakFancyOre = false;
        shouldBreakEverything = false;

        inCreativeMode = false;

        configureMineUsingSign(world, pos);
        configureMineUsingChest(world);

        log("facing is " + mineFacing);
        log("lighting? " + shouldLightTunnels);
        log("flooring? " + shouldBuildFloor);
        log("breaking blocks? basic=" + shouldBreakBasicOre + " fancy=" + shouldBreakFancyOre + " all=" + shouldBreakEverything);

        tunnelEnd = leftFrom(minePos, 1);
        leftComplete = false;
        rightComplete = false;

        branch = new ArrayList<>();

        BlockPos leftPos = leftFrom(minePos, 2);
        BlockPos rightPos = rightFrom(minePos, 2);
        do {
            branch.add(new Branch(this, false, forwardFrom(leftPos, 1)));
            branch.add(new Branch(this, true, backwardFrom(leftPos, 3)));
            branch.add(new Branch(this, false, forwardFrom(rightPos, 1)));
            branch.add(new Branch(this, true, backwardFrom(rightPos, 3)));
            leftPos = leftFrom(leftPos, 4);
            rightPos = rightFrom(rightPos, 4);
        } while (distanceBetween(leftPos, minePos) < mineWidth);
    }

    private void configureMineUsingSign(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state != null) {
            Block block = state.getBlock();
            if (block == UnderpantsGnomes.Thing.wall_sign) {
                mineFacing = state.getValue(BlockWallSign.FACING);
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
    }

    private void configureMineUsingChest(World world) {
        IBlockState state = world.getBlockState(minePos);
        if (state != null) {
            Block block = state.getBlock();
            if (block == UnderpantsGnomes.Thing.chest) {
                if (block.hasTileEntity(state)) {
                    TileEntity tileEntity = world.getTileEntity(minePos);
                    if (tileEntity instanceof TileEntityChest) {
                        ItemStack ironStack = new ItemStack(UnderpantsGnomes.Thing.iron_ore, 1);
                        TileEntityChest chest = (TileEntityChest) tileEntity;
                        int size = chest.getSizeInventory();
                        for (int i = 0; i < size; ++i) {
                            ItemStack stack = chest.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                configureMineUsingItem(ironStack, stack.getItem());
                            }
                        }
                    }
                }
            }
        }
    }

    private void configureMineUsingItem(ItemStack ironStack, Item anything) {
        if (anything instanceof ItemTool) {
            // FIXME does this work for Tinker's?
            ItemTool pickaxe = (ItemTool) anything;
            int harvest = pickaxe.getHarvestLevel(ironStack, "pickaxe", null, null);
            if (harvest >= 3) {
                shouldBreakEverything = true;
            }
            if (harvest >= 2) {
                shouldBreakFancyOre = true;
            }
            if (harvest >= 1) {
                shouldBreakBasicOre = true;
            }
        }
        else if (anything instanceof ItemBlock) {
            // FIXME use ore dictionary instead?
            ItemBlock item = (ItemBlock) anything;
            ResourceLocation resLoc = item.getRegistryName();
            if (resLoc != null) {
                String name = resLoc.getResourcePath();
                switch (name) {
                    case "torch":
                        shouldLightTunnels = true;
                        break;
                    case "stone_slab":
                        shouldBuildFloor = true;
                        break;
                    case "ladder":
                        shouldBuildDown = true;
                        break;
                }
            }
        }
    }

    void consumeResourcesFromChest(World world) {
        IBlockState state = world.getBlockState(minePos);
        if (state != null) {
            Block block = state.getBlock();
            if (block == UnderpantsGnomes.Thing.chest) {
                if (block.hasTileEntity(state)) {
                    TileEntity tileEntity = world.getTileEntity(minePos);
                    if (tileEntity instanceof TileEntityChest) {
                        TileEntityChest chest = (TileEntityChest) tileEntity;
                        int size = chest.getSizeInventory();
                        for (int i = 0; i < size; ++i) {
                            ItemStack stack = chest.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                Item anything = stack.getItem();
                                log("contains: @" + i +
                                        " x" + stack.getCount() +
                                        " " + anything.getRegistryName());
                                if (anything instanceof ItemFood) {
                                    ItemFood food = (ItemFood) anything;
                                    // FIXME why do these take a stack?
                                    int heal = food.getHealAmount(stack);
                                    float saturation = food.getSaturationModifier(stack);
                                    log("  food! heal=" + heal + " saturation=" + saturation);
                                }
                            }
                        }
                    }
                }
            }
        }
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
            else {
                log("hit block " + block + " state=" + state);
            }
        }
        return false;
    }

    private boolean expandCentralTunnel(World world) {
        if (!leftComplete) {
            if (digCentralTunnelSection(world)) {
                tunnelEnd = leftFrom(tunnelEnd, 1);
                leftComplete = (distanceBetween(tunnelEnd, minePos) > mineWidth);
                if (leftComplete) {
                    tunnelEnd = rightFrom(minePos, 1);
                }
            }
            return false;
        }
        if (!rightComplete) {
            if (digCentralTunnelSection(world)) {
                tunnelEnd = rightFrom(tunnelEnd, 1);
                rightComplete = (distanceBetween(tunnelEnd, minePos) > mineWidth);
            }
        }
        return rightComplete;
    }

    private boolean digCentralTunnelSection(World world) {
        // FIXME all the same issues as with the branch expand
        BlockPos a = tunnelEnd;
        BlockPos b = backwardFrom(tunnelEnd, 1);
        BlockPos c = backwardFrom(tunnelEnd, 2);
        return (digBlock(world, a) &&
                digBlock(world, upFrom(a, 1)) &&
                digBlock(world, upFrom(a, 2)) &&
                digBlock(world, b) &&
                digBlock(world, upFrom(b, 1)) &&
                digBlock(world, upFrom(b, 2)) &&
                digBlock(world, c) &&
                digBlock(world, upFrom(c, 1)) &&
                digBlock(world, upFrom(c, 2)));
    }

    public void expand(World world) {
        if (--delayRemaining <= 0) {
            delayRemaining = 20;
            log("mining!");

            if (expandCentralTunnel(world)) {
                int completed = 0;
                for (Branch b : branch) {
                    // FIXME should all the branches expand in the same tick?
                    if (b.expand(world)) {
                        ++completed;
                    }
                }
                if (completed == branch.size()) {
                    log("all branch mines complete!");
                    // FIXME should the branch list be cleared?
                    delayRemaining = 1200;
                }
            }
        }
    }

    public boolean digBlock(World world, BlockPos pos) {
        // digging an air block costs nothing.
        // FIXME consume resources and move the block or dropped items
        world.setBlockToAir(pos);
        return true;
    }

    void digTest1(World world) {
        log("digTest1: remote? " + world.isRemote + " facing " + mineFacing);
        BlockPos p = minePos;
        for (int i = 0; i < 5; ++i) {
            p = leftFrom(p, 1);
            world.setBlockToAir(p);
        }
        for (int i = 0; i < 4; ++i) {
            p = forwardFrom(p, 1);
            world.setBlockToAir(p);
        }
        for (int i = 0; i < 3; ++i) {
            p = rightFrom(p, 1);
            world.setBlockToAir(p);
        }
        for (int i = 0; i < 2; ++i) {
            p = backwardFrom(p, 1);
            world.setBlockToAir(p);
        }
    }

    void digTest2(World world) {
        log("digtest2: remote? " + world.isRemote + " facing " + mineFacing);
        NonNullList<ItemStack> drops = NonNullList.create();

        for (int digOffset = -5; digOffset < 2; ++digOffset) {
            BlockPos branchPos = leftFrom(minePos, digOffset);
            if (digOffset == 0)
                branchPos = forwardFrom(branchPos, 2);
            for (int dz = 0; dz < 4; ++dz) {
                for (int dy = 0; dy < 3; ++dy) {
                    BlockPos p = upFrom(forwardFrom(branchPos, dz), dy);
                    digBlockInMine(world, p, drops);
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

    private void digBlockInMine(World world, BlockPos minePos, NonNullList<ItemStack> drops) {
        IBlockState state = world.getBlockState(minePos);
        Block block = state.getBlock();
        block.getDrops(drops, world, minePos, state, 0);
        world.setBlockToAir(minePos);
    }
}
