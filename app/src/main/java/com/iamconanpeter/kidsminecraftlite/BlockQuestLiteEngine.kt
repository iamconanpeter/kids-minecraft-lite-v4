package com.iamconanpeter.kidsminecraftlite

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class BlockQuestLiteBlock {
    AIR,
    DIRT,
    WOOD,
    STONE,
    PLANK,
    TORCH,
    CRYSTAL
}

enum class BlockQuestLiteItem {
    DIRT,
    WOOD,
    STONE,
    PLANK,
    TORCH,
    CRYSTAL,
    PICKAXE
}

enum class BlockQuestLiteInputMode {
    MINE,
    PLACE
}

enum class BlockQuestLitePhase {
    DAY,
    DUSK,
    NIGHT,
    DAWN
}

enum class BlockQuestLiteStatusTone {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    DANGER
}

data class BlockQuestLiteRecipe(
    val id: String,
    val icon: String,
    val inputs: Map<BlockQuestLiteItem, Int>,
    val output: Pair<BlockQuestLiteItem, Int>,
    val starsRequired: Int = 0
)

data class BlockQuestLiteShelterEvaluation(
    val enclosureSafety: Int,
    val lightQuality: Int,
    val score: Int,
    val openings: Int
)

data class BlockQuestLiteState(
    val width: Int,
    val height: Int,
    val world: IntArray,
    val inventory: Map<BlockQuestLiteItem, Int>,
    val mode: BlockQuestLiteInputMode,
    val selectedPlaceIndex: Int,
    val hearts: Int,
    val stars: Int,
    val unlockTier: Int,
    val dayNumber: Int,
    val cycleTick: Int,
    val phase: BlockQuestLitePhase,
    val easyMode: Boolean,
    val shelterScore: Int,
    val shelterSafety: Int,
    val shelterLightQuality: Int,
    val craftedThisCycle: Boolean,
    val bossEventActive: Boolean,
    val nightsSurvived: Int,
    val boomSproutThreat: Int,
    val glowmewGiftMoments: Int,
    val skyWyrmEvents: Int,
    val rescuedCount: Int,
    val blocksMined: Int,
    val blocksPlaced: Int,
    val onboardingShelterBuilt: Boolean,
    val buddyTrust: Int,
    val buddyHintCharges: Int,
    val adaptiveGraceNights: Int,
    val statusTone: BlockQuestLiteStatusTone,
    val statusMessage: String
)

