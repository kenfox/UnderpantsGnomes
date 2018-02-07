package com.vulpes.upg;

import com.vulpes.upg.world.Server;
import com.vulpes.upg.world.WorldManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.apache.logging.log4j.Logger;

@Mod(
        modid = UnderpantsGnomes.MOD_ID,
        name = UnderpantsGnomes.MOD_NAME,
        version = UnderpantsGnomes.VERSION
)
public class UnderpantsGnomes {

    public static final String MOD_ID = "underpants-gnomes";
    public static final String MOD_NAME = "Underpants Gnomes";
    public static final String VERSION = "0.0.1";

    static public Logger logger;

    @Mod.Instance(MOD_ID)
    public static UnderpantsGnomes INSTANCE;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    }

    @GameRegistry.ObjectHolder("minecraft")
    public static class Thing {
        // FIXME how to tell intellij that these are not really null?
        public static final Block wall_sign = null;
        public static final Block chest = null;

        public static final Block log = null;
        public static final Item coal = null;

        public static final Block torch = null;
        public static final Item wooden_pickaxe = null;
        public static final Item stone_pickaxe = null;
        public static final Item iron_pickaxe = null;
        public static final Item diamond_pickaxe = null;

        public static final Block stone = null;
        public static final Block dirt = null;
        public static final Block gravel = null;
        public static final Block cobblestone = null;
        public static final Block iron_ore = null;
        public static final Block coal_ore = null;
        public static final Block diamond_ore = null;
    }

    @Mod.EventBusSubscriber
    public static class Handlers {
        @SubscribeEvent
        public static void worldTick(TickEvent.WorldTickEvent event) {
            World world = event.world;
            if (event.phase == TickEvent.Phase.END) {
                WorldManager manager = Server.getManager(world);
                manager.periodicRefreshMines(world);
                manager.expandMines(world);
            }
        }

        @SubscribeEvent
        public static void blockHit(PlayerInteractEvent.RightClickBlock event) {
            World world = event.getWorld();
            if (!world.isRemote && event.getHand() == EnumHand.MAIN_HAND) {
                WorldManager manager = Server.getManager(world);
                if (manager.addPossibleMine(world, event.getPos())) {
                    logger.info("Gnomes appear interested");
                }
            }
        }
    }
}
