package com.iamconanpeter.kidsminecraftlite

class BlockQuestLiteEngine(snapshot: SaveSnapshot? = null) {

    enum class BlockType {
        AIR,
        DIRT,
        WOOD,
        STONE,
        PLANK,
        TORCH,
        CRYSTAL
    }

    enum class ItemType {
        DIRT,
        WOOD,
        STONE,
        PLANK,
        TORCH,
        CRYSTAL,
        PICKAXE
    }

    enum class DayPhase {
        DAY,
        NIGHT
    }

    enum class InputMode {
        MINE,
        PLACE
    }

    enum class MobMood {
        HELPFUL,
        HUNTING,
        RAGING,
        CALM
    }

    data class Recipe(
        val id: String,
        val icon: String,
        val inputs: Map<ItemType, Int>,
        val output: ItemType,
        val outputCount: Int
    )

    data class SaveSnapshot(
        val world: String,
        val inventory: String,
        val stars: Int,
        val hearts: Int,
        val cycleTick: Int,
        val nightsSurvived: Int,
        val craftedSinceSunrise: Boolean,
        val easyMode: Boolean,
        val hasPickaxe: Boolean,
        val selectedItem: String,
        val inputMode: String,
        val bossActive: Boolean,
        val bossHp: Int,
        val bossTicksLeft: Int,
        val chaserDistance: Int,
        val rescueCount: Int
    )

    data class GameSnapshot(
        val width: Int,
        val height: Int,
        val world: List<BlockType>,
        val phase: DayPhase,
        val cycleTick: Int,
        val cycleLength: Int,
        val stars: Int,
        val hearts: Int,
        val maxHearts: Int,
        val shelterScore: Int,
        val sheltered: Boolean,
        val inventory: Map<ItemType, Int>,
        val selectedPlaceable: ItemType,
        val unlockedPlaceables: List<ItemType>,
        val mode: InputMode,
        val easyMode: Boolean,
        val nightsSurvived: Int,
        val friendlyMood: MobMood,
        val chaserMood: MobMood,
        val bossMood: MobMood,
        val chaserDistance: Int,
        val bossActive: Boolean,
        val bossHp: Int,
        val bossHpMax: Int,
        val lastEvent: String
    )

    private val world = MutableList(WORLD_WIDTH * WORLD_HEIGHT) { BlockType.AIR }
    private val inventory = mutableMapOf<ItemType, Int>()

    var mode: InputMode = InputMode.MINE
        private set
    var easyMode: Boolean = false
        private set
    var selectedPlaceable: ItemType = ItemType.DIRT
        private set

    var stars: Int = 0
        private set
    var hearts: Int = MAX_HEARTS
        private set
    var cycleTick: Int = 0
        private set
    var phase: DayPhase = DayPhase.DAY
        private set
    var nightsSurvived: Int = 0
        private set
    var rescueCount: Int = 0
        private set

    var craftedSinceSunrise: Boolean = false
        private set
    var hasPickaxe: Boolean = false
        private set

    private var petGiftCooldown = 8
    private var chaserDistance = CHASER_START_DISTANCE
    private var chaserStepClock = 0

    private var bossActive = false
    private var bossHp = 0
    private var bossTicksLeft = 0

    private var lastEvent = "🌤️ Build your shelter!"

    val recipes: List<Recipe> = listOf(
        Recipe(
            id = "plank_bundle",
            icon = "🪵→🧱",
            inputs = mapOf(ItemType.WOOD to 2),
            output = ItemType.PLANK,
            outputCount = 2
        ),
        Recipe(
            id = "stone_pick",
            icon = "⛏️",
            inputs = mapOf(ItemType.STONE to 2, ItemType.WOOD to 1),
            output = ItemType.PICKAXE,
            outputCount = 1
        ),
        Recipe(
            id = "torch_pair",
            icon = "🔥x2",
            inputs = mapOf(ItemType.PLANK to 1, ItemType.CRYSTAL to 1),
            output = ItemType.TORCH,
            outputCount = 2
        ),
        Recipe(
            id = "mud_brick",
            icon = "🟫→🪨",
            inputs = mapOf(ItemType.DIRT to 2, ItemType.WOOD to 1),
            output = ItemType.STONE,
            outputCount = 1
        )
    )