class BlockQuestLiteEngine(savedPayload: String? = null) {
    companion object {
        const val WIDTH = 10
        const val HEIGHT = 8

        private const val DAY_TICKS = 80
        private const val DUSK_TICKS = 20
        private const val NIGHT_TICKS = 60
        private const val DAWN_TICKS = 20
        private const val CYCLE_TICKS = DAY_TICKS + DUSK_TICKS + NIGHT_TICKS + DAWN_TICKS

        private const val MAX_HEARTS = 5
        private const val MAX_TRUST = 100
        private const val MIN_TRUST = 0

        private val PLACEABLE_ITEMS = listOf(
            BlockQuestLiteItem.DIRT,
            BlockQuestLiteItem.WOOD,
            BlockQuestLiteItem.STONE,
            BlockQuestLiteItem.PLANK,
            BlockQuestLiteItem.TORCH
        )

        val RECIPES: List<BlockQuestLiteRecipe> = listOf(
            BlockQuestLiteRecipe(
                id = "plank_bundle",
                icon = "🪵→🟫",
                inputs = mapOf(BlockQuestLiteItem.WOOD to 2),
                output = BlockQuestLiteItem.PLANK to 2
            ),
            BlockQuestLiteRecipe(
                id = "torch_pair",
                icon = "🟫+💎→🔥",
                inputs = mapOf(
                    BlockQuestLiteItem.PLANK to 1,
                    BlockQuestLiteItem.CRYSTAL to 1
                ),
                output = BlockQuestLiteItem.TORCH to 2
            ),
            BlockQuestLiteRecipe(
                id = "stone_press",
                icon = "🟫+🪨→⛰️",
                inputs = mapOf(
                    BlockQuestLiteItem.DIRT to 2,
                    BlockQuestLiteItem.WOOD to 1
                ),
                output = BlockQuestLiteItem.STONE to 1
            ),
            BlockQuestLiteRecipe(
                id = "pickaxe",
                icon = "⛰️+🪵→⛏️",
                inputs = mapOf(
                    BlockQuestLiteItem.STONE to 2,
                    BlockQuestLiteItem.WOOD to 1
                ),
                output = BlockQuestLiteItem.PICKAXE to 1,
                starsRequired = 1
            )
        )

        fun phaseForTick(cycleTick: Int): BlockQuestLitePhase {
            return when {
                cycleTick < DAY_TICKS -> BlockQuestLitePhase.DAY
                cycleTick < DAY_TICKS + DUSK_TICKS -> BlockQuestLitePhase.DUSK
                cycleTick < DAY_TICKS + DUSK_TICKS + NIGHT_TICKS -> BlockQuestLitePhase.NIGHT
                else -> BlockQuestLitePhase.DAWN
            }
        }

        private fun toItem(block: BlockQuestLiteBlock): BlockQuestLiteItem? {
            return when (block) {
                BlockQuestLiteBlock.DIRT -> BlockQuestLiteItem.DIRT
                BlockQuestLiteBlock.WOOD -> BlockQuestLiteItem.WOOD
                BlockQuestLiteBlock.STONE -> BlockQuestLiteItem.STONE
                BlockQuestLiteBlock.PLANK -> BlockQuestLiteItem.PLANK
                BlockQuestLiteBlock.TORCH -> BlockQuestLiteItem.TORCH
                BlockQuestLiteBlock.CRYSTAL -> BlockQuestLiteItem.CRYSTAL
                BlockQuestLiteBlock.AIR -> null
            }
        }

        private fun toBlock(item: BlockQuestLiteItem): BlockQuestLiteBlock? {
            return when (item) {
                BlockQuestLiteItem.DIRT -> BlockQuestLiteBlock.DIRT
                BlockQuestLiteItem.WOOD -> BlockQuestLiteBlock.WOOD
                BlockQuestLiteItem.STONE -> BlockQuestLiteBlock.STONE
                BlockQuestLiteItem.PLANK -> BlockQuestLiteBlock.PLANK
                BlockQuestLiteItem.TORCH -> BlockQuestLiteBlock.TORCH
                BlockQuestLiteItem.CRYSTAL,
                BlockQuestLiteItem.PICKAXE -> null
            }
        }
    }

    private val world = IntArray(WIDTH * HEIGHT) { BlockQuestLiteBlock.AIR.ordinal }
    private val inventory = mutableMapOf<BlockQuestLiteItem, Int>()

    private var mode = BlockQuestLiteInputMode.MINE
    private var selectedPlaceIndex = 0

    private var hearts = MAX_HEARTS
    private var stars = 0
    private var unlockTier = 1

    private var dayNumber = 1
    private var cycleTick = 0
    private var phase = BlockQuestLitePhase.DAY
    private var easyMode = true

    private var shelterScore = 0
    private var shelterSafety = 0
    private var shelterLightQuality = 0

    private var craftedThisCycle = false
    private var bossEventActive = false
    private var bossRewardPending = false

    private var nightsSurvived = 0
    private var boomSproutThreat = 0
    private var glowmewGiftMoments = 0
    private var skyWyrmEvents = 0
    private var rescuedCount = 0

    private var blocksMined = 0
    private var blocksPlaced = 0
    private var onboardingShelterBuilt = false

    private var buddyTrust = 26
    private var buddyHintCharges = 0
    private var consecutiveRescues = 0
    private var adaptiveGraceNights = 0
    private var graceShieldCharges = 0
    private var rescuedThisCycle = false

    private var giftCooldownTicks = 20
    private var nightDamageCooldown = 12
    private var statusTone = BlockQuestLiteStatusTone.INFO
    private var statusMessage = "⛏️"

    init {
        generateInitialWorld()
        if (!savedPayload.isNullOrBlank()) {
            if (!restore(savedPayload)) {
                setStatus("🗺️✨", BlockQuestLiteStatusTone.INFO)
            }
        }
        refreshShelter()
        buddyHintCharges = max(buddyHintCharges, computeBuddyHintCharges())
    }

    fun recipes(): List<BlockQuestLiteRecipe> = RECIPES

    fun currentPlaceItem(): BlockQuestLiteItem = PLACEABLE_ITEMS[selectedPlaceIndex]

