# UnderpantsGnomes

This mod gives you some very early game (day one!) mining automation.
Food and other basic resources are used to power the mine. RF power is
not needed. The idea is to fill a chest with stuff gnomes want and
then place a sign above it asking for help. They will dig out a branch
mine while you are busy doing other things.

I've tried to balance the mod for early and mid-game. Gnomes mine a
bit faster than you, but eat more. They need about the same amount of
wood and coal to light up the mine with torches and replace their
pickaxes. At high levels near the surface, only food is required for
mining. A small mine can be started with only an apple and a couple logs.

Mining deeper than level 30 requires underpants. Underpants can be
crafted out of string and dye. The more kinds of underpants you offer
them, the faster and more efficiently they work. Iron and better food
are also very important at deeper levels.

When gnomes dig out blocks, what happens to the blocks depends on what
quality of tools you are giving them. The default stone pickaxe will
break basic blocks like coal and iron ore, but not quartz for
example. Iron will break most blocks and diamond pickaxes will break
everything. You may not want to give them diamond though if you can't
keep them happy--their happiness works similar to fortune. Blocks that
the gnomes can't break will be hauled into a store room for you to
break. All other drops (including the stone) will be placed into a
chest in the same store room. They will not void or compress any
blocks. It's up to you to manage the output. Mid-game you will
definitely want to automate this!

If you need larger amounts of basic ore than what the gnomes are
mining, you can try calling on dwarves for help instead. They consume
massive quantities of ale and other alcohols so you'll definitely want
some farming automation. The advantage to dwarves is that they can
find basic ores in common stone. They also have potentially higher
mining fortune if you can keep them lubricated enough.

UnderpantsGnomes can be used server-side with a vanilla client. You must
change the configuration to re-balance around other resources besides
underpants and alcohol. Food balance should also be changed if you
aren't using Pam's Harvestcraft or other mods with high quality food.

Late game you will probably switch to mining with Immersive
Engineering's Excavator, Botania's Orechid or Environmental Tech's
Void Miner. You will also need one of these other solutions if you
want to mine resources in hell or some exotic non-surface dimension.

# How to start a mine

1. Dig a small room where you want the center of your mine to be. The
height of the room (up to 5 blocks) determines the height of the mine
tunnels. The bottom level of the room must be level 30 or higher for
this starter mine.
2. Place down a small chest next to a wall. Not a double chest. Not an
ender chest. Not a crate. Just a 27 slot basic chest. Gnomes are
simple creatures.
3. Put logs and food into the chest. An apple or piece of bread and
two logs will get you started.
4. Place a sign on the wall directly above the chest and write: "MINE
HERE PLEASE"
5. Bang the sign for luck. (Right click it with an open hand.) Go away
and leave the gnomes to their work. Food and resources will start to
disappear from the chest as the mine is built. You need to restock
it. The store room across from the chest will start to fill up. I hope
you have a plan for how to empty it because when it's completely full,
the gnomes will stop mining until you empty it.

# YOU WON'T SEE ANY GNOMES OR DWARVES

UnderpantsGnomes does not animate anything on the client. You won't see
or hear anything while a mine is operating. They may occasionally write on
the sign though.

# Mining options

You can change how the mine works by changing the sign or the contents of the
chest BEFORE YOU BANG ON THE SIGN. If the mine is already operating, you must
replace the existing sign and restart the mine. Consumables can be placed into
the chest at any time.

Words to write on the sign:

* "MINE HERE PLEASE" -- the key phrase to get everything started
* "{width} x {length}" -- the horizontal size of the mine in blocks

Non-consumable items to place in the chest:

* torch -- lights up all tunnels with torches
* stone slab -- extends floors over open air such as chasms
* bucket -- clears liquids from the mine
* minecart -- increases the maximum size of the mine to 272x272
* rabbit foot -- increases the maximum efficiency of the mine
* stone shovel -- increases speed of digging dirt and gravel
* stone axe -- required to allow breaking non-ore blocks
* iron pickaxe -- breaks more blocks (less manual work in your store room)
* diamond pickaxe -- breaks all blocks (full automation)

Consumable resources to place in the chest:

* wood logs -- required for digging and making torches
* coal or charcoal -- required for making torches
* iron ingots -- required for making iron pickaxes
* diamonds -- required for making diamond pickaxes
* any food -- required for digging, complex foods increase happiness
* any underpants -- required for gnomes mining deep, variety increases efficiency
* any alcohol -- required for dwarves

# Yields

The largest mine is 272x272 with 5 high tunnels for a total volume of
272x272x7 or 517,888 blocks. A more efficient configuration though
(with less waste stone) is two mines stacked above each other each with
only 2 high tunnels. Each of the mines contains 295,936
blocks. Combined they have a total volume of 591,872 and require only
80% of the tunnel digging of the 5 high tunnel mine.

The size of a mine is controlled by writing the size you want on the
sign. The height of a mine is controlled by how high you dig the
ceiling in the initial room. A minecart is required for mines larger
than 160x160.

** I will provide more info on ore percentage and drops as I test the mod **

# Mod Development

In creative mode no resources are needed while mining. You can also set the mine
efficiency and happiness levels directly so this makes it easy to test
different mine building logic and game play without spawning in lots of items.
To reset an area of the world, the /fill command is helpful, e.g.:

    /fill ~50 ~-2 ~-0 ~-50 ~-1 ~-50 stone 0
