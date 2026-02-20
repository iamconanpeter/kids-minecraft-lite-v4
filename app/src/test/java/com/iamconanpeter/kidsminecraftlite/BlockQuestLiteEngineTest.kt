package com.iamconanpeter.kidsminecraftlite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockQuestLiteEngineTest {

    @Test
    fun dayNightCycleTransitionsAndResets() {
        val engine = BlockQuestLiteEngine()
        val firstDay = engine.currentState().dayNumber

        repeat(82) { engine.tick() }
        assertEquals(BlockQuestLitePhase.DUSK, engine.currentState().phase)

        repeat(20) { engine.tick() }
        assertEquals(BlockQuestLitePhase.NIGHT, engine.currentState().phase)

        repeat(60) { engine.tick() }
        assertEquals(BlockQuestLitePhase.DAWN, engine.currentState().phase)

        repeat(20) { engine.tick() }
        assertEquals(BlockQuestLitePhase.DAY, engine.currentState().phase)
        assertEquals(firstDay + 1, engine.currentState().dayNumber)
    }

    @Test
    fun mineAndPlaceLoopChangesInventoryAndWorld() {
        val engine = BlockQuestLiteEngine()
        val beforeWood = engine.getInventory(BlockQuestLiteItem.WOOD)

        val mined = engine.mineBlock(1, BlockQuestLiteEngine.HEIGHT - 4)
        assertTrue(mined)
        assertEquals(beforeWood + 1, engine.getInventory(BlockQuestLiteItem.WOOD))

        engine.toggleMode() // place mode
        val targetX = 2
        val targetY = 2

        // cycle until wood is selected
        repeat(10) {
            if (engine.currentPlaceItem() != BlockQuestLiteItem.WOOD) {
                engine.cyclePlaceItem()
            }
        }

        val placed = engine.placeBlock(targetX, targetY)
        assertTrue(placed)
        assertEquals(BlockQuestLiteBlock.WOOD, engine.getBlock(targetX, targetY))
    }

    @Test
    fun craftingConsumesInputsAndProducesOutput() {
        val engine = BlockQuestLiteEngine()
        engine.debugGrant(BlockQuestLiteItem.WOOD, 4)

        val crafted = engine.craft("plank_bundle")
        assertTrue(crafted)
        assertTrue(engine.getInventory(BlockQuestLiteItem.PLANK) >= 2)

        val failCraft = engine.craft("pickaxe")
        assertFalse(failCraft) // no enough stone yet (or stars)
    }

    @Test
    fun nightPressureAndBossEventAffectState() {
        val engine = BlockQuestLiteEngine()
        val initialHearts = engine.currentState().hearts

        repeat(520) { engine.tick() }
        val after = engine.currentState()

        assertTrue(after.skyWyrmEvents >= 1)
        assertTrue(after.boomSproutThreat >= 1)
        assertTrue(after.hearts <= initialHearts)
    }

    @Test
    fun shelterScoreImprovesWithEnclosureAndSaveRoundTripWorks() {
        val engine = BlockQuestLiteEngine()
        val before = engine.currentState().shelterScore

        val ax = BlockQuestLiteEngine.WIDTH / 2
        val ay = 2
        engine.debugGrant(BlockQuestLiteItem.WOOD, 10)
        engine.toggleMode()
        while (engine.currentPlaceItem() != BlockQuestLiteItem.WOOD) {
            engine.cyclePlaceItem()
        }

        assertTrue(engine.placeBlock(ax - 1, ay))
        assertTrue(engine.placeBlock(ax + 1, ay))
        assertTrue(engine.placeBlock(ax, ay - 1))
        assertTrue(engine.placeBlock(ax, ay + 1))

        val after = engine.currentState().shelterScore
        assertTrue(after > before)

        val payload = engine.toSavePayload()
        val restored = BlockQuestLiteEngine(payload)
        assertEquals(engine.currentState().dayNumber, restored.currentState().dayNumber)
        assertEquals(engine.currentState().shelterScore, restored.currentState().shelterScore)
        assertNotEquals("", payload)
    }

    @Test
    fun onboardingProgressTracksAndPersists() {
        val engine = BlockQuestLiteEngine()

        assertEquals(0, engine.currentState().blocksMined)
        assertEquals(0, engine.currentState().blocksPlaced)
        assertFalse(engine.currentState().onboardingShelterBuilt)

        assertTrue(engine.mineBlock(1, BlockQuestLiteEngine.HEIGHT - 4))

        engine.toggleMode()
        while (engine.currentPlaceItem() != BlockQuestLiteItem.WOOD) {
            engine.cyclePlaceItem()
        }
        engine.debugGrant(BlockQuestLiteItem.WOOD, 4)
        assertTrue(engine.placeBlock(2, 2))

        val ax = BlockQuestLiteEngine.WIDTH / 2
        val ay = 2
        engine.debugSetBlock(ax - 1, ay, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax, ay - 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax, ay + 1, BlockQuestLiteBlock.WOOD)

        val progressed = engine.currentState()
        assertTrue(progressed.blocksMined >= 1)
        assertTrue(progressed.blocksPlaced >= 1)
        assertTrue(progressed.onboardingShelterBuilt)

        val payload = engine.toSavePayload()
        val restored = BlockQuestLiteEngine(payload).currentState()
        assertEquals(progressed.blocksMined, restored.blocksMined)
        assertEquals(progressed.blocksPlaced, restored.blocksPlaced)
        assertTrue(restored.onboardingShelterBuilt)
    }
}
