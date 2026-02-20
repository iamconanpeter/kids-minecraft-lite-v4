package com.iamconanpeter.kidsminecraftlite

import android.content.SharedPreferences

class BlockQuestLiteProgressManager(private val prefs: SharedPreferences) {

    fun save(snapshot: BlockQuestLiteEngine.SaveSnapshot) {
        prefs.edit()
            .putString(KEY_WORLD, snapshot.world)
            .putString(KEY_INVENTORY, snapshot.inventory)
            .putInt(KEY_STARS, snapshot.stars)
            .putInt(KEY_HEARTS, snapshot.hearts)
            .putInt(KEY_CYCLE_TICK, snapshot.cycleTick)
            .putInt(KEY_NIGHTS, snapshot.nightsSurvived)
            .putBoolean(KEY_CRAFTED, snapshot.craftedSinceSunrise)
            .putBoolean(KEY_EASY, snapshot.easyMode)
            .putBoolean(KEY_PICKAXE, snapshot.hasPickaxe)
            .putString(KEY_SELECTED_ITEM, snapshot.selectedItem)
            .putString(KEY_MODE, snapshot.inputMode)
            .putBoolean(KEY_BOSS_ACTIVE, snapshot.bossActive)
            .putInt(KEY_BOSS_HP, snapshot.bossHp)
            .putInt(KEY_BOSS_TICKS, snapshot.bossTicksLeft)
            .putInt(KEY_CHASER_DISTANCE, snapshot.chaserDistance)
            .putInt(KEY_RESCUE_COUNT, snapshot.rescueCount)
            .apply()
    }

    fun load(): BlockQuestLiteEngine.SaveSnapshot? {
        val world = prefs.getString(KEY_WORLD, null) ?: return null
        val inventory = prefs.getString(KEY_INVENTORY, "") ?: ""

        return BlockQuestLiteEngine.SaveSnapshot(
            world = world,
            inventory = inventory,
            stars = prefs.getInt(KEY_STARS, 0),
            hearts = prefs.getInt(KEY_HEARTS, BlockQuestLiteEngine.MAX_HEARTS),
            cycleTick = prefs.getInt(KEY_CYCLE_TICK, 0),
            nightsSurvived = prefs.getInt(KEY_NIGHTS, 0),
            craftedSinceSunrise = prefs.getBoolean(KEY_CRAFTED, false),
            easyMode = prefs.getBoolean(KEY_EASY, false),
            hasPickaxe = prefs.getBoolean(KEY_PICKAXE, false),
            selectedItem = prefs.getString(KEY_SELECTED_ITEM, BlockQuestLiteEngine.ItemType.DIRT.name)
                ?: BlockQuestLiteEngine.ItemType.DIRT.name,
            inputMode = prefs.getString(KEY_MODE, BlockQuestLiteEngine.InputMode.MINE.name)
                ?: BlockQuestLiteEngine.InputMode.MINE.name,
            bossActive = prefs.getBoolean(KEY_BOSS_ACTIVE, false),
            bossHp = prefs.getInt(KEY_BOSS_HP, 0),
            bossTicksLeft = prefs.getInt(KEY_BOSS_TICKS, 0),
            chaserDistance = prefs.getInt(KEY_CHASER_DISTANCE, BlockQuestLiteEngine.CHASER_START_DISTANCE),
            rescueCount = prefs.getInt(KEY_RESCUE_COUNT, 0)
        )
    }

    companion object {
        private const val KEY_WORLD = "world"
        private const val KEY_INVENTORY = "inventory"
        private const val KEY_STARS = "stars"
        private const val KEY_HEARTS = "hearts"
        private const val KEY_CYCLE_TICK = "cycle_tick"
        private const val KEY_NIGHTS = "nights"
        private const val KEY_CRAFTED = "crafted_since_sunrise"
        private const val KEY_EASY = "easy_mode"
        private const val KEY_PICKAXE = "has_pickaxe"
        private const val KEY_SELECTED_ITEM = "selected_item"
        private const val KEY_MODE = "input_mode"
        private const val KEY_BOSS_ACTIVE = "boss_active"
        private const val KEY_BOSS_HP = "boss_hp"
        private const val KEY_BOSS_TICKS = "boss_ticks"
        private const val KEY_CHASER_DISTANCE = "chaser_distance"
        private const val KEY_RESCUE_COUNT = "rescue_count"
    }
}