    fun currentState(): BlockQuestLiteState {
        return BlockQuestLiteState(
            width = WIDTH,
            height = HEIGHT,
            world = world.copyOf(),
            inventory = inventory.toMap(),
            mode = mode,
            selectedPlaceIndex = selectedPlaceIndex,
            hearts = hearts,
            stars = stars,
            unlockTier = unlockTier,
            dayNumber = dayNumber,
            cycleTick = cycleTick,
            phase = phase,
            easyMode = easyMode,
            shelterScore = shelterScore,
            shelterSafety = shelterSafety,
            shelterLightQuality = shelterLightQuality,
            craftedThisCycle = craftedThisCycle,
            bossEventActive = bossEventActive,
            nightsSurvived = nightsSurvived,
            boomSproutThreat = boomSproutThreat,
            glowmewGiftMoments = glowmewGiftMoments,
            skyWyrmEvents = skyWyrmEvents,
            rescuedCount = rescuedCount,
            blocksMined = blocksMined,
            blocksPlaced = blocksPlaced,
            onboardingShelterBuilt = onboardingShelterBuilt,
            buddyTrust = buddyTrust,
            buddyHintCharges = buddyHintCharges,
            adaptiveGraceNights = adaptiveGraceNights,
            statusTone = statusTone,
            statusMessage = statusMessage
        )
    }

    fun getBlock(x: Int, y: Int): BlockQuestLiteBlock {
        if (!inBounds(x, y)) return BlockQuestLiteBlock.AIR
        return BlockQuestLiteBlock.values()[world[idx(x, y)]]
    }

    fun getInventory(item: BlockQuestLiteItem): Int = inventory[item] ?: 0

    fun toggleMode() {
        mode = if (mode == BlockQuestLiteInputMode.MINE) {
            BlockQuestLiteInputMode.PLACE
        } else {
            BlockQuestLiteInputMode.MINE
        }
        setStatus(if (mode == BlockQuestLiteInputMode.MINE) "⛏️" else "🧱", BlockQuestLiteStatusTone.INFO)
    }

    fun cyclePlaceItem() {
        selectedPlaceIndex = (selectedPlaceIndex + 1) % PLACEABLE_ITEMS.size
        setStatus("🎒 ${currentPlaceItem().name.lowercase()}", BlockQuestLiteStatusTone.INFO)
    }

    fun toggleEasyMode() {
        easyMode = !easyMode
        setStatus(if (easyMode) "🙂🌙" else "🔥🌙", BlockQuestLiteStatusTone.INFO)
    }

    fun tapTile(x: Int, y: Int): Boolean {
        return if (mode == BlockQuestLiteInputMode.MINE) {
            mineBlock(x, y)
        } else {
            placeBlock(x, y)
        }
    }

    fun mineBlock(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        val current = getBlock(x, y)
        if (current == BlockQuestLiteBlock.AIR) {
            setStatus("❔", BlockQuestLiteStatusTone.ERROR)
            return false
        }

        world[idx(x, y)] = BlockQuestLiteBlock.AIR.ordinal

        val item = toItem(current)
        if (item != null) {
            addInventory(item, 1)
            blocksMined += 1
            if (current == BlockQuestLiteBlock.STONE && ((x + y + dayNumber + cycleTick) % 5 == 0)) {
                addInventory(BlockQuestLiteItem.CRYSTAL, 1)
                setStatus("💎", BlockQuestLiteStatusTone.SUCCESS)
            } else {
                setStatus("⛏️+1", BlockQuestLiteStatusTone.SUCCESS)
            }
        }

        refreshShelter()
        updateOnboardingShelterProgress()
        return true
    }

    fun placeBlock(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        if (getBlock(x, y) != BlockQuestLiteBlock.AIR) {
            setStatus("🚫", BlockQuestLiteStatusTone.ERROR)
            return false
        }

        val selectedItem = currentPlaceItem()
        val selectedBlock = toBlock(selectedItem)
        if (selectedBlock == null) {
            setStatus("🚫", BlockQuestLiteStatusTone.ERROR)
            return false
        }

        if (getInventory(selectedItem) <= 0) {
            setStatus("📦❌", BlockQuestLiteStatusTone.ERROR)
            return false
        }

        world[idx(x, y)] = selectedBlock.ordinal
        addInventory(selectedItem, -1)
        blocksPlaced += 1

        if (abs(x - WIDTH / 2) <= 2 && abs(y - 2) <= 2 && isStructural(selectedBlock)) {
            adjustBuddyTrust(1)
        }

        refreshShelter()
        updateOnboardingShelterProgress()
        setStatus("🧱✅", BlockQuestLiteStatusTone.SUCCESS)
        return true
    }

