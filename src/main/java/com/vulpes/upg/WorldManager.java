package com.vulpes.upg;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class WorldManager extends WorldSavedData {
    static final String MINES_NBT_NAME = "gnomeMines";

    private HashSet<BlockPos> possibleMinePos = new HashSet<>();
    private HashMap<BlockPos, Mine> mineCache = new HashMap<>();
    private int ticksUntilMinesRefresh = 200;

    public WorldManager(String name) {
        super(name);
    }

    @Nonnull
    static public WorldManager getManager(World world) {
        MapStorage storage = world.getPerWorldStorage();
        int dimension = world.provider.getDimension();
        String name = UnderpantsGnomes.MOD_ID + "_DIM" + dimension;
        WorldManager manager = (WorldManager) storage.getOrLoadData(WorldManager.class, name);
        if (manager == null) {
            manager = new WorldManager(name);
        }
        return manager;
    }

    void save(World world) {
        MapStorage storage = world.getPerWorldStorage();
        storage.setData(mapName, this);
        markDirty();
    }

    private Mine refreshCachedMine(World world, BlockPos pos) {
        Mine mine = mineCache.get(pos);
        if (mine == null) {
            if (Mine.isPresent(world, pos)) {
                mine = new Mine(world, pos);
                mineCache.put(pos, mine);
            }
        }
        else {
            if (!Mine.isPresent(world, pos)) {
                mine = null;
                mineCache.remove(pos);
            }
        }
        return mine;
    }

    private void refreshMines(World world) {
        boolean dirty = false;
        Iterator<BlockPos> i = possibleMinePos.iterator();
        while (i.hasNext()) {
            BlockPos pos = i.next();
            if (world.isBlockLoaded(pos)) {
                Mine mine = refreshCachedMine(world, pos);
                if (mine == null) {
                    dirty = true;
                    i.remove();
                }
            }
            else {
                mineCache.remove(pos);
            }
        }
        if (dirty) {
            save(world);
        }
    }

    public void periodicRefreshMines(World world) {
        if (ticksUntilMinesRefresh == 0) {
            refreshMines(world);
            ticksUntilMinesRefresh = 20;
        } else {
            --ticksUntilMinesRefresh;
        }
    }

    public boolean addPossibleMine(World world, BlockPos pos) {
        if (world.isBlockLoaded(pos)) {
            if (Mine.isPresent(world, pos)) {
                Mine mine = new Mine(world, pos);
                mineCache.put(pos, mine);
                possibleMinePos.add(pos);
                save(world);
                return true;
            }
        }
        return false;
    }

    public void expandMines(World world) {
        for (HashMap.Entry<BlockPos, Mine> mine: mineCache.entrySet()) {
            if (world.isBlockLoaded(mine.getKey())) {
                mine.getValue().expand(world);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (BlockPos minePos : possibleMinePos) {
            list.appendTag(NBTUtil.createPosTag(minePos));
        }
        compound.setTag(MINES_NBT_NAME, list);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList(MINES_NBT_NAME, Constants.NBT.TAG_COMPOUND);
        possibleMinePos = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            possibleMinePos.add(NBTUtil.getPosFromTag(list.getCompoundTagAt(i)));
        }
        // The cache will be expanded as chunks load and mining is started.
        mineCache = new HashMap<>();
    }
}
