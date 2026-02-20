package com.iamconanpeter.kidsminecraftlite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: BlockQuestLiteView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("block_quest_lite", MODE_PRIVATE)
        val progressManager = BlockQuestLiteProgressManager(prefs)
        gameView = BlockQuestLiteView(this, progressManager)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        gameView.persistState()
    }
}
