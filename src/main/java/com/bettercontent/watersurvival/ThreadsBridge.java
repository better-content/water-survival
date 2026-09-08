package com.bettercontent.watersurvival;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Optional Threads bridge for the player's current unsafe-water episode. */
public final class ThreadsBridge {
    private static final String ROOT = "WaterSurvivalThreadEpisode";
    private ThreadsBridge() {}

    public static void thirstLost(ServerPlayer player) {
        String active = activeCorrelation(player, "water_made_safe");
        String token = active == null || active.isBlank()
                ? player.getUUID() + ":thirst:" + player.server.getTickCount()
                : active;
        remember(player, token);
        if (active == null || active.isBlank()) emit(player, "thirst_loss", "first_drop", token);
    }

    /** Retained for the collector call site; unsafe collection no longer teaches the card or replaces its thirst episode. */
    public static void unsafeCollected(ServerPlayer player) {
        if (!remembered(player).isBlank()) return;
        String active = activeCorrelation(player, "water_made_safe");
        if (active != null && !active.isBlank()) remember(player, active);
    }

    private static void remember(ServerPlayer player, String token) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putString(ROOT, token);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void purifiedDrunk(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        String token = persisted.getString(ROOT);
        if (token.isBlank()) token = activeCorrelation(player, "water_made_safe");
        if (token == null || token.isBlank() || token.length() > 128) return;
        emit(player, "water_drink", "purity_3_correlated", token);
        persisted.remove(ROOT);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    static String remembered(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getString(ROOT);
    }

    private static String activeCorrelation(ServerPlayer player, String threadId) {
        try {
            Class<?> api = Class.forName("com.bettercontent.threads.api.ThreadSignals");
            Object value = api.getMethod("activeCorrelation", ServerPlayer.class, String.class)
                    .invoke(null, player, threadId);
            return value instanceof String token ? token : null;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
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
