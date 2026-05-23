package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.FrameLayout
import org.fcitx.fcitx5.android.data.InputFeedbacks
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.data.theme.ThemeManager
import org.fcitx.fcitx5.android.input.SecLogger
import kotlin.math.abs
import splitties.dimensions.dp

@SuppressLint("ViewConstructor")
class T9PunctuationPanelView(
    context: Context,
    private val theme: Theme,
    panelDef: T9PunctuationPanelKey
) : FrameLayout(context) {

    private val defaultSymbols = panelDef.symbols
    private val visibleCount = panelDef.visibleCount

    private val prefs = ThemeManager.prefs
    private val radius = dp(prefs.keyRadius.getValue().toFloat())
    private val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    private val hMargin = dp(
        (if (landscape) prefs.keyHorizontalMarginLandscape else prefs.keyHorizontalMargin).getValue()
    )
    private val vMargin = dp(
        (if (landscape) prefs.keyVerticalMarginLandscape else prefs.keyVerticalMargin).getValue()
    )

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.altKeyBackgroundColor
    }

    private val highlightBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.keyPressHighlightColor
    }

    private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(18f).toFloat()
        typeface = Typeface.DEFAULT
        color = theme.altKeyTextColor
        textAlign = Paint.Align.CENTER
    }

    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(18f).toFloat()
        typeface = Typeface.DEFAULT_BOLD
        color = theme.keyTextColor
        textAlign = Paint.Align.CENTER
    }

    // Scroll indicator paint
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.altKeyTextColor
        alpha = 80
    }

    private var items: List<String> = defaultSymbols
    private var onSelect: (Int, String) -> Unit = { _, text ->
        onAction(KeyAction.T9CommitAction(text))
    }

    // Scroll: integer item offset + fractional sub-pixel offset for smooth drag
    private var scrollOffset = 0  // top visible item index
    private var scrollFraction = 0f  // 0..1 fraction of itemSpacing for smooth scroll
    private var maxScrollOffset = 0  // max value of scrollOffset

    // Touch state
    private var downY = 0f
    private var totalDragY = 0f
    private var isDragging = false
    private var pressedIndex = -1
    private var velocityTracker: VelocityTracker? = null

    // Fling animation
    private var flingVelocity = 0f  // px/s
    private val flingRunnable = object : Runnable {
        override fun run() {
            if (abs(flingVelocity) < 50f) {
                flingVelocity = 0f
                // Snap to nearest item
                if (scrollFraction > 0.5f && scrollOffset < maxScrollOffset) {
                    scrollOffset++
                }
                scrollFraction = 0f
                invalidate()
                return
            }
            val shift = flingVelocity * 16f / 1000f  // px per frame (~16ms)
            flingVelocity *= 0.92f  // friction

            val totalPx = scrollOffset * itemSpacing + scrollFraction * itemSpacing - shift
            val newOffset = (totalPx / itemSpacing).toInt().coerceIn(0, maxScrollOffset)
            val newFraction = ((totalPx / itemSpacing) - newOffset).coerceIn(0f, 1f)
            scrollOffset = newOffset
            scrollFraction = newFraction
            invalidate()
            postDelayed(this, 16)
        }
    }

    private var itemSpacing = 0f
    private var itemCenters = FloatArray(visibleCount)

    var onAction: (KeyAction) -> Unit = {}

    init {
        setWillNotDraw(false)
        isClickable = true
        isHapticFeedbackEnabled = false
    }

    fun showSymbols() {
        cancelFling()
        items = defaultSymbols
        onSelect = { _, text ->
            onAction(KeyAction.T9CommitAction(text))
        }
        scrollOffset = 0
        scrollFraction = 0f
        pressedIndex = -1
        computeLayout()
        invalidate()
    }

    fun showPinyin(pinyinList: List<String>) {
        cancelFling()
        items = pinyinList
        onSelect = { _, text ->
            onAction(KeyAction.T9PinyinSelectAction(text))
        }
        scrollOffset = 0
        scrollFraction = 0f
        pressedIndex = -1
        computeLayout()
        invalidate()
    }

    private fun computeLayout() {
        val contentHeight = height - vMargin * 2
        itemSpacing = contentHeight.toFloat() / (visibleCount + 1)
        val firstCenterY = vMargin + itemSpacing
        for (i in 0 until visibleCount) {
            itemCenters[i] = firstCenterY + i * itemSpacing
        }
        maxScrollOffset = (items.size - visibleCount).coerceAtLeast(0)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeLayout()
        SecLogger.d("T9PunctPanel", "onSizeChanged: w=$w h=$h items=${items.size} visibleCount=$visibleCount itemSpacing=$itemSpacing maxScroll=$maxScrollOffset")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height

        // Key background
        canvas.drawRoundRect(
            hMargin.toFloat(), vMargin.toFloat(),
            (w - hMargin).toFloat(), (h - vMargin).toFloat(),
            radius, radius, keyBgPaint
        )

        // Clip items to background bounds
        canvas.save()
        canvas.clipRect(hMargin.toFloat(), vMargin.toFloat(), (w - hMargin).toFloat(), (h - vMargin).toFloat())

        // Draw items offset by scrollFraction
        val centerX = w / 2f
        val fractionalShift = scrollFraction * itemSpacing

        for (i in 0 until visibleCount) {
            val visualY = itemCenters[i] - fractionalShift
            val dataIndex = scrollOffset + i
            if (dataIndex >= items.size) break

            val item = items[dataIndex]
            val isPressed = (dataIndex == pressedIndex)

            if (isPressed) {
                val halfH = itemSpacing / 2f
                canvas.drawRect(
                    hMargin.toFloat(), visualY - halfH,
                    (w - hMargin).toFloat(), visualY + halfH,
                    highlightBgPaint
                )
            }

            val paint = if (isPressed) pressedPaint else normalPaint
            canvas.drawText(item, centerX, visualY - (paint.descent() + paint.ascent()) / 2, paint)
        }

        // Draw partially visible item at top/bottom edges during scroll
        if (scrollFraction > 0f) {
            // Item scrolling out at top
            if (scrollOffset > 0) {
                val topItemY = itemCenters[0] - itemSpacing - fractionalShift
                val topIdx = scrollOffset - 1
                if (topIdx >= 0 && topItemY > vMargin) {
                    val item = items[topIdx]
                    val paint = normalPaint
                    canvas.drawText(item, centerX, topItemY - (paint.descent() + paint.ascent()) / 2, paint)
                }
            }
            // Item scrolling in at bottom
            val botIdx = scrollOffset + visibleCount
            if (botIdx < items.size) {
                val botItemY = itemCenters[visibleCount - 1] + itemSpacing - fractionalShift
                if (botItemY < h - vMargin) {
                    val item = items[botIdx]
                    val paint = normalPaint
                    canvas.drawText(item, centerX, botItemY - (paint.descent() + paint.ascent()) / 2, paint)
                }
            }
        }

        canvas.restore()

        // Scroll indicator (drawn outside clip)
        if (maxScrollOffset > 0) {
            val indicatorX = w - hMargin - dp(4)
            val indicatorTop = vMargin + dp(8)
            val indicatorBottom = h - vMargin - dp(8)
            val trackLen = indicatorBottom - indicatorTop
            val thumbLen = (trackLen * visibleCount.toFloat() / items.size).coerceAtLeast(dp(8).toFloat())
            val scrollRatio = scrollOffset.toFloat() / maxScrollOffset.coerceAtLeast(1)
            val thumbTop = indicatorTop + scrollRatio * (trackLen - thumbLen)
            canvas.drawRoundRect(
                indicatorX - dp(2).toFloat(), thumbTop,
                indicatorX + dp(2).toFloat(), thumbTop + thumbLen,
                dp(2).toFloat(), dp(2).toFloat(), indicatorPaint
            )
        }
    }

    private fun cancelFling() {
        flingVelocity = 0f
        removeCallbacks(flingRunnable)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!isEnabled) return false
                cancelFling()
                downY = event.y
                totalDragY = 0f
                isDragging = false
                pressedIndex = yToItemIndex(event.y)
                if (pressedIndex >= 0) {
                    InputFeedbacks.hapticFeedback(this)
                    InputFeedbacks.soundEffect(InputFeedbacks.SoundEffect.Standard)
                }
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - downY
                totalDragY += dy
                downY = event.y
                velocityTracker?.addMovement(event)

                if (!isDragging && abs(totalDragY) > dp(6)) {
                    isDragging = true
                    pressedIndex = -1
                }

                if (isDragging) {
                    // Scroll: dy > 0 = finger down = scroll up (show earlier items)
                    val totalPx = scrollOffset * itemSpacing + scrollFraction * itemSpacing - dy
                    val newOffset = (totalPx / itemSpacing).toInt().coerceIn(0, maxScrollOffset)
                    val newFraction = ((totalPx / itemSpacing) - newOffset).coerceIn(0f, 1f)
                    scrollOffset = newOffset
                    scrollFraction = newFraction
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val vy = velocityTracker?.yVelocity ?: 0f
                velocityTracker?.recycle()
                velocityTracker = null

                if (!isDragging && pressedIndex >= 0) {
                    // Tap — select the item
                    val idx = yToItemIndex(event.y)
                    if (idx >= 0 && idx < items.size) {
                        val item = items[idx]
                        SecLogger.d("T9PunctPanel", "Select item: $item (index=$idx)")
                        onSelect(idx, item)
                    }
                } else if (isDragging && abs(vy) > 300f) {
                    // Fling: vy > 0 = finger moving down = scroll up
                    flingVelocity = -vy
                    post(flingRunnable)
                } else if (isDragging) {
                    // Slow release — snap to nearest item
                    if (scrollFraction > 0.5f && scrollOffset < maxScrollOffset) {
                        scrollOffset++
                    }
                    scrollFraction = 0f
                    invalidate()
                }
                pressedIndex = -1
                isDragging = false
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelFling()
                velocityTracker?.recycle()
                velocityTracker = null
                pressedIndex = -1
                isDragging = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun yToItemIndex(y: Float): Int {
        val fractionalShift = scrollFraction * itemSpacing
        for (i in 0 until visibleCount) {
            val visualY = itemCenters[i] - fractionalShift
            val halfH = itemSpacing / 2f
            if (y >= visualY - halfH && y <= visualY + halfH) {
                return scrollOffset + i
            }
        }
        return -1
    }
}