    init {
        if (snapshot == null) {
            seedWorld()
            seedInventory()
        } else {
            restore(snapshot)
        }
        enforceSelectedPlaceableUnlocked()
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        width = WORLD_WIDTH,
        height = WORLD_HEIGHT,
        world = world.toList(),
        phase = phase,
        cycleTick = cycleTick,
        cycleLength = CYCLE_TICKS,
        stars = stars,
        hearts = hearts,
        maxHearts = MAX_HEARTS,
        shelterScore = shelterScore(),
        sheltered = isSheltered(),
        inventory = inventory.toMap(),
        selectedPlaceable = selectedPlaceable,
        unlockedPlaceables = unlockedPlaceables(),
        mode = mode,
        easyMode = easyMode,
        nightsSurvived = nightsSurvived,
        friendlyMood = if (phase == DayPhase.DAY) MobMood.HELPFUL else MobMood.CALM,
        chaserMood = if (phase == DayPhase.NIGHT) MobMood.HUNTING else MobMood.CALM,
        bossMood = when {
            bossActive -> MobMood.RAGING
            else -> MobMood.CALM
        },
        chaserDistance = chaserDistance,
        bossActive = bossActive,
        bossHp = bossHp,
        bossHpMax = if (easyMode) 2 else 3,
        lastEvent = lastEvent
    )

    fun exportSaveSnapshot(): SaveSnapshot = SaveSnapshot(
        world = world.joinToString(separator = ",") { it.name },
        inventory = inventory.entries.joinToString(separator = ";") { "${it.key.name}:${it.value}" },
        stars = stars,
        hearts = hearts,
        cycleTick = cycleTick,
        nightsSurvived = nightsSurvived,
        craftedSinceSunrise = craftedSinceSunrise,
        easyMode = easyMode,
        hasPickaxe = hasPickaxe,
        selectedItem = selectedPlaceable.name,
        inputMode = mode.name,
        bossActive = bossActive,
        bossHp = bossHp,
        bossTicksLeft = bossTicksLeft,
        chaserDistance = chaserDistance,
        rescueCount = rescueCount
    )

    fun blockAt(x: Int, y: Int): BlockType = world[indexOf(x, y)]

    fun setMode(newMode: InputMode) {
        mode = newMode
    }

    fun toggleEasyMode() {
        easyMode = !easyMode
        lastEvent = if (easyMode) "🧑‍🍼 Easy mode ON" else "🎯 Easy mode OFF"
    }

    fun cyclePlaceable() {
        val unlocked = unlockedPlaceables()
        if (unlocked.isEmpty()) return
        val current = unlocked.indexOf(selectedPlaceable)
        val next = if (current < 0) 0 else (current + 1) % unlocked.size
        selectedPlaceable = unlocked[next]
    }

    fun craft(recipeId: String): Boolean {
        val recipe = recipes.firstOrNull { it.id == recipeId } ?: return false
        if (!hasResources(recipe.inputs)) {
            lastEvent = "❌ Need more blocks"
            return false
        }

        recipe.inputs.forEach { (item, count) -> removeInventory(item, count) }
        addInventory(recipe.output, recipe.outputCount)

        if (recipe.output == ItemType.PICKAXE) {
            hasPickaxe = true
            lastEvent = "⛏️ Pickaxe crafted!"
        } else {
            lastEvent = "✨ Crafted ${recipe.icon}"
        }

        craftedSinceSunrise = true
        return true
    }

    fun performTileAction(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        return when (mode) {
            InputMode.MINE -> mineTile(x, y)
            InputMode.PLACE -> placeTile(x, y)
        }
    }

    fun mineTile(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        val block = blockAt(x, y)
        if (block == BlockType.AIR) return false

        val loot = when (block) {
            BlockType.AIR -> null
            BlockType.DIRT -> ItemType.DIRT to 1
            BlockType.WOOD -> ItemType.WOOD to 1
            BlockType.STONE -> ItemType.STONE to if (hasPickaxe) 2 else 1
            BlockType.PLANK -> ItemType.PLANK to 1
            BlockType.TORCH -> ItemType.TORCH to 1
            BlockType.CRYSTAL -> ItemType.CRYSTAL to 1
        }

        setBlock(x, y, BlockType.AIR)
        loot?.let { addInventory(it.first, it.second) }
        lastEvent = "⛏️ Mined ${block.name.lowercase()}"
        return true
    }

    fun placeTile(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        if (blockAt(x, y) != BlockType.AIR) {
            lastEvent = "🧱 Spot occupied"
            return false
        }
        if (selectedPlaceable !in unlockedPlaceables()) {
            lastEvent = "🔒 Item locked"
            return false
        }
        if ((inventory[selectedPlaceable] ?: 0) <= 0) {
            lastEvent = "🎒 Need more ${selectedPlaceable.name.lowercase()}"
            return false
        }

        val blockType = selectedPlaceable.toBlockType() ?: return false
        setBlock(x, y, blockType)
        removeInventory(selectedPlaceable, 1)
        lastEvent = "🧱 Placed ${selectedPlaceable.name.lowercase()}"
        return true
    }

