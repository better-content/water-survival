package com.bettercontent.watersurvival;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WaterSurvival.MOD_ID)
public final class WaterSurvival {
    public static final String MOD_ID = "water_survival";

    public WaterSurvival() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        RainCollectorRegistry.BLOCKS.register(bus);
        RainCollectorRegistry.ITEMS.register(bus);
        bus.addListener(this::registerGameTests);
        MinecraftForge.EVENT_BUS.register(SnowMeltHandler.class);
        MinecraftForge.EVENT_BUS.register(WaterBottleCurio.class);
        WaterBottleCurio.registerPredicate();
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(WaterSurvivalGameTests.class);
    }
}
