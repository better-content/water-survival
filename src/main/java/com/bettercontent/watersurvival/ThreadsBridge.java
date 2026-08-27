package com.bettercontent.watersurvival;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Optional Threads bridge for the player's current unsafe-water episode. */
public final class ThreadsBridge {
    private static final String ROOT = "WaterSurvivalThreadEpisode";
    private ThreadsBridge() {}

    public static void unsafeCollected(ServerPlayer player) {
        String token = player.getUUID() + ":water:" + player.server.getTickCount();
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putString(ROOT, token);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        emit(player, "water_collect", "unsafe", token);
    }

    public static void purifiedDrunk(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        String token = persisted.getString(ROOT);
        if (token.isBlank() || token.length() > 128) return;
        emit(player, "water_drink", "purified_correlated", token);
        persisted.remove(ROOT);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static void emit(ServerPlayer player, String type, String value, String token) {
        try {
            Class<?> api = Class.forName("com.bettercontent.threads.api.ThreadSignals");
            api.getMethod("emit", ServerPlayer.class, String.class, String.class, String.class)
                    .invoke(null, player, type, value, token);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