    fun tick() {
        val previousPhase = phase
        cycleTick += 1
        if (cycleTick >= CYCLE_TICKS) {
            cycleTick = 0
        }

        phase = if (cycleTick < DAY_TICKS) DayPhase.DAY else DayPhase.NIGHT

        if (previousPhase != phase) {
            if (phase == DayPhase.NIGHT) {
                beginNight()
            } else {
                beginDay()
            }
        }

        if (phase == DayPhase.DAY) {
            handleDayTick()
        } else {
            handleNightTick()
        }

        enforceSelectedPlaceableUnlocked()
    }

    fun shelterScore(): Int {
        val scoreRaw =
            scoreIfSolid(1, 4, 18) + // left wall
            scoreIfSolid(3, 4, 18) + // right wall
            scoreIfSolid(2, 3, 24) + // roof
            scoreIfSolid(2, 5, 14) + // floor
            scoreIfSolid(1, 3, 10) + // roof-left
            scoreIfSolid(3, 3, 10) + // roof-right
            scoreIfSolid(1, 5, 3) + // floor-left
            scoreIfSolid(3, 5, 3) // floor-right

        return if (easyMode) maxOf(scoreRaw, EASY_MODE_SHELTER_FLOOR) else scoreRaw
    }

    fun isSheltered(): Boolean = shelterScore() >= SHELTER_SAFE_THRESHOLD

    private fun beginNight() {
        nightsSurvived += 1
        chaserDistance = if (easyMode) 5 else CHASER_START_DISTANCE
        chaserStepClock = 0
        lastEvent = "🌙 Night! Build and hide!"

        if (nightsSurvived % 3 == 0) {
            bossActive = true
            bossHp = if (easyMode) 2 else 3
            bossTicksLeft = if (easyMode) 16 else 20
            lastEvent = "🐉 Sky Wyrm appears!"
        }
    }

    private fun beginDay() {
        stars += 1 // survived the night
        if (craftedSinceSunrise) {
            stars += 1
        }
        craftedSinceSunrise = false

        if (hearts < MAX_HEARTS) hearts += 1

        petGiftCooldown = if (easyMode) 10 else 12
        chaserDistance = CHASER_START_DISTANCE
        chaserStepClock = 0

        lastEvent = "☀️ Sunrise! +stars"
    }

    private fun handleDayTick() {
        petGiftCooldown -= 1
        if (petGiftCooldown <= 0) {
            petGiftCooldown = if (easyMode) 10 else 12
            if (nightsSurvived % 2 == 0) {
                addInventory(ItemType.WOOD, 1)
                lastEvent = "🐾 Glowmew found wood"
            } else {
                addInventory(ItemType.CRYSTAL, 1)
                lastEvent = "🐾 Glowmew found crystal"
            }
        }
    }

    private fun handleNightTick() {
        chaserStepClock += 1
        val stepEvery = if (easyMode) 8 else 5
        if (chaserStepClock >= stepEvery) {
            chaserStepClock = 0
            chaserDistance -= 1
        }

        if (chaserDistance <= 0) {
            if (isSheltered()) {
                chaserDistance = if (easyMode) 4 else 3
                lastEvent = "🏠 Boom Sprout bounced off your shelter"
            } else {
                hearts -= 1
                chaserDistance = if (easyMode) 5 else CHASER_START_DISTANCE
                lastEvent = "💥 Boom Sprout hit!"
                if (hearts <= 0) {
                    hearts = MAX_HEARTS
                    stars = maxOf(0, stars - 1)
                    rescueCount += 1
                    lastEvent = "🕊️ Dawn rescue! Keep trying"
                }
            }
        }

        if (bossActive) {
            bossTicksLeft -= 1
            if (isSheltered() && cycleTick % 4 == 0) {
                bossHp -= 1
            }
            if (bossHp <= 0) {
                bossActive = false
                stars += 3
                lastEvent = "🌟 Sky Wyrm calmed! +3 stars"
            } else if (bossTicksLeft <= 0) {
                bossActive = false
                lastEvent = "🐉 Sky Wyrm flew away"
            }
        }
    }

    private fun unlockedPlaceables(): List<ItemType> {
        val tier = unlockTier()
        val list = mutableListOf(ItemType.DIRT, ItemType.WOOD)
        if (tier >= 1) list += ItemType.STONE
        if (tier >= 2) list += ItemType.PLANK
        if (tier >= 3) list += ItemType.TORCH
        if (tier >= 4) list += ItemType.CRYSTAL
        return list
    }

    private fun unlockTier(): Int = when {
        stars >= 9 -> 4
        stars >= 6 -> 3
        stars >= 4 -> 2
        stars >= 2 -> 1
        else -> 0
    }

    private fun enforceSelectedPlaceableUnlocked() {
        if (selectedPlaceable !in unlockedPlaceables()) {
            selectedPlaceable = unlockedPlaceables().first()
        }
    }

