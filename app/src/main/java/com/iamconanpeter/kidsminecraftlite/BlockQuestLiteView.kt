package com.iamconanpeter.kidsminecraftlite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
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
        color = Color.argb(180, 10, 20, 25)
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
    private val parentRect = RectF()
    private val parentEasyRect = RectF()
    private val recipeRects = mutableMapOf<String, RectF>()

    private var craftPanelVisible = false
    private var parentPanelVisible = false
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
        drawOnboarding(canvas, state)
        drawControls(canvas, state)
        if (craftPanelVisible) {
            drawCraftPanel(canvas, state)
        }
        if (parentPanelVisible) {
            drawParentPanel(canvas, state)
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
        val topPadding = dp(154f)
        var bottomReserved = dp(88f)
        if (craftPanelVisible) bottomReserved += dp(182f)
        if (parentPanelVisible) bottomReserved += dp(128f)

        val availableHeight = max(dp(84f), height - topPadding - bottomReserved)
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
        val hudHeight = dp(82f)
        canvas.drawRoundRect(0f, 0f, width.toFloat(), hudHeight, dp(12f), dp(12f), hudPaint)

        textPaint.textSize = dp(18f)
        canvas.drawText("${timeIcon(state.phase)} D${state.dayNumber}", dp(10f), dp(28f), textPaint)
        canvas.drawText("❤️${state.hearts}", dp(120f), dp(28f), textPaint)
        canvas.drawText("⭐${state.stars}", dp(182f), dp(28f), textPaint)
        canvas.drawText("🏠${shelterBadge(state.shelterScore)}", dp(238f), dp(28f), textPaint)

        val modeIcon = if (state.mode == BlockQuestLiteInputMode.MINE) "⛏️" else "🧱"
        val selectedItem = engine.currentPlaceItem()
        val selectedCount = state.inventory[selectedItem] ?: 0

        textPaint.textSize = dp(16f)
        canvas.drawText("$modeIcon  ${itemIcon(selectedItem)}$selectedCount", dp(10f), dp(56f), textPaint)

        val moodIcon = if (state.easyMode) "🙂" else "🔥"
        val dangerIcon = if (state.bossEventActive) "🐉" else if (state.phase == BlockQuestLitePhase.NIGHT) "🌱" else "🐾"
        canvas.drawText("$dangerIcon  $moodIcon", width - dp(86f), dp(56f), textPaint)
    }

    private fun drawOnboarding(canvas: Canvas, state: BlockQuestLiteState) {
        val barTop = dp(88f)
        val barBottom = dp(144f)
        val barRect = RectF(dp(8f), barTop, width - dp(8f), barBottom)

        buttonPaint.color = Color.argb(165, 10, 20, 25)
        canvas.drawRoundRect(barRect, dp(12f), dp(12f), buttonPaint)

        val mineDone = state.blocksMined > 0
        val placeDone = state.blocksPlaced > 0
        val shelterDone = state.onboardingShelterBuilt

        val steps = listOf(
            Triple(mineDone, "⛏️", "Mine"),
            Triple(placeDone, "🧱", "Place"),
            Triple(shelterDone, "🏠🌙", "Shelter")
        )
        val currentStep = steps.indexOfFirst { !it.first }

        val gap = dp(8f)
        val stepW = (barRect.width() - gap * 4) / 3f
        val stepH = barRect.height() - gap * 2

        steps.forEachIndexed { index, (done, icon, label) ->
            val left = barRect.left + gap + index * (stepW + gap)
            val top = barRect.top + gap
            val rect = RectF(left, top, left + stepW, top + stepH)

            val isActive = index == currentStep
            buttonPaint.color = when {
                done -> Color.rgb(62, 137, 92)
                isActive -> Color.rgb(76, 109, 164)
                else -> Color.rgb(52, 58, 70)
            }
            canvas.drawRoundRect(rect, dp(10f), dp(10f), buttonPaint)

            textPaint.textSize = dp(16f)
            val iconX = rect.left + rect.width() / 2f - dp(14f)
            canvas.drawText(if (done) "✅ $icon" else icon, iconX, rect.top + dp(23f), textPaint)

            textPaint.textSize = dp(11f)
            canvas.drawText(label, rect.left + rect.width() / 2f - dp(18f), rect.top + dp(40f), textPaint)
        }

        if (currentStep >= 0) {
            textPaint.textSize = dp(12f)
            val prompt = when (currentStep) {
                0 -> "👉 ⛏️"
                1 -> "👉 🧱"
                else -> "👉 🏠 ➜ 🌙"
            }
            canvas.drawText(prompt, barRect.right - dp(84f), barRect.bottom - dp(9f), textPaint)
        }
    }

    private fun drawControls(canvas: Canvas, state: BlockQuestLiteState) {
        val h = dp(64f)
        val gap = dp(8f)
        val y = height - h - dp(12f)
        val buttonW = (width - dp(16f) - gap * 3) / 4f

        modeRect.set(dp(8f), y, dp(8f) + buttonW, y + h)
        cycleRect.set(modeRect.right + gap, y, modeRect.right + gap + buttonW, y + h)
        craftToggleRect.set(cycleRect.right + gap, y, cycleRect.right + gap + buttonW, y + h)
        parentRect.set(craftToggleRect.right + gap, y, craftToggleRect.right + gap + buttonW, y + h)

        drawButton(
            canvas,
            modeRect,
            if (state.mode == BlockQuestLiteInputMode.MINE) "⛏️" else "🧱",
            if (state.mode == BlockQuestLiteInputMode.MINE) "Mine" else "Place",
            false
        )

        val selected = engine.currentPlaceItem()
        val selectedCount = state.inventory[selected] ?: 0
        drawButton(canvas, cycleRect, itemIcon(selected), selectedCount.toString(), false)

        drawButton(canvas, craftToggleRect, if (craftPanelVisible) "❌" else "🧪", "Craft", craftPanelVisible)
        drawButton(canvas, parentRect, if (parentPanelVisible) "✅" else "👪", "Parent", parentPanelVisible)
    }

    private fun drawCraftPanel(canvas: Canvas, state: BlockQuestLiteState) {
        val panelBottom = modeRect.top - dp(10f)
        val panelTop = panelBottom - dp(172f)
        val panelRect = RectF(dp(8f), panelTop, width - dp(8f), panelBottom)

        buttonPaint.color = Color.argb(215, 17, 24, 31)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), buttonPaint)

        textPaint.textSize = dp(12f)
        canvas.drawText("🧪", panelRect.left + dp(10f), panelRect.top + dp(18f), textPaint)

        recipeRects.clear()
        val recipes = engine.recipes()
        val cols = 2
        val spacing = dp(8f)
        val cellW = (panelRect.width() - spacing * 3) / cols
        val cellH = max(dp(66f), (panelRect.height() - spacing * 3 - dp(10f)) / 2f)

        recipes.forEachIndexed { i, recipe ->
            val col = i % cols
            val row = i / cols
            val left = panelRect.left + spacing + col * (cellW + spacing)
            val top = panelRect.top + dp(20f) + row * (cellH + spacing)
            val rect = RectF(left, top, left + cellW, top + cellH)
            recipeRects[recipe.id] = rect

            val unlocked = state.stars >= recipe.starsRequired
            buttonPaint.color = if (unlocked) Color.rgb(56, 95, 79) else Color.rgb(95, 62, 62)
            canvas.drawRoundRect(rect, dp(10f), dp(10f), buttonPaint)

            borderPaint.color = if (unlocked) Color.argb(170, 124, 232, 172) else Color.argb(170, 232, 132, 132)
            canvas.drawRoundRect(rect, dp(10f), dp(10f), borderPaint)

            textPaint.textSize = dp(12f)
            canvas.drawText(recipe.icon, rect.left + dp(8f), rect.top + dp(18f), textPaint)

            val needs = recipe.inputs.entries.joinToString(" ") { "${itemIcon(it.key)}${it.value}" }
            canvas.drawText(needs, rect.left + dp(8f), rect.top + dp(35f), textPaint)

            val out = "${itemIcon(recipe.output.first)}x${recipe.output.second}"
            canvas.drawText(out, rect.left + dp(8f), rect.top + dp(52f), textPaint)

            textPaint.textSize = dp(14f)
            val lock = if (unlocked) "🔓" else "🔒"
            canvas.drawText(lock, rect.right - dp(22f), rect.top + dp(18f), textPaint)

            if (recipe.starsRequired > 0) {
                textPaint.textSize = dp(10f)
                val starsCue = "⭐".repeat(recipe.starsRequired.coerceAtMost(3))
                canvas.drawText(starsCue, rect.right - dp(30f), rect.bottom - dp(8f), textPaint)
            }
        }
    }

    private fun drawParentPanel(canvas: Canvas, state: BlockQuestLiteState) {
        val panelBottom = modeRect.top - dp(10f)
        val panelTop = panelBottom - dp(114f)
        val panelRect = RectF(dp(8f), panelTop, width - dp(8f), panelBottom)

        buttonPaint.color = Color.argb(220, 17, 24, 31)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), buttonPaint)

        textPaint.textSize = dp(12f)
        canvas.drawText("👪 Parent", panelRect.left + dp(10f), panelRect.top + dp(18f), textPaint)

        parentEasyRect.set(
            panelRect.left + dp(10f),
            panelRect.top + dp(28f),
            panelRect.right - dp(10f),
            panelRect.bottom - dp(10f)
        )

        buttonPaint.color = if (state.easyMode) Color.rgb(66, 135, 98) else Color.rgb(123, 82, 74)
        canvas.drawRoundRect(parentEasyRect, dp(10f), dp(10f), buttonPaint)

        textPaint.textSize = dp(16f)
        val modeLabel = if (state.easyMode) "🙂 Calm" else "🔥 Brave"
        canvas.drawText(modeLabel, parentEasyRect.left + dp(10f), parentEasyRect.top + dp(24f), textPaint)

        textPaint.textSize = dp(11f)
        val helper = if (state.easyMode) "Soft nights + helper gifts" else "Harder nights, bigger challenge"
        canvas.drawText(helper, parentEasyRect.left + dp(10f), parentEasyRect.top + dp(42f), textPaint)
    }

    private fun drawButton(canvas: Canvas, rect: RectF, icon: String, caption: String, active: Boolean) {
        buttonPaint.color = if (active) Color.argb(220, 67, 97, 130) else Color.argb(205, 17, 24, 31)
        canvas.drawRoundRect(rect, dp(10f), dp(10f), buttonPaint)

        borderPaint.color = if (active) Color.argb(170, 154, 218, 255) else Color.argb(120, 255, 255, 255)
        canvas.drawRoundRect(rect, dp(10f), dp(10f), borderPaint)

        textPaint.textSize = dp(24f)
        val iconX = rect.left + rect.width() / 2f - dp(12f)
        canvas.drawText(icon, iconX, rect.top + dp(30f), textPaint)

        textPaint.textSize = dp(10f)
        val captionX = rect.left + rect.width() / 2f - (caption.length * dp(2f))
        canvas.drawText(caption, captionX, rect.bottom - dp(9f), textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        val x = event.x
        val y = event.y

        when {
            modeRect.contains(x, y) -> engine.toggleMode()
            cycleRect.contains(x, y) -> engine.cyclePlaceItem()
            craftToggleRect.contains(x, y) -> {
                craftPanelVisible = !craftPanelVisible
                if (craftPanelVisible) parentPanelVisible = false
            }
            parentRect.contains(x, y) -> {
                parentPanelVisible = !parentPanelVisible
                if (parentPanelVisible) craftPanelVisible = false
            }
            parentPanelVisible && parentEasyRect.contains(x, y) -> engine.toggleEasyMode()
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

    private fun shelterBadge(score: Int): String {
        return when {
            score >= 75 -> "🟩🟩🟩"
            score >= 50 -> "🟨🟨"
            score >= 25 -> "🟧"
            else -> "🟥"
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