    fun craft(recipeId: String): Boolean {
        val recipe = RECIPES.firstOrNull { it.id == recipeId }
            ?: return false

        if (stars < recipe.starsRequired) {
            setStatus("🔒 ${recipe.starsRequired}⭐", BlockQuestLiteStatusTone.ERROR)
            return false
        }

        for ((item, amount) in recipe.inputs) {
            if (getInventory(item) < amount) {
                setStatus("📦❌", BlockQuestLiteStatusTone.ERROR)
                return false
            }
        }

        recipe.inputs.forEach { (item, amount) ->
            addInventory(item, -amount)
        }
        addInventory(recipe.output.first, recipe.output.second)

        craftedThisCycle = true
        adjustBuddyTrust(3)
        setStatus("🧪✅", BlockQuestLiteStatusTone.SUCCESS)
        return true
    }

    fun tick() {
        val previousPhase = phase

        cycleTick += 1
        if (cycleTick >= CYCLE_TICKS) {
            cycleTick = 0
            dayNumber += 1
            onSunrise()
        }

        phase = phaseForTick(cycleTick)

        if (previousPhase != phase) {
            when (phase) {
                BlockQuestLitePhase.DUSK -> {
                    setStatus("🌆🏠", BlockQuestLiteStatusTone.WARNING)
                    deliverBuddyHint("dusk")
                }

                BlockQuestLitePhase.NIGHT -> startNightPhase()
                BlockQuestLitePhase.DAWN -> setStatus("🌤️", BlockQuestLiteStatusTone.INFO)
                BlockQuestLitePhase.DAY -> setStatus("☀️✨", BlockQuestLiteStatusTone.SUCCESS)
            }
        }

        if (phase == BlockQuestLitePhase.DAY) {
            processGlowmewGift()
        }

        if (phase == BlockQuestLitePhase.NIGHT) {
            processNightPressure()
        }

        refreshShelter()
        updateOnboardingShelterProgress()
    }

    fun toSavePayload(): String {
        val worldString = world.joinToString(",")
        val inventoryString = BlockQuestLiteItem.values().joinToString(",") {
            "${it.name}:${getInventory(it)}"
        }
        val scalar = listOf(
            mode.name,
            selectedPlaceIndex,
            hearts,
            stars,
            unlockTier,
            dayNumber,
            cycleTick,
            phase.name,
            easyMode,
            shelterScore,
            craftedThisCycle,
            bossEventActive,
            bossRewardPending,
            nightsSurvived,
            boomSproutThreat,
            glowmewGiftMoments,
            skyWyrmEvents,
            rescuedCount,
            giftCooldownTicks,
            nightDamageCooldown,
            blocksMined,
            blocksPlaced,
            onboardingShelterBuilt,
            buddyTrust,
            buddyHintCharges,
            consecutiveRescues,
            adaptiveGraceNights,
            rescuedThisCycle,
            shelterSafety,
            shelterLightQuality,
            statusTone.name
        ).joinToString(",")

        return listOf(worldString, inventoryString, scalar).joinToString(";")
    }

    internal fun debugGrant(item: BlockQuestLiteItem, amount: Int) {
        addInventory(item, amount)
    }

    internal fun debugSetBlock(x: Int, y: Int, block: BlockQuestLiteBlock) {
        if (!inBounds(x, y)) return
        world[idx(x, y)] = block.ordinal
        refreshShelter()
        updateOnboardingShelterProgress()
    }

    internal fun debugForceRescue() {
        rescuePlayer()
    }

