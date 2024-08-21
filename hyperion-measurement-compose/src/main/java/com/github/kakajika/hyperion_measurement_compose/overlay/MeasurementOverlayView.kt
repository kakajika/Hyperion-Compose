package com.github.kakajika.hyperion_measurement_compose.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.text.TextPaint
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.Px
import androidx.compose.ui.graphics.toArgb
import androidx.core.util.TypedValueCompat
import com.github.kakajika.hyperion_compose_plugin.ComposeMeasurementHelper
import com.github.kakajika.hyperion_compose_plugin.ComposeMeasurementHelperImpl
import com.github.kakajika.hyperion_compose_plugin.View
import com.github.kakajika.hyperion_compose_plugin.inspect.ScannableView
import com.github.kakajika.hyperion_measurement_compose.R
import com.github.kakajika.hyperion_measurement_compose.theme.HyperionBlue
import com.willowtreeapps.hyperion.plugin.v1.ExtensionProvider

internal class MeasurementOverlayView(context: Context) : FrameLayout(context) {
    private val contentRoot: View
    private val measurementHelper: ComposeMeasurementHelper
    private val path = Path()
    private val outRect = Rect()
    private val paintDashed: Paint
    private val paintPrimary: Paint
    private val paintSecondary: Paint
    private val paintText: TextPaint

    @Px
    private val measurementTextOffset: Int
    private var currentView: View? = null
    private var rectPrimary: Rect = Rect()
    private var rectSecondary: Rect = Rect()
    private var measurementWidthText: TextView? = null
    private var measurementHeightText: TextView? = null

    /* Used for nested measurementHelper */
    private var measurementLeftText: TextView? = null
    private var measurementTopText: TextView? = null
    private var measurementRightText: TextView? = null
    private var measurementBottomText: TextView? = null

