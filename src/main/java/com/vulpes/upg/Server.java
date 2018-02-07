package com.vulpes.upg;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class Server {
    // Gnomes really hate some dimensions.
    static public boolean isGnomishWorld(World world) {
        int dimension = world.provider.getDimension();
        return (dimension >= MIN_DIMENSION
                && dimension <= MAX_DIMENSION
                && world.provider.isSurfaceWorld());
    }

    static private int MIN_DIMENSION = -20;
    static private int MAX_DIMENSION = 20;
    static private WorldManager MANAGERS[] = new WorldManager[MAX_DIMENSION - MIN_DIMENSION + 1];

    static private WorldManager getManager(int dimension) {
        int i = dimension - MIN_DIMENSION;
        return MANAGERS[i];
    }

    static private void setManager(int dimension, WorldManager manager) {
        int i = dimension - MIN_DIMENSION;
        MANAGERS[i] = manager;
    }

    static private class DummyWorldManager extends WorldManager {
        DummyWorldManager() { super("do-not-save"); }
        void save(World world) { }
        public void periodicRefreshMines(World world) { }
        public boolean addPossibleMine(World world, BlockPos pos) { return false; }
        public void expandMines(World world) { }
    }

    static private WorldManager DUMMY = new DummyWorldManager();

    @Nonnull
    public static WorldManager getManager(World world) {
        int dimension = world.provider.getDimension();
        if (dimension >= MIN_DIMENSION && dimension <= MAX_DIMENSION) {
            WorldManager manager = getManager(dimension);
            if (manager == null) {
                if (isGnomishWorld(world)) {
                    manager = WorldManager.getManager(world);
                }
                else {
                    manager = DUMMY;
                }
                setManager(dimension, manager);
            }
            return manager;
        }
        return DUMMY;
    }
}
