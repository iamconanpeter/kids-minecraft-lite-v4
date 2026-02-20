package com.iamconanpeter.kidsminecraftlite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockQuestLiteEngineTest {

    @Test
    fun dayNightCycleTransitionsDeterministically() {
        val engine = BlockQuestLiteEngine()
        repeat(BlockQuestLiteEngine.DAY_TICKS) { engine.tick() }

        assertEquals(BlockQuestLiteEngine.DayPhase.NIGHT, engine.phase)
        assertEquals(1, engine.nightsSurvived)

        repeat(BlockQuestLiteEngine.NIGHT_TICKS) { engine.tick() }

        assertEquals(BlockQuestLiteEngine.DayPhase.DAY, engine.phase)
        assertTrue(engine.stars >= 1)
    }

    @Test
    fun mineAndPlaceLoopChangesInventoryAndWorld() {
        val engine = BlockQuestLiteEngine()
        val before = engine.snapshot().inventory[BlockQuestLiteEngine.ItemType.DIRT] ?: 0

        engine.setMode(BlockQuestLiteEngine.InputMode.PLACE)
        assertTrue(engine.placeTile(0, 0))

        val afterPlace = engine.snapshot().inventory[BlockQuestLiteEngine.ItemType.DIRT] ?: 0
        assertEquals(before - 1, afterPlace)

        engine.setMode(BlockQuestLiteEngine.InputMode.MINE)
        assertTrue(engine.mineTile(0, 0))

        val afterMine = engine.snapshot().inventory[BlockQuestLiteEngine.ItemType.DIRT] ?: 0
        assertTrue(afterMine >= afterPlace)
        assertEquals(BlockQuestLiteEngine.BlockType.AIR, engine.blockAt(0, 0))
    }

    @Test
    fun craftingValidatesCostsAndCanUnlockPickaxe() {
        val engine = BlockQuestLiteEngine()

        assertFalse(engine.craft("stone_pick")) // starts with only 1 stone

        assertTrue(engine.mineTile(0, 6)) // stone row
        assertTrue(engine.craft("stone_pick"))
        assertTrue(engine.hasPickaxe)
    }

    @Test
    fun shelterScoreIncreasesWhenEnclosed() {
        val engine = BlockQuestLiteEngine()
        engine.setMode(BlockQuestLiteEngine.InputMode.PLACE)

        assertTrue(engine.placeTile(1, 4))
        assertTrue(engine.placeTile(3, 4))
        assertTrue(engine.placeTile(2, 3))
        assertTrue(engine.placeTile(1, 3))

        assertTrue(engine.shelterScore() >= BlockQuestLiteEngine.SHELTER_SAFE_THRESHOLD)
        assertTrue(engine.isSheltered())
    }

    @Test
    fun nightChaserDamagesWhenUnsheltered() {
        val engine = BlockQuestLiteEngine()
        val startingHearts = engine.hearts

        repeat(BlockQuestLiteEngine.DAY_TICKS) { engine.tick() } // enter night
        repeat(21) { engine.tick() } // enough for first chaser contact, before dawn heal

        assertTrue(engine.hearts < startingHearts || engine.rescueCount > 0)
    }

    @Test
    fun thirdNightTriggersBossEvent() {
        val engine = BlockQuestLiteEngine()
        var guard = 0

        while (engine.nightsSurvived < 3 && guard < 1000) {
            engine.tick()
            guard++
        }

        assertEquals(3, engine.nightsSurvived)
        assertTrue(engine.snapshot().bossActive)
    }

    @Test
    fun craftingBeforeSunriseAddsExtraStarReward() {
        val engine = BlockQuestLiteEngine()
        assertTrue(engine.craft("plank_bundle"))

        repeat(BlockQuestLiteEngine.DAY_TICKS + BlockQuestLiteEngine.NIGHT_TICKS) { engine.tick() }

        assertTrue(engine.stars >= 2)
    }
}
