package com.iamconanpeter.kidsminecraftlite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class BlockQuestLiteView @JvmOverloads constructor(
    context: Context,
    private val progressManager: BlockQuestLiteProgressManager,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val engine = BlockQuestLiteEngine(progressManager.load())
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mineRect = RectF()
    private val placeRect = RectF()
    private val cycleRect = RectF()
    private val craftRect = RectF()
    private val easyRect = RectF()
    private val worldRect = RectF()

    private val recipeRects = mutableListOf<RectF>()
    private var craftPanelOpen = false

    private var tickCount = 0

    private val tickRunnable = object : Runnable {
        override fun run() {
            engine.tick()
            tickCount += 1
            if (tickCount % 8 == 0) {
                persistState()
            }
            invalidate()
            postDelayed(this, TICK_MS)
        }
    }

    init {
        post(tickRunnable)
    }

    fun persistState() {
        progressManager.save(engine.exportSaveSnapshot())
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tickRunnable)
        persistState()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = engine.snapshot()

        drawBackground(canvas, s)
        layoutRects(width.toFloat(), height.toFloat(), craftPanelOpen)
        drawHud(canvas, s)
        drawWorld(canvas, s)
        drawControls(canvas, s)
        if (craftPanelOpen) drawCraftPanel(canvas, s)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val x = event.x
        val y = event.y

        if (mineRect.contains(x, y)) {
            engine.setMode(BlockQuestLiteEngine.InputMode.MINE)
            invalidate()
            return true
        }
        if (placeRect.contains(x, y)) {
            engine.setMode(BlockQuestLiteEngine.InputMode.PLACE)
            invalidate()
            return true
        }
        if (cycleRect.contains(x, y)) {
            engine.cyclePlaceable()
            invalidate()
            return true
        }
        if (craftRect.contains(x, y)) {
            craftPanelOpen = !craftPanelOpen
            invalidate()
            return true
        }
        if (easyRect.contains(x, y)) {
            engine.toggleEasyMode()
            invalidate()
            return true
        }

        if (craftPanelOpen) {
            recipeRects.forEachIndexed { index, rect ->
                if (rect.contains(x, y)) {
                    engine.craft(safeRecipeId(index))
                    invalidate()
                    return true
                }
            }
        }

        if (worldRect.contains(x, y)) {
            val s = engine.snapshot()
            val tileSize = worldRect.width() / s.width
            val tx = ((x - worldRect.left) / tileSize).toInt()
            val ty = ((y - worldRect.top) / tileSize).toInt()
            engine.performTileAction(tx, ty)
            invalidate()
            return true
        }

        return true
    }

    private fun safeRecipeId(index: Int): String {
        val recipes = engine.recipes
        if (index < 0 || index >= recipes.size) return recipes.first().id
        return recipes[index].id
    }

    private fun drawBackground(canvas: Canvas, s: BlockQuestLiteEngine.GameSnapshot) {
        val dayColor = Color.rgb(142, 210, 255)
        val nightColor = Color.rgb(22, 30, 72)
        paint.style = Paint.Style.FILL
        paint.color = if (s.phase == BlockQuestLiteEngine.DayPhase.DAY) dayColor else nightColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = if (s.phase == BlockQuestLiteEngine.DayPhase.DAY) Color.rgb(103, 188, 98) else Color.rgb(41, 73, 58)
        canvas.drawRect(0f, height * 0.45f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawHud(canvas: Canvas, s: BlockQuestLiteEngine.GameSnapshot) {
        paint.color = Color.WHITE
        paint.textSize = sp(18f)
        paint.style = Paint.Style.FILL

        val sunMoon = if (s.phase == BlockQuestLiteEngine.DayPhase.DAY) "☀️" else "🌙"
        canvas.drawText("$sunMoon  ⭐ ${s.stars}   ❤️ ${s.hearts}/${s.maxHearts}", dp(12f), dp(28f), paint)
        canvas.drawText("🏠 ${s.shelterScore}%   🐾 ${mobLine(s)}", dp(12f), dp(52f), paint)

        paint.textSize = sp(13f)
        canvas.drawText(s.lastEvent, dp(12f), dp(74f), paint)
    }

    private fun mobLine(s: BlockQuestLiteEngine.GameSnapshot): String {
        val chaser = "💥${s.chaserDistance}"
        val boss = if (s.bossActive) " 🐉${s.bossHp}/${s.bossHpMax}" else ""
        return "$chaser$boss"
    }

    private fun drawWorld(canvas: Canvas, s: BlockQuestLiteEngine.GameSnapshot) {
        val tileSize = worldRect.width() / s.width
        for (y in 0 until s.height) {
            for (x in 0 until s.width) {
                val idx = y * s.width + x
                val block = s.world[idx]
                val left = worldRect.left + x * tileSize
                val top = worldRect.top + y * tileSize
                val rect = RectF(left, top, left + tileSize - dp(1f), top + tileSize - dp(1f))

                paint.style = Paint.Style.FILL
                paint.color = blockColor(block)
                canvas.drawRoundRect(rect, dp(4f), dp(4f), paint)

                // simple 2.5D lower shade
                paint.color = shade(blockColor(block), 0.82f)
                canvas.drawRect(rect.left, rect.bottom - dp(5f), rect.right, rect.bottom, paint)
            }
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = Color.argb(190, 255, 255, 255)
        canvas.drawRect(worldRect, paint)
    }

    private fun drawControls(canvas: Canvas, s: BlockQuestLiteEngine.GameSnapshot) {
        drawButton(canvas, mineRect, if (s.mode == BlockQuestLiteEngine.InputMode.MINE) "⛏️" else "⛏", s.mode == BlockQuestLiteEngine.InputMode.MINE)
        drawButton(canvas, placeRect, if (s.mode == BlockQuestLiteEngine.InputMode.PLACE) "🧱" else "⬜", s.mode == BlockQuestLiteEngine.InputMode.PLACE)
        drawButton(canvas, cycleRect, "🎒 ${shortItem(s.selectedPlaceable)}", false)
        drawButton(canvas, craftRect, if (craftPanelOpen) "🛠️▲" else "🛠️▼", craftPanelOpen)
        drawButton(canvas, easyRect, if (s.easyMode) "🧑‍🍼ON" else "🧑‍🍼OFF", s.easyMode)
    }

    private fun drawCraftPanel(canvas: Canvas, s: BlockQuestLiteEngine.GameSnapshot) {
        val panelTop = craftRect.bottom + dp(8f)
        val panelBottom = panelTop + dp(190f)
        val panel = RectF(dp(12f), panelTop, width - dp(12f), panelBottom)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(210, 20, 20, 24)
        canvas.drawRoundRect(panel, dp(12f), dp(12f), paint)

        paint.textSize = sp(13f)
        paint.color = Color.WHITE
        canvas.drawText("Tiny Craft", panel.left + dp(12f), panel.top + dp(18f), paint)

        recipeRects.clear()
        val itemHeight = dp(34f)
        var top = panel.top + dp(26f)

        s.unlockedPlaceables
        engine.recipes.forEach { recipe ->
            val row = RectF(panel.left + dp(10f), top, panel.right - dp(10f), top + itemHeight)
            recipeRects += row

            paint.color = Color.argb(180, 60, 90, 150)
            canvas.drawRoundRect(row, dp(8f), dp(8f), paint)

            paint.color = Color.WHITE
            paint.textSize = sp(12f)
            val ok = recipe.inputs.all { (item, need) -> (s.inventory[item] ?: 0) >= need }
            val status = if (ok) "✅" else "❌"
            canvas.drawText("${recipe.icon}  $status", row.left + dp(8f), row.centerY() + dp(4f), paint)

            top += itemHeight + dp(6f)
        }
    }

    private fun drawButton(canvas: Canvas, rect: RectF, label: String, active: Boolean) {
        paint.style = Paint.Style.FILL
        paint.color = if (active) Color.argb(220, 70, 135, 210) else Color.argb(180, 35, 48, 78)
        canvas.drawRoundRect(rect, dp(10f), dp(10f), paint)

        paint.color = Color.WHITE
        paint.textSize = sp(14f)
        val tw = paint.measureText(label)
        canvas.drawText(label, rect.centerX() - tw / 2f, rect.centerY() + dp(5f), paint)
    }

    private fun layoutRects(w: Float, h: Float, craftOpen: Boolean) {
        val tileSide = min((w - dp(24f)) / BlockQuestLiteEngine.WORLD_WIDTH, h * 0.44f / BlockQuestLiteEngine.WORLD_HEIGHT)
        val worldW = tileSide * BlockQuestLiteEngine.WORLD_WIDTH
        val worldH = tileSide * BlockQuestLiteEngine.WORLD_HEIGHT
        val left = (w - worldW) / 2f
        worldRect.set(left, dp(90f), left + worldW, dp(90f) + worldH)

        val controlsTop = worldRect.bottom + dp(12f)
        val buttonW = (w - dp(36f)) / 5f
        val buttonH = dp(40f)

        fun slot(i: Int): RectF {
            val l = dp(8f) + i * (buttonW + dp(4f))
            return RectF(l, controlsTop, l + buttonW, controlsTop + buttonH)
        }

        mineRect.set(slot(0))
        placeRect.set(slot(1))
        cycleRect.set(slot(2))
        craftRect.set(slot(3))
        easyRect.set(slot(4))

        if (!craftOpen) recipeRects.clear()
    }

    private fun shortItem(item: BlockQuestLiteEngine.ItemType): String = when (item) {
        BlockQuestLiteEngine.ItemType.DIRT -> "DIRT"
        BlockQuestLiteEngine.ItemType.WOOD -> "WOOD"
        BlockQuestLiteEngine.ItemType.STONE -> "STONE"
        BlockQuestLiteEngine.ItemType.PLANK -> "PLANK"
        BlockQuestLiteEngine.ItemType.TORCH -> "TORCH"
        BlockQuestLiteEngine.ItemType.CRYSTAL -> "CRYS"
        BlockQuestLiteEngine.ItemType.PICKAXE -> "PICK"
    }

    private fun blockColor(block: BlockQuestLiteEngine.BlockType): Int = when (block) {
        BlockQuestLiteEngine.BlockType.AIR -> Color.argb(35, 255, 255, 255)
        BlockQuestLiteEngine.BlockType.DIRT -> Color.rgb(130, 89, 54)
        BlockQuestLiteEngine.BlockType.WOOD -> Color.rgb(154, 112, 58)
        BlockQuestLiteEngine.BlockType.STONE -> Color.rgb(120, 126, 133)
        BlockQuestLiteEngine.BlockType.PLANK -> Color.rgb(183, 143, 85)
        BlockQuestLiteEngine.BlockType.TORCH -> Color.rgb(255, 176, 46)
        BlockQuestLiteEngine.BlockType.CRYSTAL -> Color.rgb(126, 211, 255)
    }

    private fun shade(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity

    companion object {
        private const val TICK_MS = 700L
    }
}
