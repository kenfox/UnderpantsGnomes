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

public class Mine {
    private BlockPos minePos; // position of center of mine.
    private EnumFacing mineFacing; // direction mine faces.

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

    static void log(String msg) {
        UnderpantsGnomes.logger.info(msg);
    }

    public Mine(World world, BlockPos pos) {
        minePos = pos.add(0, -1, 0);
        mineFacing = EnumFacing.NORTH;
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

        // FIXME construct ArrayList<Branch>
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

    public void expand(World world) {
        if (--delayRemaining <= 0) {
            log("mining!");
            digTest2(world);
            // FIXME actually expand mine!
            delayRemaining = 200;
        }
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

    void digBlockInMine(World world, BlockPos minePos, NonNullList<ItemStack> drops) {
        IBlockState state = world.getBlockState(minePos);
        Block block = state.getBlock();
        block.getDrops(drops, world, minePos, state, 0);
        world.setBlockToAir(minePos);
    }
}
