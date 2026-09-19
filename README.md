# Water Survival

Forge 1.20.1 mod owning rain collection, campfire snow melting, and dedicated water/empty-bottle Curios slots.

Partial Curios sips are stored on the opened water-bottle ItemStack. Removing or transferring that bottle carries its consumed fraction and purity with it; a different bottle begins at zero and gets its own purity check. Stacked bottles supplied by other mods are separated into one opened bottle plus sealed inventory extras before sipping. Legacy player-wide fraction data is ignored because it cannot be safely attributed to a physical bottle.

The optional Better Content Threads bridge begins `water_made_safe` on the
first observed thirst decrease. It persists that exact episode across reloads
and completes only after the player actually drinks purity-3 water, whether
from a consumed bottle, the water-bottle Curio, or a safe Rain Collector.
Servers without Threads continue to run without a hard dependency.
