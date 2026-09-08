package com.bettercontent.watersurvival;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WaterThreadEventsTest {
    @Test void onlyAnActualThirstDecreaseRevealsTheLesson() {
        assertTrue(WaterThreadEvents.revealsOnDrop(20, 19));
        assertTrue(WaterThreadEvents.revealsOnDrop(7, 6));
        assertFalse(WaterThreadEvents.revealsOnDrop(19, 19));
        assertFalse(WaterThreadEvents.revealsOnDrop(10, 14));
        assertFalse(WaterThreadEvents.revealsOnDrop(0, -1));
    }

    @Test void completionRequiresTheExactMaximumPurityTier() {
        assertTrue(WaterThreadEvents.isSafePurity(3, 3));
        assertFalse(WaterThreadEvents.isSafePurity(2, 3));
        assertFalse(WaterThreadEvents.isSafePurity(4, 3));
    }
}
