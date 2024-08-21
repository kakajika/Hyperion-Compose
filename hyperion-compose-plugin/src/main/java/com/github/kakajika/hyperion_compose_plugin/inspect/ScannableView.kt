package com.github.kakajika.hyperion_compose_plugin.inspect

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.unit.toRect
import com.github.kakajika.hyperion_compose_plugin.inspect.ScannableView.AndroidView
import com.github.kakajika.hyperion_compose_plugin.inspect.ScannableView.ComposeView
import radiography.ScannableView.CallGroupInfo
import radiography.internal.ComposeLayoutInfo
import radiography.internal.ComposeLayoutInfo.AndroidViewInfo
import radiography.internal.ComposeLayoutInfo.LayoutNodeInfo
import radiography.internal.ComposeLayoutInfo.SubcompositionInfo
import radiography.internal.mightBeComposeView

/**
 * Represents a logic view that can be rendered as a node in the view tree.
 *
 * Can either be an actual Android [View] ([AndroidView]) or a grouping of Composables that roughly
 * represents the concept of a logical "view" ([ComposeView]).
 */
public sealed class ScannableView {

  /** The string that be used to identify the type of the view in the rendered output. */
  public abstract val displayName: String

  /** The children of this view. */
  public abstract val children: Sequence<ScannableView>

  public abstract val bounds: Rect
  val width: Int get() = bounds.width.toInt()
  val height: Int get() = bounds.height.toInt()

  public data class AndroidView(public val view: View) : ScannableView() {
    override val displayName: String get() = view::class.java.simpleName
    override val children: Sequence<ScannableView> = view.scannableChildren()
    override val bounds: Rect get() = Rect(
        left = view.left.toFloat(),
        top = view.top.toFloat(),
        right = view.right.toFloat(),
        bottom = view.bottom.toFloat(),
    )

    override fun toString(): String = "${AndroidView::class.java.simpleName}($displayName)"
  }

  /**
   * Represents a group of Composables that make up a logical "view".
   *
   * @param modifiers The list of [Modifier]s that are currently applied to the Composable.
   */
  public data class ComposeView(
    override val displayName: String,
    /**
     * This [ComposeView] represents a collapsed chain of Compose nodes, with the [children] and
     * and other details only coming from nodes that emit values. For simplicity, the "Call" nodes
     * (ie, Compose functions that don't emit values) are not directly included in the
     * [children] of this [ComposeView]; however, the top most "Call" node provides the [displayName]
     * of this [ComposeView].
     *
     * This [callChain] provides the complete chain of Compose Call Groups wrapping the ultimate layout
     * node, which you can use to see granular data about the exact compose hierarchy.
     */
    public val callChain: List<CallGroupInfo>,
    override val bounds: Rect,
    public val modifiers: List<Modifier>,
    public val semanticsNodes: List<SemanticsNode>,
    override val children: Sequence<ScannableView>,
    public val isSubcomposition: Boolean,
  ) : ScannableView()

  /**
   * Indicates that an exception was thrown while rendering part of the tree.
   * This should be used for non-fatal errors, when the rest of the tree should still be processed.
   *
   * By default, exceptions thrown during rendering will abort the entire rendering process, and
   * return the error message along with any portion of the tree that was rendered before the
   * exception was thrown.
   */
  public class ChildRenderingError(private val message: String) : ScannableView() {
    override val displayName: String get() = message
    override val children: Sequence<ScannableView> get() = emptySequence()
    override val bounds: Rect get() =
      throw UnsupportedOperationException("ChildRenderingError has no bounds")
  }
}

private fun View.scannableChildren(): Sequence<ScannableView> = sequence {
  if (mightBeComposeView) {
    val (composableViews, parsedComposables) = getComposeScannableViews(this@scannableChildren)
    // If unsuccessful, the list will contain a RenderError, so yield it anyway.
    yieldAll(composableViews)
    if (parsedComposables) {
      // Don't visit children ourselves, the compose renderer will have done that.
      return@sequence
    }
  }

  if (this@scannableChildren !is ViewGroup) return@sequence

  for (i in 0 until childCount) {
    // Child may be null, if children were removed by another thread after we captured the child
    // count. getChildAt returns null for invalid indices, it doesn't throw.
    val view = getChildAt(i) ?: continue
    yield(AndroidView(view))
  }
}

internal fun ComposeLayoutInfo.toScannableView(): ScannableView = when (val layoutInfo = this) {
  is LayoutNodeInfo -> ComposeView(
    displayName = layoutInfo.name,
    callChain = layoutInfo.callChain,
    bounds = layoutInfo.bounds.toRect(),
    modifiers = layoutInfo.modifiers,
    semanticsNodes = layoutInfo.semanticsNodes,
    children = layoutInfo.children.map(ComposeLayoutInfo::toScannableView),
    isSubcomposition = false,
  )

  is SubcompositionInfo -> ComposeView(
    displayName = layoutInfo.name,
    callChain = layoutInfo.callChain,
    bounds = layoutInfo.bounds.toRect(),
    modifiers = emptyList(),
    semanticsNodes = emptyList(),
    children = layoutInfo.children.map(ComposeLayoutInfo::toScannableView),
    isSubcomposition = true,
  )

  is AndroidViewInfo -> AndroidView(layoutInfo.view)
}
