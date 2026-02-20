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
        assertFalse(failCraft)
    }

    @Test
    fun shelterEvaluationReflectsSafetyAndLightQuality() {
        val engine = BlockQuestLiteEngine()
        val ax = BlockQuestLiteEngine.WIDTH / 2
        val ay = 2

        val before = engine.evaluateShelter()
        assertTrue(before.score < 60)

        engine.debugSetBlock(ax - 1, ay, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax, ay - 1, BlockQuestLiteBlock.PLANK)
        engine.debugSetBlock(ax, ay + 1, BlockQuestLiteBlock.PLANK)
        engine.debugSetBlock(ax - 1, ay - 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay - 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax - 1, ay + 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay + 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax, ay - 2, BlockQuestLiteBlock.TORCH)

        val after = engine.evaluateShelter()
        assertTrue(after.enclosureSafety > before.enclosureSafety)
        assertTrue(after.lightQuality >= before.lightQuality)
        assertTrue(after.score > before.score)
    }

    @Test
    fun tunedNightPressureIsGentlerWithEasyModeAndGrace() {
        val engine = BlockQuestLiteEngine()

        val braveChance = engine.computeNightPressureChance(
            shelterScore = 45,
            dayNumber = 2,
            easyMode = false,
            bossEventActive = false,
            boomSproutThreat = 2,
            adaptiveGraceNights = 0
        )
        val easyChance = engine.computeNightPressureChance(
            shelterScore = 45,
            dayNumber = 2,
            easyMode = true,
            bossEventActive = false,
            boomSproutThreat = 2,
            adaptiveGraceNights = 0
        )
        val graceChance = engine.computeNightPressureChance(
            shelterScore = 45,
            dayNumber = 2,
            easyMode = true,
            bossEventActive = false,
            boomSproutThreat = 2,
            adaptiveGraceNights = 2
        )

        assertTrue(easyChance < braveChance)
        assertTrue(graceChance < easyChance)
    }

    @Test
    fun buddyTrustMethodsAdjustMeterAndHintBands() {
        val engine = BlockQuestLiteEngine()

        assertEquals(0, engine.computeBuddyHintCharges(10))
        assertEquals(1, engine.computeBuddyHintCharges(50))
        assertEquals(2, engine.computeBuddyHintCharges(90))

        val before = engine.currentState().buddyTrust
        val raised = engine.adjustBuddyTrust(12)
        assertEquals(before + 12, raised)

        val lowered = engine.adjustBuddyTrust(-200)
        assertEquals(0, lowered)
    }

    @Test
    fun repeatedRescuesActivateAdaptiveGrace() {
        val engine = BlockQuestLiteEngine()

        engine.debugForceRescue()
        assertEquals(0, engine.currentState().adaptiveGraceNights)

        engine.debugForceRescue()
        val afterSecond = engine.currentState().adaptiveGraceNights
        assertTrue(afterSecond >= 1)

        engine.debugForceRescue()
        assertTrue(engine.currentState().adaptiveGraceNights >= afterSecond)
    }

    @Test
    fun buddyTrustAffectsSunriseRewards() {
        val lowTrust = BlockQuestLiteEngine()
        val highTrust = BlockQuestLiteEngine()
        fortifyShelter(lowTrust)
        fortifyShelter(highTrust)

        lowTrust.adjustBuddyTrust(-100)
        highTrust.adjustBuddyTrust(100)

        repeat(180) { lowTrust.tick() }
        repeat(180) { highTrust.tick() }

        assertEquals(1, lowTrust.currentState().stars)
        assertTrue(highTrust.currentState().stars >= 2)
    }

    @Test
    fun saveRoundTripPersistsNewFields() {
        val engine = BlockQuestLiteEngine()
        fortifyShelter(engine)
        engine.adjustBuddyTrust(30)
        engine.debugForceRescue()
        engine.debugForceRescue()

        val payload = engine.toSavePayload()
        val restored = BlockQuestLiteEngine(payload).currentState()
        val current = engine.currentState()

        assertEquals(current.buddyTrust, restored.buddyTrust)
        assertEquals(current.adaptiveGraceNights, restored.adaptiveGraceNights)
        assertEquals(current.shelterScore, restored.shelterScore)
        assertNotEquals("", payload)
    }

    private fun fortifyShelter(engine: BlockQuestLiteEngine) {
        val ax = BlockQuestLiteEngine.WIDTH / 2
        val ay = 2
        engine.debugSetBlock(ax - 1, ay, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax, ay - 1, BlockQuestLiteBlock.PLANK)
        engine.debugSetBlock(ax, ay + 1, BlockQuestLiteBlock.PLANK)
        engine.debugSetBlock(ax - 1, ay - 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay - 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax - 1, ay + 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax + 1, ay + 1, BlockQuestLiteBlock.WOOD)
        engine.debugSetBlock(ax, ay - 2, BlockQuestLiteBlock.TORCH)
    }
}
