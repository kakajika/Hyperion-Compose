@file:OptIn(ExperimentalRadiographyComposeApi::class)

package com.github.kakajika.hyperion_compose_plugin

import androidx.annotation.MainThread
import androidx.compose.ui.geometry.Rect
import com.github.kakajika.hyperion_compose_plugin.inspect.ScannableView
import com.willowtreeapps.hyperion.plugin.v1.PluginExtension
import radiography.ExperimentalRadiographyComposeApi

typealias View = ScannableView

@MainThread
interface ComposeMeasurementHelper {
    @MainThread
    fun getParentRelativeRect(view: View): Rect

    @MainThread
    fun getRelativeLeft(view: View): Float

    @MainThread
    fun getRelativeTop(view: View): Float

    @MainThread
    fun getRelativeRight(view: View): Float

    @MainThread
    fun getRelativeBottom(view: View): Float

    @MainThread
    fun getScreenLocation(view: View)

    fun traverse(view: View, action: (View) -> Unit)

    /**
     * Returns the view's coordinates relative to the content root
     * ([PluginExtension.getContentRoot]).
     */
    @MainThread
    fun getContentRootLocation(view: View, outRect: android.graphics.Rect)

    @MainThread
    fun hitTest(x: Float, y: Float): View
}