    private fun seedWorld() {
        world.indices.forEach { world[it] = BlockType.AIR }

        for (x in 0 until WORLD_WIDTH) {
            setBlock(x, WORLD_HEIGHT - 1, BlockType.DIRT)
            if (x % 2 == 0) setBlock(x, WORLD_HEIGHT - 2, BlockType.STONE)
        }

        setBlock(5, 4, BlockType.WOOD)
        setBlock(6, 4, BlockType.WOOD)
        setBlock(7, 4, BlockType.CRYSTAL)
        setBlock(8, 4, BlockType.STONE)

        // tiny starter shelter frame target around (2,4) interior
        setBlock(2, 5, BlockType.DIRT)
    }

    private fun seedInventory() {
        inventory.clear()
        inventory[ItemType.DIRT] = 8
        inventory[ItemType.WOOD] = 5
        inventory[ItemType.STONE] = 1
    }

    private fun restore(snapshot: SaveSnapshot) {
        val worldTokens = snapshot.world.split(",")
        if (worldTokens.size == world.size) {
            for (i in world.indices) {
                world[i] = runCatching { BlockType.valueOf(worldTokens[i]) }.getOrDefault(BlockType.AIR)
            }
        } else {
            seedWorld()
        }

        inventory.clear()
        if (snapshot.inventory.isNotBlank()) {
            snapshot.inventory.split(";").forEach { token ->
                val pair = token.split(":")
                if (pair.size == 2) {
                    val item = runCatching { ItemType.valueOf(pair[0]) }.getOrNull()
                    val count = pair[1].toIntOrNull() ?: 0
                    if (item != null && count > 0) inventory[item] = count
                }
            }
        }

        stars = snapshot.stars
        hearts = snapshot.hearts.coerceIn(1, MAX_HEARTS)
        cycleTick = snapshot.cycleTick.coerceIn(0, CYCLE_TICKS - 1)
        phase = if (cycleTick < DAY_TICKS) DayPhase.DAY else DayPhase.NIGHT
        nightsSurvived = snapshot.nightsSurvived.coerceAtLeast(0)
        craftedSinceSunrise = snapshot.craftedSinceSunrise
        easyMode = snapshot.easyMode
        hasPickaxe = snapshot.hasPickaxe
        selectedPlaceable = runCatching { ItemType.valueOf(snapshot.selectedItem) }.getOrDefault(ItemType.DIRT)
        mode = runCatching { InputMode.valueOf(snapshot.inputMode) }.getOrDefault(InputMode.MINE)
        bossActive = snapshot.bossActive
        bossHp = snapshot.bossHp.coerceAtLeast(0)
        bossTicksLeft = snapshot.bossTicksLeft.coerceAtLeast(0)
        chaserDistance = snapshot.chaserDistance.coerceAtLeast(1)
        rescueCount = snapshot.rescueCount.coerceAtLeast(0)
    }

    private fun addInventory(item: ItemType, count: Int) {
        inventory[item] = (inventory[item] ?: 0) + count
    }

    private fun removeInventory(item: ItemType, count: Int) {
        val now = (inventory[item] ?: 0) - count
        if (now <= 0) {
            inventory.remove(item)
        } else {
            inventory[item] = now
        }
    }

    private fun hasResources(cost: Map<ItemType, Int>): Boolean = cost.all { (item, count) ->
        (inventory[item] ?: 0) >= count
    }

    private fun ItemType.toBlockType(): BlockType? = when (this) {
        ItemType.DIRT -> BlockType.DIRT
        ItemType.WOOD -> BlockType.WOOD
        ItemType.STONE -> BlockType.STONE
        ItemType.PLANK -> BlockType.PLANK
        ItemType.TORCH -> BlockType.TORCH
        ItemType.CRYSTAL -> BlockType.CRYSTAL
        ItemType.PICKAXE -> null
    }

    private fun inBounds(x: Int, y: Int): Boolean =
        x in 0 until WORLD_WIDTH && y in 0 until WORLD_HEIGHT

    private fun setBlock(x: Int, y: Int, block: BlockType) {
        world[indexOf(x, y)] = block
    }

    private fun scoreIfSolid(x: Int, y: Int, score: Int): Int {
        if (!inBounds(x, y)) return 0
        return if (blockAt(x, y) != BlockType.AIR) score else 0
    }

    private fun indexOf(x: Int, y: Int): Int = y * WORLD_WIDTH + x

    companion object {
        const val WORLD_WIDTH = 10
        const val WORLD_HEIGHT = 8

        const val DAY_TICKS = 36
        const val NIGHT_TICKS = 24
        const val CYCLE_TICKS = DAY_TICKS + NIGHT_TICKS

        const val MAX_HEARTS = 3
        const val SHELTER_SAFE_THRESHOLD = 70
        const val EASY_MODE_SHELTER_FLOOR = 35
        const val CHASER_START_DISTANCE = 4
    }
}