    internal fun evaluateShelter(anchorX: Int = WIDTH / 2, anchorY: Int = 2): BlockQuestLiteShelterEvaluation {
        val cardinal = listOf(
            anchorX - 1 to anchorY,
            anchorX + 1 to anchorY,
            anchorX to anchorY - 1,
            anchorX to anchorY + 1
        )
        val diagonals = listOf(
            anchorX - 1 to anchorY - 1,
            anchorX + 1 to anchorY - 1,
            anchorX - 1 to anchorY + 1,
            anchorX + 1 to anchorY + 1
        )

        var safety = 0
        cardinal.forEach { (x, y) ->
            val block = getBlock(x, y)
            safety += when {
                isStructural(block) -> 15
                block == BlockQuestLiteBlock.TORCH -> 7
                else -> -4
            }
        }
        diagonals.forEach { (x, y) ->
            val block = getBlock(x, y)
            safety += when {
                isStructural(block) -> 6
                block == BlockQuestLiteBlock.TORCH -> 2
                else -> -2
            }
        }

        var floorSupport = 0
        for (x in anchorX - 1..anchorX + 1) {
            val block = getBlock(x, anchorY + 2)
            floorSupport += when {
                isStructural(block) -> 6
                block == BlockQuestLiteBlock.TORCH -> 2
                else -> 0
            }
        }
        safety += floorSupport

        var roofSupport = 0
        for (x in anchorX - 1..anchorX + 1) {
            val block = getBlock(x, anchorY - 1)
            roofSupport += when {
                isStructural(block) -> 7
                block == BlockQuestLiteBlock.TORCH -> 3
                else -> 0
            }
        }
        safety += roofSupport

        val openings = (cardinal + diagonals).count { (x, y) -> getBlock(x, y) == BlockQuestLiteBlock.AIR }
        safety -= openings * 5

        val skyExposure = (anchorX - 1..anchorX + 1).count { x ->
            isOpenToSky(x, anchorY - 1)
        }
        safety -= skyExposure * 6

        val enclosureSafety = safety.coerceIn(0, 100)

        var light = 0
        for (dx in -2..2) {
            for (dy in -2..2) {
                val x = anchorX + dx
                val y = anchorY + dy
                if (!inBounds(x, y)) continue
                if (getBlock(x, y) == BlockQuestLiteBlock.TORCH) {
                    val dist = abs(dx) + abs(dy)
                    light += when (dist) {
                        0 -> 20
                        1 -> 16
                        2 -> 12
                        3 -> 8
                        else -> 5
                    }
                }
            }
        }
        val lightPenalty = openings * 2 + skyExposure * 2
        var lightQuality = (light - lightPenalty).coerceIn(0, 100)
        if (enclosureSafety < 30) {
            lightQuality = min(lightQuality, 40)
        }

        var score = (enclosureSafety * 0.72f + lightQuality * 0.28f).toInt()
        if (enclosureSafety < 35) {
            score = min(score, 58)
        }
        score = score.coerceIn(0, 100)

        return BlockQuestLiteShelterEvaluation(
            enclosureSafety = enclosureSafety,
            lightQuality = lightQuality,
            score = score,
            openings = openings
        )
    }

    internal fun computeNightPressureChance(
        shelterScore: Int = this.shelterScore,
        dayNumber: Int = this.dayNumber,
        easyMode: Boolean = this.easyMode,
        bossEventActive: Boolean = this.bossEventActive,
        boomSproutThreat: Int = this.boomSproutThreat,
        adaptiveGraceNights: Int = this.adaptiveGraceNights
    ): Int {
        val base = when {
            shelterScore >= 85 -> 8
            shelterScore >= 70 -> 14
            shelterScore >= 55 -> 22
            shelterScore >= 40 -> 30
            else -> 40
        }

        val dayPressure = ((dayNumber - 1) / 3) * 4
        val threatBonus = min(18, boomSproutThreat * 2)
        val bossBonus = if (bossEventActive) if (easyMode) 8 else 14 else 0
        val easyRelief = if (easyMode) 14 else 0
        val earlyChildRelief = if (easyMode && dayNumber <= 2) 8 else 0
        val graceRelief = adaptiveGraceNights * 12

        return (base + dayPressure + threatBonus + bossBonus - easyRelief - earlyChildRelief - graceRelief)
            .coerceIn(3, 90)
    }

    internal fun computeBuddyHintCharges(trust: Int = buddyTrust): Int {
        return when {
            trust >= 80 -> 2
            trust >= 45 -> 1
            else -> 0
        }
    }