    init {
        val extension = ExtensionProvider.get(context)
        val contentRootView = extension.contentRoot
        contentRoot = ScannableView.AndroidView(contentRootView)
        measurementHelper = ComposeMeasurementHelperImpl(contentRoot)
        paintDashed = Paint()
        paintDashed.setColor(HyperionBlue.toArgb())
        paintDashed.style = Paint.Style.STROKE
        paintDashed.strokeWidth = 4f
        paintDashed.setPathEffect(DashPathEffect(floatArrayOf(10f, 20f), 0f))
        paintPrimary = Paint()
        paintPrimary.setColor(HyperionBlue.toArgb())
        paintPrimary.style = Paint.Style.STROKE
        paintPrimary.strokeWidth = 6f
        paintSecondary = Paint()
        paintSecondary.setColor(HyperionBlue.toArgb())
        paintSecondary.style = Paint.Style.STROKE
        paintSecondary.strokeWidth = 6f
        paintText = TextPaint()
        paintText.setColor(HyperionBlue.toArgb())
        paintText.textSize = 45f
        paintText.style = Paint.Style.FILL_AND_STROKE
        paintText.strokeWidth = 2f
        measurementTextOffset = resources.getDimensionPixelSize(R.dimen.hm_measurement_text_offset)
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        if (currentView == null || rectPrimary.isZero()) {
            return
        }

        //rectangle around target
        canvas.drawRect(rectPrimary, paintPrimary)

        /*
         * guidelines
         */
        // top-left to top
        path.reset()
        path.moveTo(rectPrimary.left.toFloat(), 0f)
        path.lineTo(rectPrimary.left.toFloat(), rectPrimary.top.toFloat())
        canvas.drawPath(path, paintDashed)

        // top-right to top
        path.reset()
        path.moveTo(rectPrimary.right.toFloat(), 0f)
        path.lineTo(rectPrimary.right.toFloat(), rectPrimary.top.toFloat())
        canvas.drawPath(path, paintDashed)

        // top-left to left
        path.reset()
        path.moveTo(0f, rectPrimary.top.toFloat())
        path.lineTo(rectPrimary.left.toFloat(), rectPrimary.top.toFloat())
        canvas.drawPath(path, paintDashed)

        // bottom-left to left
        path.reset()
        path.moveTo(0f, rectPrimary.bottom.toFloat())
        path.lineTo(rectPrimary.left.toFloat(), rectPrimary.bottom.toFloat())
        canvas.drawPath(path, paintDashed)

        // bottom-left to bottom
        path.reset()
        path.moveTo(rectPrimary.left.toFloat(), rectPrimary.bottom.toFloat())
        path.lineTo(rectPrimary.left.toFloat(), bottom.toFloat())
        canvas.drawPath(path, paintDashed)

        // bottom-right to bottom
        path.reset()
        path.moveTo(rectPrimary.right.toFloat(), rectPrimary.bottom.toFloat())
        path.lineTo(rectPrimary.right.toFloat(), bottom.toFloat())
        canvas.drawPath(path, paintDashed)

        // bottom-right to right
        path.reset()
        path.moveTo(rectPrimary.right.toFloat(), rectPrimary.bottom.toFloat())
        path.lineTo(right.toFloat(), rectPrimary.bottom.toFloat())
        canvas.drawPath(path, paintDashed)

        // top-right to right
        path.reset()
        path.moveTo(rectPrimary.right.toFloat(), rectPrimary.top.toFloat())
        path.lineTo(right.toFloat(), rectPrimary.top.toFloat())
        canvas.drawPath(path, paintDashed)
        if (rectSecondary.isZero()) {
            // draw width measurement text
            measurementWidthText?.let { text ->
                canvas.save()
                canvas.translate(
                    (rectPrimary.centerX() - text.width / 2).toFloat(),
                    (rectPrimary.top - text.height - measurementTextOffset).toFloat()
                )
                text.draw(canvas)
                canvas.restore()
            }

            // draw height measurement text
            measurementHeightText?.let { text ->
                canvas.save()
                canvas.translate(
                    (rectPrimary.right + measurementTextOffset).toFloat(),
                    (rectPrimary.bottom - rectPrimary.height() / 2 - text.height / 2).toFloat()
                )
                text.draw(canvas)
                canvas.restore()
            }
        } else {
            canvas.drawRect(rectSecondary, paintSecondary)

            /*
             * view to view measurement views
             */if (rectPrimary.bottom < rectSecondary.top) {
                // secondary is below. draw vertical line.
                canvas.drawLine(
                    rectSecondary.centerX().toFloat(),
                    rectPrimary.bottom.toFloat(),
                    rectSecondary.centerX().toFloat(),
                    rectSecondary.top.toFloat(),
                    paintPrimary
                )
                measurementHeightText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        (rectSecondary.centerX() - text.width / 2).toFloat(),
                        ((rectPrimary.bottom + rectSecondary.top) / 2 - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }
            }
            if (rectPrimary.right < rectSecondary.left) {
                // secondary is right. draw horizontal line.
                canvas.drawLine(
                    rectPrimary.right.toFloat(),
                    rectSecondary.centerY().toFloat(),
                    rectSecondary.left.toFloat(),
                    rectSecondary.centerY().toFloat(),
                    paintPrimary
                )
                measurementWidthText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        ((rectPrimary.right + rectSecondary.left) / 2 - text.width / 2).toFloat(),
                        (rectSecondary.centerY() - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }
            }
            if (rectSecondary.bottom < rectPrimary.top) {
                // secondary is above. draw vertical line.
                canvas.drawLine(
                    rectSecondary.centerX().toFloat(),
                    rectPrimary.top.toFloat(),
                    rectSecondary.centerX().toFloat(),
                    rectSecondary.bottom.toFloat(),
                    paintPrimary
                )
                measurementHeightText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        (rectSecondary.centerX() - text.width / 2).toFloat(),
                        ((rectPrimary.top + rectSecondary.bottom) / 2 - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }
            }
            if (rectSecondary.right < rectPrimary.left) {
                // secondary is left. draw horizontal line.
                canvas.drawLine(
                    rectPrimary.left.toFloat(),
                    rectSecondary.centerY().toFloat(),
                    rectSecondary.right.toFloat(),
                    rectSecondary.centerY().toFloat(),
                    paintPrimary
                )
                measurementWidthText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        ((rectPrimary.left + rectSecondary.right) / 2 - text.width / 2).toFloat(),
                        (rectSecondary.centerY() - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }
            }

            // check nested
            var inside: Rect? = null
            var outside: Rect? = null
            if (rectPrimary.contains(rectSecondary)) {
                outside = rectPrimary
                inside = rectSecondary
            } else if (rectSecondary.contains(rectPrimary)) {
                outside = rectSecondary
                inside = rectPrimary
            }

            /*
             * parent to child measurement views
             */if (inside != null && outside != null) {
                // left inside
                canvas.drawLine(
                    outside.left.toFloat(),
                    inside.centerY().toFloat(),
                    inside.left.toFloat(),
                    inside.centerY().toFloat(),
                    paintPrimary
                )
                measurementLeftText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        ((outside.left + inside.left) / 2 - text.width / 2).toFloat(),
                        (inside.centerY() - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }

                // right inside
                canvas.drawLine(
                    outside.right.toFloat(),
                    inside.centerY().toFloat(),
                    inside.right.toFloat(),
                    inside.centerY().toFloat(),
                    paintPrimary
                )
                measurementRightText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        ((outside.right + inside.right) / 2 - text.width / 2).toFloat(),
                        (inside.centerY() - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }

                // top inside
                canvas.drawLine(
                    inside.centerX().toFloat(),
                    outside.top.toFloat(),
                    inside.centerX().toFloat(),
                    inside.top.toFloat(),
                    paintPrimary
                )
                measurementTopText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        (inside.centerX() - text.width / 2).toFloat(),
                        ((outside.top + inside.top) / 2 - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }

                // bottom inside
                canvas.drawLine(
                    inside.centerX().toFloat(),
                    outside.bottom.toFloat(),
                    inside.centerX().toFloat(),
                    inside.bottom.toFloat(),
                    paintPrimary
                )
                measurementBottomText?.let { text ->
                    canvas.save()
                    canvas.translate(
                        (inside.centerX() - text.width / 2).toFloat(),
                        ((outside.bottom + inside.bottom) / 2 - text.height / 2).toFloat()
                    )
                    text.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y
            val touchTarget = measurementHelper.hitTest(x, y)

            // reset selection if target is root or same target.
            if (touchTarget.isEquivalent(contentRoot) || touchTarget.isEquivalent(currentView)) {
                currentView = null
                rectPrimary = Rect()
                rectSecondary = Rect()
            } else if (rectPrimary.isZero()) {
                setPrimaryTarget(touchTarget)
            } else {
                setSecondaryTarget(touchTarget)
            }
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun findTarget(root: View, x: Float, y: Float): View {
        // we consider the "best target" to be the view width the smallest width / height
        // whose location on screen is within the given touch area.
        var bestTarget = root
        for (child in root.children) {
            measurementHelper.getContentRootLocation(child, outRect)
            if (child is ScannableView.AndroidView && child.view.visibility != VISIBLE) {
                continue
            }
            if (child is ScannableView.ComposeView && child.isSubcomposition) {
                child.children.forEach { v ->
                    findTarget(v, x, y)
                }
            }
            if (outRect.contains(x.toInt(), y.toInt())) {
                val target = findTarget(child, x, y)
                if (target.width <= bestTarget.width && target.height <= bestTarget.height) {
                    bestTarget = target
                }
            }
        }
        return bestTarget
    }

    private fun setPrimaryTarget(view: View) {
        currentView = view
        rectPrimary = Rect()
        measurementHelper.getContentRootLocation(view, rectPrimary)
        setWidthMeasurementText(rectPrimary.width())
        setHeightMeasurementText(rectPrimary.height())
    }

    private fun setSecondaryTarget(view: View) {
        rectSecondary = Rect()
        measurementHelper.getContentRootLocation(view, rectSecondary)
        if (rectPrimary.bottom < rectSecondary.top) {
            setHeightMeasurementText(rectSecondary.top - rectPrimary.bottom)
        }
        if (rectPrimary.right < rectSecondary.left) {
            setWidthMeasurementText(rectSecondary.left - rectPrimary.right)
        }
        if (rectSecondary.bottom < rectPrimary.top) {
            setHeightMeasurementText(rectPrimary.top - rectSecondary.bottom)
        }
        if (rectSecondary.right < rectPrimary.left) {
            setWidthMeasurementText(rectPrimary.left - rectSecondary.right)
        }

        // check nested
        var inside: Rect? = null
        var outside: Rect? = null
        if (rectPrimary.contains(rectSecondary)) {
            outside = rectPrimary
            inside = rectSecondary
        } else if (rectSecondary.contains(rectPrimary)) {
            outside = rectSecondary
            inside = rectPrimary
        }
        if (inside != null && outside != null) {
            setLeftMeasurementText(inside.left - outside.left)
            setTopMeasurementText(inside.top - outside.top)
            setRightMeasurementText(outside.right - inside.right)
            setBottomMeasurementText(outside.bottom - inside.bottom)
        }
    }

    private fun setWidthMeasurementText(@Px measurement: Int) {
        if (measurement <= 0) {
            measurementWidthText = null
            return
        }
        measurementWidthText = makeMeasurementView(measurement)
    }

    private fun setHeightMeasurementText(@Px measurement: Int) {
        if (measurement <= 0) {
            measurementHeightText = null
            return
        }
        measurementHeightText = makeMeasurementView(measurement)
    }

    private fun setLeftMeasurementText(@Px measurement: Int) {
        if (measurement <= 0) {
            measurementLeftText = null
            return
        }
        measurementLeftText = makeMeasurementView(measurement)
    }

    private fun setTopMeasurementText(@Px measurement: Int) {
        if (measurement <= 0) {
            measurementTopText = null
            return
        }
        measurementTopText = makeMeasurementView(measurement)
    }

    private fun setRightMeasurementText(@Px measurement: Int) {
        if (measurement <= 0) {
            measurementRightText = null
            return
        }
        measurementRightText = makeMeasurementView(measurement)
    }

    private fun setBottomMeasurementText(@Px measurement: Int) {
        if (measurement <= 0) {
            measurementBottomText = null
            return
        }
        measurementBottomText = makeMeasurementView(measurement)
    }

    /**
     * Prepares a TextView to display a measurement
     * Centers text
     * Sets text with "dp" suffix
     * Sets selection color as text color
     * Sets TextView background
     * Lays out view
     *
     * @param measurement The measurement to display in the created TextView
     */
    private fun makeMeasurementView(@Px measurement: Int): TextView {
        val tv = TextView(context)
        tv.setGravity(Gravity.CENTER)
        val text = TypedValueCompat
            .pxToDp(measurement.toFloat(), resources.displayMetrics)
            .toInt()
            .toString() + "dp"
        tv.text = text
        tv.setTextColor(HyperionBlue.toArgb())
        tv.setBackgroundResource(R.drawable.hm_rounded_measurement)
        layoutTextView(tv)
        return tv
    }

    /**
     * Lays out textview to (text dimensions + padding) * system font scale
     * Padding based on screen density
     *
     * @param tv The TextView to layout
     */
    private fun layoutTextView(tv: TextView) {
        val fontScale = resources.configuration.fontScale

        /*
         * Multiplier for padding based on phone density
         * Density of 2 does not need padding
         */
        val densityMultiplier = context.resources.displayMetrics.density - 2
        val bounds = Rect()
        paintText.getTextBounds(
            tv.getText().toString().toCharArray(),
            0,
            tv.getText().length,
            bounds
        )
        tv.layout(
            0,
            0,
            ((bounds.width() + 33 * densityMultiplier) * fontScale).toInt(),
            ((bounds.height() + 22 * densityMultiplier) * fontScale).toInt()
        )
    }

    fun Rect.isZero() = this.left == 0
            && this.top == 0
            && this.right == 0
            && this.bottom == 0
}