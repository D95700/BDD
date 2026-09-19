package com.example.bddmod;

import com.example.bddmod.client.OBSMonitor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BDDMod.MODID)
public final class BDDMod {
    public static final String MODID = "bddmod";

    public BDDMod(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            OBSMonitor.start();
        }
    }
}