    internal fun applyAdaptiveGraceAfterRescue(rescueStreak: Int = consecutiveRescues): Int {
        if (rescueStreak >= 2) {
            adaptiveGraceNights = max(adaptiveGraceNights, min(3, rescueStreak - 1))
        }
        return adaptiveGraceNights
    }

    internal fun adjustBuddyTrust(delta: Int): Int {
        buddyTrust = (buddyTrust + delta).coerceIn(MIN_TRUST, MAX_TRUST)
        return buddyTrust
    }

    private fun restore(payload: String): Boolean {
        return try {
            val parts = payload.split(";")
            if (parts.size < 3) return false

            val worldValues = parts[0].split(",")
            if (worldValues.size != WIDTH * HEIGHT) return false
            worldValues.forEachIndexed { index, value ->
                val ordinal = value.toIntOrNull() ?: 0
                world[index] = ordinal.coerceIn(0, BlockQuestLiteBlock.values().size - 1)
            }

            inventory.clear()
            parts[1].split(",").forEach { token ->
                val kv = token.split(":")
                if (kv.size == 2) {
                    val item = runCatching { BlockQuestLiteItem.valueOf(kv[0]) }.getOrNull()
                    val amount = kv[1].toIntOrNull() ?: 0
                    if (item != null && amount > 0) inventory[item] = amount
                }
            }

            val s = parts[2].split(",")
            if (s.size < 20) return false

            mode = BlockQuestLiteInputMode.valueOf(s[0])
            selectedPlaceIndex = (s[1].toIntOrNull() ?: 0).coerceIn(0, PLACEABLE_ITEMS.lastIndex)
            hearts = (s[2].toIntOrNull() ?: MAX_HEARTS).coerceIn(1, MAX_HEARTS)
            stars = max(0, s[3].toIntOrNull() ?: 0)
            unlockTier = max(1, s[4].toIntOrNull() ?: 1)
            dayNumber = max(1, s[5].toIntOrNull() ?: 1)
            cycleTick = (s[6].toIntOrNull() ?: 0).coerceIn(0, CYCLE_TICKS - 1)
            phase = BlockQuestLitePhase.valueOf(s[7])
            easyMode = s[8].toBooleanStrictOrNull() ?: true
            shelterScore = (s[9].toIntOrNull() ?: 0).coerceIn(0, 100)
            craftedThisCycle = s[10].toBooleanStrictOrNull() ?: false
            bossEventActive = s[11].toBooleanStrictOrNull() ?: false
            bossRewardPending = s[12].toBooleanStrictOrNull() ?: false
            nightsSurvived = max(0, s[13].toIntOrNull() ?: 0)
            boomSproutThreat = max(0, s[14].toIntOrNull() ?: 0)
            glowmewGiftMoments = max(0, s[15].toIntOrNull() ?: 0)
            skyWyrmEvents = max(0, s[16].toIntOrNull() ?: 0)
            rescuedCount = max(0, s[17].toIntOrNull() ?: 0)
            giftCooldownTicks = max(0, s[18].toIntOrNull() ?: 20)
            nightDamageCooldown = max(1, s[19].toIntOrNull() ?: 12)
            blocksMined = max(0, s.getOrNull(20)?.toIntOrNull() ?: 0)
            blocksPlaced = max(0, s.getOrNull(21)?.toIntOrNull() ?: 0)
            onboardingShelterBuilt = s.getOrNull(22)?.toBooleanStrictOrNull() ?: false
            buddyTrust = (s.getOrNull(23)?.toIntOrNull() ?: 26).coerceIn(MIN_TRUST, MAX_TRUST)
            buddyHintCharges = max(0, s.getOrNull(24)?.toIntOrNull() ?: 0)
            consecutiveRescues = max(0, s.getOrNull(25)?.toIntOrNull() ?: 0)
            adaptiveGraceNights = max(0, s.getOrNull(26)?.toIntOrNull() ?: 0)
            rescuedThisCycle = s.getOrNull(27)?.toBooleanStrictOrNull() ?: false
            shelterSafety = (s.getOrNull(28)?.toIntOrNull() ?: 0).coerceIn(0, 100)
            shelterLightQuality = (s.getOrNull(29)?.toIntOrNull() ?: 0).coerceIn(0, 100)
            statusTone = runCatching {
                BlockQuestLiteStatusTone.valueOf(s.getOrNull(30) ?: BlockQuestLiteStatusTone.INFO.name)
            }.getOrElse { BlockQuestLiteStatusTone.INFO }

            true
        } catch (_: Exception) {
            false
        }
    }

