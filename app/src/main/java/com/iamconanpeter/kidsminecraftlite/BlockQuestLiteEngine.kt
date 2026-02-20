package com.iamconanpeter.kidsminecraftlite

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

data class BlockQuestLiteRecipe(
    val id: String,
    val icon: String,
    val inputs: Map<BlockQuestLiteItem, Int>,
    val output: Pair<BlockQuestLiteItem, Int>,
    val starsRequired: Int = 0
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

    private var giftCooldownTicks = 20
    private var nightDamageCooldown = 10
    private var statusMessage = "Tap blocks to mine!"

    init {
        generateInitialWorld()
        if (!savedPayload.isNullOrBlank()) {
            if (!restore(savedPayload)) {
                statusMessage = "New world ready!"
            }
        }
        shelterScore = computeShelterScore()
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
        statusMessage = if (mode == BlockQuestLiteInputMode.MINE) "⛏️" else "🧱"
    }

    fun cyclePlaceItem() {
        selectedPlaceIndex = (selectedPlaceIndex + 1) % PLACEABLE_ITEMS.size
        statusMessage = "🎒 ${currentPlaceItem().name.lowercase()}"
    }

    fun toggleEasyMode() {
        easyMode = !easyMode
        statusMessage = if (easyMode) "🙂 Calm mode" else "🔥 Brave mode"
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
            statusMessage = "❔"
            return false
        }

        world[idx(x, y)] = BlockQuestLiteBlock.AIR.ordinal

        val item = toItem(current)
        if (item != null) {
            addInventory(item, 1)
            blocksMined += 1
            if (current == BlockQuestLiteBlock.STONE && ((x + y + dayNumber + cycleTick) % 5 == 0)) {
                addInventory(BlockQuestLiteItem.CRYSTAL, 1)
                statusMessage = "💎"
            } else {
                statusMessage = "⛏️+1"
            }
        }

        shelterScore = computeShelterScore()
        updateOnboardingShelterProgress()
        return true
    }

    fun placeBlock(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        if (getBlock(x, y) != BlockQuestLiteBlock.AIR) {
            statusMessage = "🚫"
            return false
        }

        val selectedItem = currentPlaceItem()
        val selectedBlock = toBlock(selectedItem)
        if (selectedBlock == null) {
            statusMessage = "🚫"
            return false
        }

        if (getInventory(selectedItem) <= 0) {
            statusMessage = "📦❌"
            return false
        }

        world[idx(x, y)] = selectedBlock.ordinal
        addInventory(selectedItem, -1)
        blocksPlaced += 1

        shelterScore = computeShelterScore()
        updateOnboardingShelterProgress()
        statusMessage = "🧱✅"
        return true
    }

    fun craft(recipeId: String): Boolean {
        val recipe = RECIPES.firstOrNull { it.id == recipeId }
            ?: return false

        if (stars < recipe.starsRequired) {
            statusMessage = "🔒 ${recipe.starsRequired}⭐"
            return false
        }

        for ((item, amount) in recipe.inputs) {
            if (getInventory(item) < amount) {
                statusMessage = "📦❌"
                return false
            }
        }

        recipe.inputs.forEach { (item, amount) ->
            addInventory(item, -amount)
        }
        addInventory(recipe.output.first, recipe.output.second)

        craftedThisCycle = true
        statusMessage = "🧪✅"
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
                BlockQuestLitePhase.DUSK -> statusMessage = "🌆 Dusk! Build shelter"
                BlockQuestLitePhase.NIGHT -> startNightPhase()
                BlockQuestLitePhase.DAWN -> statusMessage = "🌤️ Hold on, dawn is near"
                BlockQuestLitePhase.DAY -> statusMessage = "☀️ New day!"
            }
        }

        if (phase == BlockQuestLitePhase.DAY) {
            processGlowmewGift()
        }

        if (phase == BlockQuestLitePhase.NIGHT) {
            processNightPressure()
        }

        shelterScore = computeShelterScore()
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
            onboardingShelterBuilt
        ).joinToString(",")

        return listOf(worldString, inventoryString, scalar).joinToString(";")
    }

    internal fun debugGrant(item: BlockQuestLiteItem, amount: Int) {
        addInventory(item, amount)
    }

    internal fun debugSetBlock(x: Int, y: Int, block: BlockQuestLiteBlock) {
        if (!inBounds(x, y)) return
        world[idx(x, y)] = block.ordinal
        shelterScore = computeShelterScore()
        updateOnboardingShelterProgress()
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
            nightDamageCooldown = max(1, s[19].toIntOrNull() ?: 10)
            blocksMined = max(0, s.getOrNull(20)?.toIntOrNull() ?: 0)
            blocksPlaced = max(0, s.getOrNull(21)?.toIntOrNull() ?: 0)
            onboardingShelterBuilt = s.getOrNull(22)?.toBooleanStrictOrNull() ?: false

            true
        } catch (_: Exception) {
            false
        }
    }

    private fun startNightPhase() {
        statusMessage = "🌙 Night! Boom Sprout is out"
        boomSproutThreat += if (shelterScore >= 70) 0 else 1

        val isBossNight = dayNumber % 3 == 0
        if (isBossNight) {
            bossEventActive = true
            skyWyrmEvents += 1
            statusMessage = "🐉 Sky Wyrm event!"
        }
    }

    private fun processGlowmewGift() {
        giftCooldownTicks -= 1
        if (giftCooldownTicks <= 0) {
            val gift = if ((dayNumber + cycleTick + glowmewGiftMoments) % 3 == 0) {
                BlockQuestLiteItem.CRYSTAL
            } else {
                BlockQuestLiteItem.WOOD
            }
            addInventory(gift, 1)
            glowmewGiftMoments += 1
            giftCooldownTicks = if (easyMode) 22 else 30
            statusMessage = "🐾 Glowmew found ${gift.name.lowercase()}"
        }
    }

    private fun processNightPressure() {
        nightDamageCooldown -= 1
        if (nightDamageCooldown > 0) return

        val baseChance = when {
            shelterScore >= 75 -> 12
            shelterScore >= 55 -> 35
            else -> 72
        }

        val chance = if (easyMode) max(5, baseChance - 20) else baseChance
        val bossBonus = if (bossEventActive) 15 else 0
        val roll = (cycleTick * 7 + dayNumber * 13 + hearts * 5 + boomSproutThreat * 3) % 100

        if (roll < chance + bossBonus) {
            hearts -= 1
            boomSproutThreat += 1
            statusMessage = if (bossEventActive) "🐉 Sky Wyrm struck!" else "🌱 Boom Sprout chased you!"
            if (hearts <= 0) {
                rescuePlayer()
            }
        }

        nightDamageCooldown = if (easyMode) 12 else 9
    }

    private fun onSunrise() {
        nightsSurvived += 1

        stars += 1
        if (craftedThisCycle) stars += 1
        if (bossEventActive || bossRewardPending) stars += 1

        unlockTier = 1 + (stars / 4)

        craftedThisCycle = false
        bossRewardPending = bossEventActive
        bossEventActive = false

        boomSproutThreat = max(0, boomSproutThreat - 1)
        nightDamageCooldown = 10

        statusMessage = "🌅 Sunrise! +stars"
    }

    private fun rescuePlayer() {
        rescuedCount += 1
        hearts = MAX_HEARTS
        stars = max(0, stars - 1)
        bossEventActive = false
        bossRewardPending = false
        statusMessage = "🛟 Dawn rescue! Try again"
    }

    private fun computeShelterScore(): Int {
        val anchorX = WIDTH / 2
        val anchorY = 2

        var score = 0

        val walls = listOf(
            anchorX - 1 to anchorY,
            anchorX + 1 to anchorY,
            anchorX to anchorY - 1,
            anchorX to anchorY + 1
        )

        walls.forEach { (x, y) ->
            if (getBlock(x, y) != BlockQuestLiteBlock.AIR) score += 20
        }

        val ring = mutableListOf<Pair<Int, Int>>()
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                ring += (anchorX + dx) to (anchorY + dy)
            }
        }

        val openings = ring.count { (x, y) -> getBlock(x, y) == BlockQuestLiteBlock.AIR }
        score -= openings * 4

        val torchCount = ring.count { (x, y) -> getBlock(x, y) == BlockQuestLiteBlock.TORCH }
        score += min(3, torchCount) * 8

        val floorBand = (anchorX - 1..anchorX + 1).count { x ->
            getBlock(x, anchorY + 2) != BlockQuestLiteBlock.AIR
        }
        score += floorBand * 5

        return score.coerceIn(0, 100)
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

    private fun inBounds(x: Int, y: Int): Boolean {
        return x in 0 until WIDTH && y in 0 until HEIGHT
    }

    private fun idx(x: Int, y: Int): Int = y * WIDTH + x
}
