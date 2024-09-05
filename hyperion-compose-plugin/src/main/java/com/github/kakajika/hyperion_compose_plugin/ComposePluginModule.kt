package com.github.kakajika.hyperion_compose_plugin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.willowtreeapps.hyperion.plugin.v1.HyperionMenu
import com.willowtreeapps.hyperion.plugin.v1.OverlayContainer
import com.willowtreeapps.hyperion.plugin.v1.PluginModule

abstract class ComposePluginModule : PluginModule() {
    protected lateinit var view: FrameLayout

    protected val overlay: OverlayContainer by lazy { extension.overlayContainer }
    protected val overlayView: View? get() = overlay.overlayView
    protected val hyperionMenu: HyperionMenu? get() = extension?.hyperionMenu

    @Composable
    abstract fun PluginViewContent()

    override fun createPluginView(layoutInflater: LayoutInflater, parent: ViewGroup): View? {
        return FrameLayout(parent.context).apply {
            id = View.generateViewId()
            addView(createPluginViewContent(parent.context))
            view = this
        }
    }

    private fun createPluginViewContent(context: Context): View {
        return ComposeView(context).apply {
            setContent {
                PluginViewContent()
            }
        }
    }

    protected fun reloadPluginView() {
        view.removeAllViews()
        view.addView(createPluginViewContent(context))
    }

    override fun isStandalone(): Boolean {
        return false
    }
}