    private fun startNightPhase() {
        setStatus("🌙🌱", BlockQuestLiteStatusTone.WARNING)
        boomSproutThreat += if (shelterScore >= 78) 0 else 1

        graceShieldCharges = if (adaptiveGraceNights > 0) 1 else 0
        if (graceShieldCharges > 0) {
            setStatus("🛡️🌙", BlockQuestLiteStatusTone.INFO)
        }

        deliverBuddyHint("night")

        val isBossNight = dayNumber % 3 == 0
        if (isBossNight) {
            bossEventActive = true
            skyWyrmEvents += 1
            setStatus("🐉⚠️", BlockQuestLiteStatusTone.DANGER)
        }
    }

    private fun processGlowmewGift() {
        giftCooldownTicks -= 1
        if (giftCooldownTicks > 0) return

        val gift = if ((dayNumber + cycleTick + glowmewGiftMoments) % 3 == 0) {
            BlockQuestLiteItem.CRYSTAL
        } else {
            BlockQuestLiteItem.WOOD
        }
        addInventory(gift, 1)

        if (buddyTrust >= 70 && (glowmewGiftMoments + dayNumber) % 2 == 0) {
            addInventory(BlockQuestLiteItem.PLANK, 1)
        }

        glowmewGiftMoments += 1
        val trustBoost = buddyTrust / 30
        giftCooldownTicks = if (easyMode) max(14, 24 - trustBoost) else max(18, 30 - trustBoost)
        setStatus("🐾🎁", BlockQuestLiteStatusTone.SUCCESS)
    }

    private fun processNightPressure() {
        nightDamageCooldown -= 1
        if (nightDamageCooldown > 0) return

        val chance = computeNightPressureChance()
        val roll = (cycleTick * 7 + dayNumber * 13 + hearts * 5 + boomSproutThreat * 3 + buddyTrust) % 100

        if (roll < chance) {
            if (graceShieldCharges > 0) {
                graceShieldCharges -= 1
                setStatus("🛡️✨", BlockQuestLiteStatusTone.SUCCESS)
            } else {
                hearts -= 1
                boomSproutThreat += 1
                adjustBuddyTrust(-2)
                setStatus(
                    if (bossEventActive) "🐉💥" else "🌱💥",
                    if (hearts <= 2) BlockQuestLiteStatusTone.DANGER else BlockQuestLiteStatusTone.WARNING
                )
                if (hearts <= 0) {
                    rescuePlayer()
                }
            }
        } else if (chance >= 45) {
            setStatus("⚠️", BlockQuestLiteStatusTone.WARNING)
        }

        nightDamageCooldown = computeNightDamageCooldownTicks()
    }

    private fun onSunrise() {
        nightsSurvived += 1

        val craftedBeforeSunrise = craftedThisCycle
        var starsGained = 1
        if (craftedBeforeSunrise) starsGained += 1
        if (bossEventActive || bossRewardPending) starsGained += 1
        if (buddyTrust >= 70 && !rescuedThisCycle) starsGained += 1
        stars += starsGained

        if (buddyTrust >= 85 && !rescuedThisCycle) {
            addInventory(BlockQuestLiteItem.CRYSTAL, 1)
        }

        unlockTier = 1 + (stars / 4)

        craftedThisCycle = false
        bossRewardPending = bossEventActive
        bossEventActive = false

        boomSproutThreat = max(0, boomSproutThreat - 1)
        nightDamageCooldown = computeNightDamageCooldownTicks()

        if (adaptiveGraceNights > 0 && !rescuedThisCycle) {
            adaptiveGraceNights = max(0, adaptiveGraceNights - 1)
        }

        if (!rescuedThisCycle) {
            val trustGain = when {
                shelterScore >= 75 -> 5
                shelterScore >= 55 -> 3
                else -> 1
            } + if (craftedBeforeSunrise) 1 else 0
            adjustBuddyTrust(trustGain)
            consecutiveRescues = max(0, consecutiveRescues - 1)
        } else {
            rescuedThisCycle = false
        }

        buddyHintCharges = computeBuddyHintCharges()
        setStatus(if (starsGained >= 3) "🌅⭐🐾" else "🌅⭐", BlockQuestLiteStatusTone.SUCCESS)
    }

