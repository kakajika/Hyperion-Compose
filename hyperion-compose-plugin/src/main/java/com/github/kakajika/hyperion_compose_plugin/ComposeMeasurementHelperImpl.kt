package com.github.kakajika.hyperion_compose_plugin

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import com.github.kakajika.hyperion_compose_plugin.inspect.ScannableView


class ComposeMeasurementHelperImpl(
    private val container: ScannableView.AndroidView
) : ComposeMeasurementHelper {
    private val composeRoot = when {
        container.view is ComposeView -> {
            val androidComposeView = container.children.first()
            androidComposeView.children.first()
        }
        else -> container
    }

    override fun traverse(view: View, action: (View) -> Unit) {
        action(view)
        view.children.forEach { child ->
            traverse(child, action)
        }
    }

    override fun getParentRelativeRect(view: View): Rect {
        return view.bounds
    }

    override fun getRelativeLeft(view: View): Float {
        return view.bounds.left
    }

    override fun getRelativeTop(view: View): Float {
        return view.bounds.top
    }

    override fun getRelativeRight(view: View): Float {
        return view.bounds.right
    }

    override fun getRelativeBottom(view: View): Float {
        return view.bounds.bottom
    }

    override fun getScreenLocation(view: View) {

    }

    override fun getContentRootLocation(view: View, outRect: android.graphics.Rect) {
        val bounds = view.bounds
        val rootBounds = composeRoot.bounds
        outRect.set(
            (bounds.left - rootBounds.left).toInt(),
            (bounds.top - rootBounds.top).toInt(),
            (bounds.right - rootBounds.left).toInt(),
            (bounds.bottom - rootBounds.top).toInt(),
        )
    }

    fun getContentRootLocation(view: View): Rect {
        val bounds = view.bounds
        val rootBounds = composeRoot.bounds
        return Rect(
            left = bounds.left - rootBounds.left,
            top = bounds.top - rootBounds.top,
            right = bounds.right - rootBounds.left,
            bottom = bounds.bottom - rootBounds.top,
        )
    }

    override fun hitTest(x: Float, y: Float): View {
        return findTarget(container, Offset(x, y)) ?: container
    }

    private fun findTarget(view: View, touchPoint: Offset, skipRoot: Boolean = false): View? {
        // we consider the "best target" to be the view width the smallest width / height
        // whose location on screen is within the given touch area.
        var bestTarget: View? = if (skipRoot) null else view
        view.children.forEach { child ->
            val isSubcomposition = child is ScannableView.ComposeView && child.isSubcomposition
            if (!child.isVisible() && !isSubcomposition) return@forEach
            if (isSubcomposition || getContentRootLocation(child).contains(touchPoint)) {
                val target = findTarget(child, touchPoint, skipRoot = isSubcomposition)
                if (bestTarget == null) {
                    bestTarget = target
                } else if (target != null &&
                    target.width <= bestTarget!!.width &&
                    target.height <= bestTarget!!.height) {
                    bestTarget = target
                }
            }
        }
        return bestTarget
    }
}

fun View.isVisible() = when (this) {
    is ScannableView.AndroidView -> view.isVisible
    is ScannableView.ComposeView -> !bounds.isEmpty
    else -> false
}
