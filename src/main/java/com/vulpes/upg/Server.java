package com.vulpes.upg;

import net.minecraft.world.World;

public class Server {
    static int MIN_DIMENSION = -20;
    static int MAX_DIMENSION = 20;
    static WorldManager MANAGERS[] = new WorldManager[MAX_DIMENSION - MIN_DIMENSION + 1];

    public static WorldManager getManager(World world) {
        int dimension = world.provider.getDimension();
        if (dimension >= MIN_DIMENSION && dimension <= MAX_DIMENSION && world.provider.isSurfaceWorld()) {
            int i = dimension - MIN_DIMENSION;
            WorldManager manager = MANAGERS[i];
            if (manager == null) {
                String name = WorldManager.saveName(dimension);
                manager = (WorldManager) world.loadData(WorldManager.class, name);
                if (manager == null) {
                    manager = new WorldManager(name);
                }
                MANAGERS[i] = manager;
            }
            return manager;
        }
        return null;
    }
}