    private fun rescuePlayer() {
        rescuedCount += 1
        hearts = MAX_HEARTS
        stars = max(0, stars - 1)
        bossEventActive = false
        bossRewardPending = false
        rescuedThisCycle = true

        consecutiveRescues += 1
        adjustBuddyTrust(-12)
        val grace = applyAdaptiveGraceAfterRescue()
        if (grace > 0) {
            addInventory(BlockQuestLiteItem.TORCH, 1)
            buddyHintCharges = max(buddyHintCharges, 1)
        }

        setStatus("🛟🤍", BlockQuestLiteStatusTone.WARNING)
    }

    private fun refreshShelter() {
        val evaluation = evaluateShelter()
        shelterSafety = evaluation.enclosureSafety
        shelterLightQuality = evaluation.lightQuality
        shelterScore = evaluation.score
    }

    private fun deliverBuddyHint(reason: String) {
        if (buddyHintCharges <= 0) return

        when {
            reason == "night" && shelterScore < 55 && getInventory(BlockQuestLiteItem.TORCH) <= 0 && buddyTrust >= 45 -> {
                addInventory(BlockQuestLiteItem.TORCH, 1)
                buddyHintCharges -= 1
                setStatus("🐾🔥", BlockQuestLiteStatusTone.SUCCESS)
            }

            reason == "night" && shelterSafety < 55 -> {
                buddyHintCharges -= 1
                setStatus("🐾🏠", BlockQuestLiteStatusTone.WARNING)
            }

            reason == "dusk" && shelterLightQuality < 45 -> {
                buddyHintCharges -= 1
                setStatus("🐾💡", BlockQuestLiteStatusTone.INFO)
            }
        }
    }

    private fun updateOnboardingShelterProgress() {
        if (phase != BlockQuestLitePhase.NIGHT && shelterScore >= 55) {
            onboardingShelterBuilt = true
        }
    }

    private fun addInventory(item: BlockQuestLiteItem, delta: Int) {
        val next = (inventory[item] ?: 0) + delta
        if (next <= 0) {
            inventory.remove(item)
        } else {
            inventory[item] = next
        }
    }

    private fun generateInitialWorld() {
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                val block = when {
                    y >= HEIGHT - 1 -> BlockQuestLiteBlock.STONE
                    y >= HEIGHT - 2 -> if (x % 3 == 0) BlockQuestLiteBlock.STONE else BlockQuestLiteBlock.DIRT
                    y == HEIGHT - 3 -> BlockQuestLiteBlock.DIRT
                    y == HEIGHT - 4 && (x == 1 || x == WIDTH - 2) -> BlockQuestLiteBlock.WOOD
                    y == HEIGHT - 4 && (x == WIDTH / 2 || x == WIDTH / 2 + 1) -> BlockQuestLiteBlock.CRYSTAL
                    else -> BlockQuestLiteBlock.AIR
                }
                world[idx(x, y)] = block.ordinal
            }
        }

        inventory.clear()
        inventory[BlockQuestLiteItem.DIRT] = 3
        inventory[BlockQuestLiteItem.WOOD] = 2
        inventory[BlockQuestLiteItem.TORCH] = 1
    }

    private fun computeNightDamageCooldownTicks(): Int {
        return when {
            easyMode && dayNumber <= 3 -> 14
            easyMode -> 12
            dayNumber <= 2 -> 11
            else -> 10
        }
    }

    private fun isOpenToSky(x: Int, fromY: Int): Boolean {
        if (x !in 0 until WIDTH) return true
        if (fromY < 0) return true
        for (y in fromY downTo 0) {
            if (getBlock(x, y) != BlockQuestLiteBlock.AIR) return false
        }
        return true
    }

    private fun isStructural(block: BlockQuestLiteBlock): Boolean {
        return block != BlockQuestLiteBlock.AIR && block != BlockQuestLiteBlock.TORCH
    }

    private fun setStatus(message: String, tone: BlockQuestLiteStatusTone) {
        statusMessage = message
        statusTone = tone
    }

    private fun inBounds(x: Int, y: Int): Boolean {
        return x in 0 until WIDTH && y in 0 until HEIGHT
    }

    private fun idx(x: Int, y: Int): Int = y * WIDTH + x
}
