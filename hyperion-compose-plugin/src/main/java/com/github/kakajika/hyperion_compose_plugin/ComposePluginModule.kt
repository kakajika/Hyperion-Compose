package com.github.kakajika.hyperion_compose_plugin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.ComposeView
import com.willowtreeapps.hyperion.plugin.v1.HyperionMenu
import com.willowtreeapps.hyperion.plugin.v1.OverlayContainer
import com.willowtreeapps.hyperion.plugin.v1.PluginModule

abstract class ComposePluginModule : PluginModule() {
    protected lateinit var view: ComposeView

    protected val overlay: OverlayContainer by lazy { extension.overlayContainer }
    protected val overlayView: View? get() = overlay.overlayView
    protected val hyperionMenu: HyperionMenu? get() = extension?.hyperionMenu

    @Composable
    abstract fun PluginViewContent()

    override fun createPluginView(layoutInflater: LayoutInflater, parent: ViewGroup): View? {
        return ComposeView(parent.context).apply {
            setContent {
                currentComposer.collectParameterInformation()
                PluginViewContent()
            }
            view = this
        }
    }

    protected fun reloadPluginView() {
        view.setContent {
            currentComposer.collectParameterInformation()
            PluginViewContent()
        }
    }
}
