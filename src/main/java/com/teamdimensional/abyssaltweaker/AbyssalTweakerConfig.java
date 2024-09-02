package com.teamdimensional.abyssaltweaker;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
public class AbyssalTweakerConfig {

    @Config.Comment("Pairs of comma separated dimension IDs that can connect to each other. Example: '0,-1' will connect Overworld and Nether. Replaces the Abyssalcraft list unless it is empty, in which case nothing will be changed.")
    @Config.RequiresMcRestart
    public static String[] dimension_pairs = {};

    @Config.Comment("Pairs of comma separated dimension ID and key tier (0 = basic, 1 = dreaded, 2 = omothol, 3 = silver). Example: '-1,2' will allow Omothol and higher tiers to go into Nether. Default for non-AC keys is 0. Setting the tier of a dimension to 4 removes it from the Gateway Key whitelist.")
    @Config.RequiresMcRestart
    public static String[] dimension_key_tiers = {};

    @Config.Comment("Pairs of comma separated dimension ID and book tier (0 = basic, 1 = wasteland, 2 = dreaded, 3 = omothol, 4 = abyssalnomicon). Controls also the type of block used to build altars in that dimension. Example: '-1,2' will allow Dreadlands blocks to build rituals in Nether.")
    @Config.RequiresMcRestart
    public static String[] dimension_book_tiers = {};

}
