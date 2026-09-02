package com.bettercontent.watersurvival;

import com.bettercontent.watersurvival.WaterSurvival;
import com.mojang.authlib.GameProfile;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

@PrefixGameTestTemplate(false)
public final class WaterSurvivalGameTests {
    private WaterSurvivalGameTests() {
    }

    @GameTest(templateNamespace = WaterSurvival.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void exposedCollectorFillsOneChargePerPulse(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativePos = new BlockPos(2, 200, 2);
        final BlockPos worldPos = helper.absolutePos(relativePos);
        makeBiomeRainy(level, worldPos);
        level.setWeatherParameters(0, 1200, true, false);
        level.setRainLevel(1.0F);
        final BlockState state = RainCollectorRegistry.RAIN_COLLECTOR.get().defaultBlockState();
        helper.setBlock(relativePos, state);
        RainCollectorRegistry.RAIN_COLLECTOR.get().tick(state, level, worldPos, RandomSource.create(1L));
        helper.assertBlockProperty(relativePos, RainCollectorBlock.LEVEL, 1);
        helper.succeed();
    }

    @GameTest(templateNamespace = WaterSurvival.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void coveredCollectorDoesNotFill(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativePos = new BlockPos(2, 200, 2);
        final BlockPos worldPos = helper.absolutePos(relativePos);
        level.setWeatherParameters(0, 1200, true, false);
        level.setRainLevel(1.0F);
        final BlockState state = RainCollectorRegistry.RAIN_COLLECTOR.get().defaultBlockState();
        helper.setBlock(relativePos, state);
        helper.setBlock(relativePos.above(2), Blocks.STONE);
        RainCollectorRegistry.RAIN_COLLECTOR.get().tick(state, level, worldPos, RandomSource.create(2L));
        helper.assertBlockProperty(relativePos, RainCollectorBlock.LEVEL, 0);
        helper.succeed();
    }

    @GameTest(templateNamespace = WaterSurvival.MOD_ID, template = "empty", timeoutTicks = 180)
    public static void snowInCampfireCubeMeltsWithoutExtinguishing(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos campfirePos = new BlockPos(3, 3, 3);
        final BlockPos lowerCorner = campfirePos.offset(-1, -1, -1);
        final BlockPos upperCorner = campfirePos.offset(1, 1, 1);
        final BlockPos directlyAbove = campfirePos.above();
        final BlockPos outside = campfirePos.offset(2, 0, 0);
        helper.setBlock(lowerCorner, Blocks.SNOW_BLOCK);
        helper.setBlock(upperCorner, Blocks.SNOW_BLOCK);
        helper.setBlock(directlyAbove, Blocks.SNOW_BLOCK);
        helper.setBlock(outside, Blocks.SNOW_BLOCK);
        // The one-block template is embedded underground. Cap melt targets so random overhead
        // gravel cannot fall into the resulting water before the delayed assertion runs.
        helper.setBlock(lowerCorner.above(), Blocks.STONE);
        helper.setBlock(upperCorner.above(), Blocks.STONE);
        helper.setBlock(directlyAbove.above(), Blocks.STONE);
        helper.setBlock(campfirePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        SnowMeltHandler.scheduleAroundCampfire(level, helper.absolutePos(campfirePos));
        helper.runAfterDelay(125, () -> {
            helper.assertBlockPresent(Blocks.WATER, lowerCorner);
            helper.assertBlockPresent(Blocks.WATER, upperCorner);
            helper.assertBlockPresent(Blocks.WATER, directlyAbove);
            helper.assertBlockPresent(Blocks.SNOW_BLOCK, outside);
            helper.assertBlockProperty(campfirePos, CampfireBlock.LIT, true);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = WaterSurvival.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void waterCurioTopsOffAndPersistsFractionWithoutBottle(final GameTestHelper helper) {
        final ServerPlayer player = fakePlayer(helper, "fractional-top-off");
        final var thirst = player.getCapability(ModCapabilities.PLAYER_THIRST).resolve()
                .orElseThrow(() -> new IllegalStateException("Mock player is missing the Thirst capability"));
        final var waterSlot = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.getStacksHandler(WaterBottleCurio.SLOT))
                .orElseThrow(() -> new IllegalStateException("Mock player is missing the water Curios slot"));
        waterSlot.getStacks().setStackInSlot(0, purifiedWaterBottles(2));
        thirst.setThirst(19);
        thirst.setQuenched(0);

        tickWaterCurio(player);
        helper.assertTrue(thirst.getThirst() == 20, "One missing thirst point should be restored immediately");
        helper.assertTrue(waterSlot.getStacks().getStackInSlot(0).getCount() == 2,
                "A one-point top-off should not consume a full bottle");
        helper.assertTrue(closeTo(WaterBottleCurio.getBottleFraction(player), 1.0D / 6.0D),
                "The first top-off should persist one sixth of a bottle");

        final ItemStack equippedBottles = waterSlot.getStacks().extractItem(0, 2, false);
        thirst.setThirst(19);
        tickWaterCurio(player);
        helper.assertTrue(thirst.getThirst() == 19, "An empty water slot must not provide hydration");
        helper.assertTrue(closeTo(WaterBottleCurio.getBottleFraction(player), 1.0D / 6.0D),
                "Removing the bottle must not clear fractional progress");
        waterSlot.getStacks().setStackInSlot(0, equippedBottles);

        for (int use = 0; use < 5; use++) {
            thirst.setThirst(19);
            tickWaterCurio(player);
        }
        helper.assertTrue(thirst.getThirst() == 20, "The sixth partial use should still top off thirst");
        helper.assertTrue(thirst.getQuenched() == 8, "A complete fractional bottle should restore eight quenched points");
        helper.assertTrue(waterSlot.getStacks().getStackInSlot(0).getCount() == 1,
                "Exactly one bottle should be consumed after six one-point top-offs");
        final var emptyBottleSlot = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.getStacksHandler(WaterBottleCurio.EMPTY_BOTTLE_SLOT))
                .orElseThrow(() -> new IllegalStateException("Mock player is missing the empty-bottle Curios slot"));
        helper.assertTrue(emptyBottleSlot.getStacks().getStackInSlot(0).is(Items.GLASS_BOTTLE)
                        && emptyBottleSlot.getStacks().getStackInSlot(0).getCount() == 1,
                "Completing a fractional bottle should return one empty bottle to its dedicated slot");
        helper.assertTrue(player.getInventory().countItem(Items.GLASS_BOTTLE) == 0,
                "Returned empty bottles must not spill into normal inventory while the dedicated slot has room");
        helper.assertTrue(WaterBottleCurio.getBottleFraction(player) == 0.0D,
                "A completed bottle should reset fractional progress");
        helper.succeed();
    }

    @GameTest(templateNamespace = WaterSurvival.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void waterCurioDoesNotBorrowPastAvailableBottle(final GameTestHelper helper) {
        final ServerPlayer player = fakePlayer(helper, "single-bottle-limit");
        final var thirst = player.getCapability(ModCapabilities.PLAYER_THIRST).resolve()
                .orElseThrow(() -> new IllegalStateException("Mock player is missing the Thirst capability"));
        final var waterSlot = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.getStacksHandler(WaterBottleCurio.SLOT))
                .orElseThrow(() -> new IllegalStateException("Mock player is missing the water Curios slot"));
        waterSlot.getStacks().setStackInSlot(0, purifiedWaterBottles(1));
        thirst.setThirst(0);
        thirst.setQuenched(0);

        tickWaterCurio(player);
        helper.assertTrue(thirst.getThirst() == 6, "One equipped bottle should restore only its six thirst points");
        helper.assertTrue(waterSlot.getStacks().getStackInSlot(0).isEmpty(), "The only equipped bottle should be consumed");
        helper.assertTrue(WaterBottleCurio.getBottleFraction(player) == 0.0D,
                "No fraction of an unavailable second bottle should be borrowed");
        helper.succeed();
    }

    private static ServerPlayer fakePlayer(final GameTestHelper helper, final String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "bcf-" + name));
    }

    private static void makeBiomeRainy(final ServerLevel level, final BlockPos pos) {
        final LevelChunk chunk = level.getChunkAt(pos);
        final var plains = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        chunk.getSection(chunk.getSectionIndex(pos.getY())).fillBiomesFromNoise(
                (quartX, quartY, quartZ, sampler) -> plains,
                level.getChunkSource().randomState().sampler(), 0, 0, 0);
        chunk.setUnsaved(true);
    }

    private static ItemStack purifiedWaterBottles(final int count) {
        final ItemStack bottles = PotionUtils.setPotion(new ItemStack(Items.POTION, count), Potions.WATER);
        return WaterPurity.addPurity(bottles, 3);
    }

    private static void tickWaterCurio(final ServerPlayer player) {
        player.tickCount += 10 - player.tickCount % 10;
        WaterBottleCurio.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
    }

    private static boolean closeTo(final double left, final double right) {
        return Math.abs(left - right) < 1.0E-9D;
    }
}
