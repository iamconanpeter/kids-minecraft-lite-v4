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
        color = Color.rgb(244, 248, 255)
        textSize = dp(14f)
    }
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(226, 235, 248)
        textSize = dp(11f)
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 8, 14, 24)
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.75f)
    }

    private val worldRect = RectF()
    private val modeRect = RectF()
    private val cycleRect = RectF()
    private val craftToggleRect = RectF()
    private val parentRect = RectF()
    private val parentEasyRect = RectF()
    private val feedbackRect = RectF()
    private val objectiveRect = RectF()
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
        drawObjectiveStrip(canvas, state)
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
            BlockQuestLitePhase.DAY -> Color.rgb(68, 158, 238)
            BlockQuestLitePhase.DUSK -> Color.rgb(124, 94, 176)
            BlockQuestLitePhase.NIGHT -> Color.rgb(22, 36, 72)
            BlockQuestLitePhase.DAWN -> Color.rgb(106, 151, 208)
        }
        canvas.drawColor(color)
    }

    private fun drawWorld(canvas: Canvas, state: BlockQuestLiteState) {
        val topPadding = worldTopPadding(state)
        var bottomReserved = dp(100f)
        if (craftPanelVisible) bottomReserved += dp(196f)
        if (parentPanelVisible) bottomReserved += dp(140f)

        val availableHeight = max(dp(86f), height - topPadding - bottomReserved)
        val cell = min(width / state.width.toFloat(), availableHeight / state.height.toFloat())
        val worldWidth = cell * state.width
        val worldHeight = cell * state.height

        val left = (width - worldWidth) / 2f
        val top = topPadding

        worldRect.set(left, top, left + worldWidth, top + worldHeight)

        borderPaint.color = Color.argb(35, 255, 255, 255)
        canvas.drawRoundRect(
            worldRect.left - dp(2f),
            worldRect.top - dp(2f),
            worldRect.right + dp(2f),
            worldRect.bottom + dp(2f),
            dp(8f),
            dp(8f),
            borderPaint
        )

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
                    blockPaint.color = darken(blockColor(block), 0.2f)
                    canvas.drawRect(l, t + cell * 0.72f, r, b, blockPaint)
                    borderPaint.color = Color.argb(120, 0, 0, 0)
                } else {
                    borderPaint.color = Color.argb(45, 255, 255, 255)
                }
                canvas.drawRect(l, t, r, b, borderPaint)
            }
        }
    }

    private fun drawHud(canvas: Canvas, state: BlockQuestLiteState) {
        val hudHeight = dp(108f)
        canvas.drawRoundRect(0f, 0f, width.toFloat(), hudHeight, dp(14f), dp(14f), hudPaint)

        textPaint.textSize = dp(18f)
        canvas.drawText("${timeIcon(state.phase)} D${state.dayNumber}", dp(10f), dp(28f), textPaint)
        canvas.drawText("❤️${state.hearts}", dp(116f), dp(28f), textPaint)
        canvas.drawText("⭐${state.stars}", dp(180f), dp(28f), textPaint)
        canvas.drawText("🏠${shelterBadge(state.shelterScore)}", dp(234f), dp(28f), textPaint)

        textPaint.textSize = dp(15f)
        val modeIcon = if (state.mode == BlockQuestLiteInputMode.MINE) "⛏️" else "🧱"
        val selectedItem = engine.currentPlaceItem()
        val selectedCount = state.inventory[selectedItem] ?: 0
        canvas.drawText("$modeIcon ${itemIcon(selectedItem)}$selectedCount", dp(10f), dp(54f), textPaint)

        val trustBadge = trustMeter(state.buddyTrust)
        val graceBadge = if (state.adaptiveGraceNights > 0) "🛡️${state.adaptiveGraceNights}" else "·"
        canvas.drawText("🐾$trustBadge $graceBadge", dp(126f), dp(54f), textPaint)

        val dangerIcon = when {
            state.bossEventActive -> "🐉"
            state.phase == BlockQuestLitePhase.NIGHT -> "🌱"
            else -> "🐾"
        }
        val moodIcon = if (state.easyMode) "🙂" else "🔥"
        canvas.drawText("$dangerIcon $moodIcon", width - dp(78f), dp(54f), textPaint)

        drawFeedbackChip(canvas, state, hudHeight)
    }

    private fun drawFeedbackChip(canvas: Canvas, state: BlockQuestLiteState, hudHeight: Float) {
        val top = hudHeight - dp(34f)
        feedbackRect.set(dp(8f), top, width - dp(8f), hudHeight - dp(6f))

        buttonPaint.color = feedbackColor(state.statusTone)
        canvas.drawRoundRect(feedbackRect, dp(10f), dp(10f), buttonPaint)

        borderPaint.color = Color.argb(180, 255, 255, 255)
        canvas.drawRoundRect(feedbackRect, dp(10f), dp(10f), borderPaint)

        textPaint.textSize = dp(15f)
        val message = state.statusMessage.take(16)
        canvas.drawText(message, feedbackRect.left + dp(10f), feedbackRect.bottom - dp(9f), textPaint)
    }

    private fun drawObjectiveStrip(canvas: Canvas, state: BlockQuestLiteState) {
        val top = dp(112f)
        val bottom = top + dp(42f)
        objectiveRect.set(dp(8f), top, width - dp(8f), bottom)

        buttonPaint.color = Color.argb(200, 15, 27, 41)
        canvas.drawRoundRect(objectiveRect, dp(11f), dp(11f), buttonPaint)
        borderPaint.color = Color.argb(160, 172, 210, 246)
        canvas.drawRoundRect(objectiveRect, dp(11f), dp(11f), borderPaint)

        val objective = objectiveFor(state)
        textPaint.textSize = dp(18f)
        canvas.drawText("🎯 ${objective.first}", objectiveRect.left + dp(10f), objectiveRect.top + dp(25f), textPaint)

        smallTextPaint.textSize = dp(12f)
        canvas.drawText(objective.second, objectiveRect.right - dp(120f), objectiveRect.top + dp(25f), smallTextPaint)
    }

    private fun drawOnboarding(canvas: Canvas, state: BlockQuestLiteState) {
        val allDone = allOnboardingDone(state)
        val top = objectiveRect.bottom + dp(6f)
        val heightPx = if (allDone) dp(48f) else dp(66f)
        val barRect = RectF(dp(8f), top, width - dp(8f), top + heightPx)

        buttonPaint.color = Color.argb(190, 10, 21, 34)
        canvas.drawRoundRect(barRect, dp(12f), dp(12f), buttonPaint)

        borderPaint.color = Color.argb(140, 157, 196, 232)
        canvas.drawRoundRect(barRect, dp(12f), dp(12f), borderPaint)

        val mineDone = state.blocksMined > 0
        val placeDone = state.blocksPlaced > 0
        val shelterDone = state.onboardingShelterBuilt
        val steps = listOf(
            Triple(mineDone, "⛏️", "Mine"),
            Triple(placeDone, "🧱", "Place"),
            Triple(shelterDone, "🏠", "Safe")
        )
        val currentStep = steps.indexOfFirst { !it.first }

        if (allDone) {
            textPaint.textSize = dp(18f)
            canvas.drawText("🎉 ✅✅✅", barRect.left + dp(12f), barRect.top + dp(28f), textPaint)
            smallTextPaint.textSize = dp(12f)
            canvas.drawText("Tutorial done", barRect.right - dp(110f), barRect.top + dp(28f), smallTextPaint)
            return
        }

        val centerY = barRect.top + dp(24f)
        val stepGap = (barRect.width() - dp(44f)) / 2f

        for (i in 0..2) {
            if (i < 2) {
                val start = barRect.left + dp(22f) + stepGap * i
                val end = start + stepGap
                borderPaint.color = Color.argb(120, 152, 183, 220)
                canvas.drawLine(start, centerY, end, centerY, borderPaint)
            }
        }

        steps.forEachIndexed { index, (done, icon, label) ->
            val cx = barRect.left + dp(22f) + stepGap * index
            val r = dp(14f)
            buttonPaint.color = when {
                done -> Color.rgb(66, 150, 104)
                index == currentStep -> Color.rgb(76, 128, 196)
                else -> Color.rgb(66, 78, 96)
            }
            canvas.drawCircle(cx, centerY, r, buttonPaint)
            borderPaint.color = Color.argb(190, 255, 255, 255)
            canvas.drawCircle(cx, centerY, r, borderPaint)

            textPaint.textSize = dp(14f)
            val iconText = if (done) "✅" else icon
            canvas.drawText(iconText, cx - dp(7f), centerY + dp(4f), textPaint)

            smallTextPaint.textSize = dp(10f)
            canvas.drawText(label, cx - dp(12f), barRect.bottom - dp(8f), smallTextPaint)
        }

        if (currentStep >= 0) {
            smallTextPaint.textSize = dp(11f)
            val prompt = when (currentStep) {
                0 -> "👉 tap block"
                1 -> "👉 place block"
                else -> "👉 build shelter"
            }
            canvas.drawText(prompt, barRect.right - dp(96f), barRect.top + dp(14f), smallTextPaint)
        }
    }

    private fun drawControls(canvas: Canvas, state: BlockQuestLiteState) {
        val h = dp(72f)
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
        val panelTop = panelBottom - dp(186f)
        val panelRect = RectF(dp(8f), panelTop, width - dp(8f), panelBottom)

        buttonPaint.color = Color.argb(225, 11, 20, 31)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), buttonPaint)
        borderPaint.color = Color.argb(140, 160, 200, 238)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), borderPaint)

        textPaint.textSize = dp(13f)
        canvas.drawText("🧪 Craft", panelRect.left + dp(10f), panelRect.top + dp(18f), textPaint)

        recipeRects.clear()
        val recipes = engine.recipes()
        val cols = 2
        val spacing = dp(8f)
        val cellW = (panelRect.width() - spacing * 3) / cols
        val cellH = max(dp(74f), (panelRect.height() - spacing * 3 - dp(10f)) / 2f)

        recipes.forEachIndexed { i, recipe ->
            val col = i % cols
            val row = i / cols
            val left = panelRect.left + spacing + col * (cellW + spacing)
            val top = panelRect.top + dp(20f) + row * (cellH + spacing)
            val rect = RectF(left, top, left + cellW, top + cellH)
            recipeRects[recipe.id] = rect

            val unlocked = state.stars >= recipe.starsRequired
            buttonPaint.color = if (unlocked) Color.rgb(45, 111, 89) else Color.rgb(98, 62, 62)
            canvas.drawRoundRect(rect, dp(10f), dp(10f), buttonPaint)

            borderPaint.color = if (unlocked) Color.argb(190, 154, 239, 200) else Color.argb(180, 239, 151, 151)
            canvas.drawRoundRect(rect, dp(10f), dp(10f), borderPaint)

            textPaint.textSize = dp(12f)
            canvas.drawText(recipe.icon, rect.left + dp(8f), rect.top + dp(18f), textPaint)

            val needs = recipe.inputs.entries.joinToString(" ") { "${itemIcon(it.key)}${it.value}" }
            canvas.drawText(needs, rect.left + dp(8f), rect.top + dp(36f), textPaint)

            val out = "${itemIcon(recipe.output.first)}x${recipe.output.second}"
            canvas.drawText(out, rect.left + dp(8f), rect.top + dp(54f), textPaint)

            textPaint.textSize = dp(14f)
            canvas.drawText(if (unlocked) "🔓" else "🔒", rect.right - dp(22f), rect.top + dp(18f), textPaint)
        }
    }

    private fun drawParentPanel(canvas: Canvas, state: BlockQuestLiteState) {
        val panelBottom = modeRect.top - dp(10f)
        val panelTop = panelBottom - dp(130f)
        val panelRect = RectF(dp(8f), panelTop, width - dp(8f), panelBottom)

        buttonPaint.color = Color.argb(225, 11, 20, 31)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), buttonPaint)
        borderPaint.color = Color.argb(140, 160, 200, 238)
        canvas.drawRoundRect(panelRect, dp(12f), dp(12f), borderPaint)

        textPaint.textSize = dp(13f)
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

        smallTextPaint.textSize = dp(12f)
        val helper = if (state.easyMode) "Softer nights + buddy help" else "Harder nights"
        canvas.drawText(helper, parentEasyRect.left + dp(10f), parentEasyRect.top + dp(44f), smallTextPaint)
    }

    private fun drawButton(canvas: Canvas, rect: RectF, icon: String, caption: String, active: Boolean) {
        buttonPaint.color = if (active) Color.argb(222, 60, 106, 152) else Color.argb(210, 12, 20, 30)
        canvas.drawRoundRect(rect, dp(12f), dp(12f), buttonPaint)

        borderPaint.color = if (active) Color.argb(190, 171, 224, 255) else Color.argb(145, 235, 245, 255)
        canvas.drawRoundRect(rect, dp(12f), dp(12f), borderPaint)

        textPaint.textSize = dp(26f)
        val iconWidth = textPaint.measureText(icon)
        val iconX = rect.left + (rect.width() - iconWidth) / 2f
        canvas.drawText(icon, iconX, rect.top + dp(35f), textPaint)

        smallTextPaint.textSize = dp(11f)
        val captionWidth = smallTextPaint.measureText(caption)
        val captionX = rect.left + (rect.width() - captionWidth) / 2f
        canvas.drawText(caption, captionX, rect.bottom - dp(11f), smallTextPaint)
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
            score >= 80 -> "🟩🟩🟩"
            score >= 60 -> "🟩🟨"
            score >= 40 -> "🟨🟧"
            else -> "🟥"
        }
    }

    private fun trustMeter(trust: Int): String {
        return when {
            trust >= 85 -> "🟩🟩🟩"
            trust >= 65 -> "🟩🟩"
            trust >= 40 -> "🟨"
            else -> "🟥"
        }
    }

    private fun objectiveFor(state: BlockQuestLiteState): Pair<String, String> {
        return when {
            state.blocksMined == 0 -> "⛏️" to "Mine 1"
            state.blocksPlaced == 0 -> "🧱" to "Place 1"
            !state.onboardingShelterBuilt -> "🏠🌙" to "Safe home"
            state.buddyTrust < 70 -> "🐾🤝" to "Build trust"
            state.phase == BlockQuestLitePhase.NIGHT && state.shelterScore < 65 -> "🛡️🏠" to "Fortify"
            state.hearts <= 2 -> "❤️⬆️" to "Stay safe"
            else -> "⭐🌅" to "Survive"
        }
    }

    private fun allOnboardingDone(state: BlockQuestLiteState): Boolean {
        return state.blocksMined > 0 && state.blocksPlaced > 0 && state.onboardingShelterBuilt
    }

    private fun feedbackColor(tone: BlockQuestLiteStatusTone): Int {
        return when (tone) {
            BlockQuestLiteStatusTone.SUCCESS -> Color.argb(215, 50, 120, 83)
            BlockQuestLiteStatusTone.INFO -> Color.argb(210, 48, 90, 138)
            BlockQuestLiteStatusTone.WARNING -> Color.argb(215, 151, 102, 38)
            BlockQuestLiteStatusTone.ERROR -> Color.argb(215, 143, 67, 67)
            BlockQuestLiteStatusTone.DANGER -> Color.argb(220, 162, 52, 66)
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

    private fun worldTopPadding(state: BlockQuestLiteState): Float {
        val onboardingHeight = if (allOnboardingDone(state)) dp(48f) else dp(66f)
        return dp(112f) + dp(42f) + dp(6f) + onboardingHeight + dp(10f)
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}
