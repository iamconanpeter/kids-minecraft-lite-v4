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

    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = dp(14f)
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(170, 10, 20, 25)
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private val worldRect = RectF()
    private val modeRect = RectF()
    private val cycleRect = RectF()
    private val craftToggleRect = RectF()
    private val easyRect = RectF()
    private val recipeRects = mutableMapOf<String, RectF>()

    private var craftPanelVisible = false
    private var running = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            engine.tick()
            if (engine.currentState().cycleTick % 15 == 0) {
                persistState()
            }
            invalidate()
            postDelayed(this, 130L)
        }
    }

    init {
        isFocusable = true
        isClickable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!running) {
            running = true
            post(tickRunnable)
        }
    }

    override fun onDetachedFromWindow() {
        running = false
        removeCallbacks(tickRunnable)
        persistState()
        super.onDetachedFromWindow()
    }

    fun persistState() {
        progressManager.save(engine.toSavePayload())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val state = engine.currentState()
        drawBackground(canvas, state)
        drawWorld(canvas, state)
        drawHud(canvas, state)
        drawControls(canvas, state)
        if (craftPanelVisible) {
            drawCraftPanel(canvas, state)
        }
    }

    private fun drawBackground(canvas: Canvas, state: BlockQuestLiteState) {
        val color = when (state.phase) {
            BlockQuestLitePhase.DAY -> Color.rgb(83, 171, 255)
            BlockQuestLitePhase.DUSK -> Color.rgb(164, 109, 196)
            BlockQuestLitePhase.NIGHT -> Color.rgb(35, 45, 83)
            BlockQuestLitePhase.DAWN -> Color.rgb(120, 163, 214)
        }
        canvas.drawColor(color)
    }

    private fun drawWorld(canvas: Canvas, state: BlockQuestLiteState) {
        val topPadding = dp(74f)
        val bottomReserved = dp(if (craftPanelVisible) 210f else 126f)
        val availableHeight = height - topPadding - bottomReserved
        val cell = min(width / state.width.toFloat(), availableHeight / state.height.toFloat())
        val worldWidth = cell * state.width
        val worldHeight = cell * state.height

        val left = (width - worldWidth) / 2f
        val top = topPadding

        worldRect.set(left, top, left + worldWidth, top + worldHeight)

        for (y in 0 until state.height) {
            for (x in 0 until state.width) {
                val block = BlockQuestLiteBlock.values()[state.world[y * state.width + x]]
                val l = left + x * cell
                val t = top + y * cell
                val r = l + cell
                val b = t + cell

                blockPaint.color = blockColor(block)
                canvas.drawRect(l, t, r, b, blockPaint)

                if (block != BlockQuestLiteBlock.AIR) {
                    blockPaint.color = darken(blockColor(block), 0.22f)
                    canvas.drawRect(l, t + cell * 0.72f, r, b, blockPaint)
                    borderPaint.color = Color.argb(120, 0, 0, 0)
                    canvas.drawRect(l, t, r, b, borderPaint)
                } else {
                    borderPaint.color = Color.argb(35, 255, 255, 255)
                    canvas.drawRect(l, t, r, b, borderPaint)
                }
            }
        }
    }

    private fun drawHud(canvas: Canvas, state: BlockQuestLiteState) {
        canvas.drawRoundRect(0f, 0f, width.toFloat(), dp(70f), dp(12f), dp(12f), hudPaint)

        textPaint.textSize = dp(14f)
        canvas.drawText(timeIcon(state.phase) + " Day ${state.dayNumber}", dp(10f), dp(22f), textPaint)
        canvas.drawText("❤️ ${state.hearts}   ⭐ ${state.stars}   🛡️ ${state.shelterScore}", dp(10f), dp(42f), textPaint)

        val mobLine = buildString {
            append("🐾")
            if (state.phase == BlockQuestLitePhase.NIGHT) append(" 🌱")
            if (state.bossEventActive) append(" 🐉")
            append("   ")
            append(if (state.easyMode) "🙂" else "🔥")
            append("   T${state.unlockTier}")
        }

        canvas.drawText(mobLine, width - dp(150f), dp(22f), textPaint)

        textPaint.textSize = dp(12f)
        canvas.drawText(state.statusMessage, width / 2f - dp(130f), dp(62f), textPaint)
    }

    private fun drawControls(canvas: Canvas, state: BlockQuestLiteState) {
        val h = dp(44f)
        val gap = dp(8f)
        val y = height - h - dp(16f)
        val buttonW = (width - dp(16f) - gap * 3) / 4f

        modeRect.set(dp(8f), y, dp(8f) + buttonW, y + h)
        cycleRect.set(modeRect.right + gap, y, modeRect.right + gap + buttonW, y + h)
        craftToggleRect.set(cycleRect.right + gap, y, cycleRect.right + gap + buttonW, y + h)
        easyRect.set(craftToggleRect.right + gap, y, craftToggleRect.right + gap + buttonW, y + h)

        drawButton(canvas, modeRect, if (state.mode == BlockQuestLiteInputMode.MINE) "⛏️" else "🧱")
        drawButton(canvas, cycleRect, itemIcon(engine.currentPlaceItem()))
        drawButton(canvas, craftToggleRect, if (craftPanelVisible) "❌" else "🧪")
        drawButton(canvas, easyRect, if (state.easyMode) "🙂" else "🔥")
    }

    private fun drawCraftPanel(canvas: Canvas, state: BlockQuestLiteState) {
        val panelBottom = height - dp(66f)
        val panelTop = panelBottom - dp(128f)
        val panelRect = RectF(dp(8f), panelTop, width - dp(8f), panelBottom)

        buttonPaint.color = Color.argb(210, 17, 24, 31)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), buttonPaint)

        textPaint.textSize = dp(12f)
        canvas.drawText("Craft", panelRect.left + dp(10f), panelRect.top + dp(18f), textPaint)

        recipeRects.clear()
        val recipes = engine.recipes()
        val cols = 2
        val spacing = dp(8f)
        val cellW = (panelRect.width() - spacing * 3) / cols
        val cellH = (panelRect.height() - spacing * 3) / 2f

        recipes.forEachIndexed { i, recipe ->
            val col = i % cols
            val row = i / cols
            val left = panelRect.left + spacing + col * (cellW + spacing)
            val top = panelRect.top + dp(22f) + row * (cellH + spacing)
            val rect = RectF(left, top, left + cellW, top + cellH)
            recipeRects[recipe.id] = rect

            val allowed = state.stars >= recipe.starsRequired
            buttonPaint.color = if (allowed) Color.rgb(56, 95, 79) else Color.rgb(90, 66, 66)
            canvas.drawRoundRect(rect, dp(10f), dp(10f), buttonPaint)

            textPaint.textSize = dp(11f)
            canvas.drawText(recipe.icon, rect.left + dp(8f), rect.top + dp(18f), textPaint)
            val needs = recipe.inputs.entries.joinToString(" ") { "${itemIcon(it.key)}${it.value}" }
            canvas.drawText(needs, rect.left + dp(8f), rect.top + dp(34f), textPaint)
            val out = "${itemIcon(recipe.output.first)}x${recipe.output.second}"
            canvas.drawText(out, rect.left + dp(8f), rect.top + dp(50f), textPaint)

            if (!allowed) {
                canvas.drawText("Need ${recipe.starsRequired}⭐", rect.left + dp(8f), rect.top + dp(64f), textPaint)
            }
        }
    }

    private fun drawButton(canvas: Canvas, rect: RectF, label: String) {
        buttonPaint.color = Color.argb(200, 17, 24, 31)
        canvas.drawRoundRect(rect, dp(10f), dp(10f), buttonPaint)
        borderPaint.color = Color.argb(120, 255, 255, 255)
        canvas.drawRoundRect(rect, dp(10f), dp(10f), borderPaint)

        textPaint.textSize = dp(20f)
        val x = rect.left + rect.width() / 2f - dp(9f)
        val y = rect.top + rect.height() / 2f + dp(7f)
        canvas.drawText(label, x, y, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        val x = event.x
        val y = event.y

        when {
            modeRect.contains(x, y) -> engine.toggleMode()
            cycleRect.contains(x, y) -> engine.cyclePlaceItem()
            craftToggleRect.contains(x, y) -> craftPanelVisible = !craftPanelVisible
            easyRect.contains(x, y) -> engine.toggleEasyMode()
            craftPanelVisible && tapRecipe(x, y) -> {
                // handled
            }
            worldRect.contains(x, y) -> {
                val state = engine.currentState()
                val cell = worldRect.width() / state.width
                val gridX = ((x - worldRect.left) / cell).toInt().coerceIn(0, state.width - 1)
                val gridY = ((y - worldRect.top) / cell).toInt().coerceIn(0, state.height - 1)
                engine.tapTile(gridX, gridY)
            }
        }

        invalidate()
        return true
    }

    private fun tapRecipe(x: Float, y: Float): Boolean {
        for ((id, rect) in recipeRects) {
            if (rect.contains(x, y)) {
                engine.craft(id)
                return true
            }
        }
        return false
    }

    private fun blockColor(block: BlockQuestLiteBlock): Int {
        return when (block) {
            BlockQuestLiteBlock.AIR -> Color.argb(0, 0, 0, 0)
            BlockQuestLiteBlock.DIRT -> Color.rgb(141, 102, 68)
            BlockQuestLiteBlock.WOOD -> Color.rgb(194, 154, 101)
            BlockQuestLiteBlock.STONE -> Color.rgb(128, 132, 145)
            BlockQuestLiteBlock.PLANK -> Color.rgb(214, 183, 126)
            BlockQuestLiteBlock.TORCH -> Color.rgb(252, 185, 48)
            BlockQuestLiteBlock.CRYSTAL -> Color.rgb(111, 222, 255)
        }
    }

    private fun itemIcon(item: BlockQuestLiteItem): String {
        return when (item) {
            BlockQuestLiteItem.DIRT -> "🟫"
            BlockQuestLiteItem.WOOD -> "🪵"
            BlockQuestLiteItem.STONE -> "🪨"
            BlockQuestLiteItem.PLANK -> "📦"
            BlockQuestLiteItem.TORCH -> "🔥"
            BlockQuestLiteItem.CRYSTAL -> "💎"
            BlockQuestLiteItem.PICKAXE -> "⛏️"
        }
    }

    private fun timeIcon(phase: BlockQuestLitePhase): String {
        return when (phase) {
            BlockQuestLitePhase.DAY -> "☀️"
            BlockQuestLitePhase.DUSK -> "🌆"
            BlockQuestLitePhase.NIGHT -> "🌙"
            BlockQuestLitePhase.DAWN -> "🌤️"
        }
    }

    private fun darken(color: Int, amount: Float): Int {
        val factor = 1f - amount
        return Color.rgb(
            (Color.red(color) * factor).toInt().coerceIn(0, 255),
            (Color.green(color) * factor).toInt().coerceIn(0, 255),
            (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        )
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}
