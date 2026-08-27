package com.bettercontent.watersurvival;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Observes completion of the native drink action, after the item was actually consumed. */
public final class WaterThreadEvents {
    private WaterThreadEvents() {}

    @SubscribeEvent
    public static void onDrinkFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!WaterBottleCurio.isWaterBottle(event.getItem())) return;
        if (WaterPurity.getPurity(event.getItem()) == WaterPurity.MAX_PURITY) ThreadsBridge.purifiedDrunk(player);
    }
}
