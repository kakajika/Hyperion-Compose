package com.github.kakajika.hyperion_measurement_compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.github.kakajika.hyperion_compose_plugin.ComposePluginModule
import com.github.kakajika.hyperion_measurement_compose.overlay.MeasurementOverlayView
import com.willowtreeapps.hyperion.plugin.v1.MenuState
import com.willowtreeapps.hyperion.plugin.v1.OnMenuStateChangedListener
import com.willowtreeapps.hyperion.plugin.v1.OnOverlayViewChangedListener

internal class ComposeMeasurementModule : ComposePluginModule(),
    OnOverlayViewChangedListener,
    OnMenuStateChangedListener {
    private val isSelected = mutableStateOf<Boolean>(false)

    override fun onCreate() {
        overlay.addOnOverlayViewChangedListener(this)
        hyperionMenu?.addOnMenuStateChangedListener(this)
    }

    @Composable
    override fun PluginViewContent() {
        ComposeMeasurementPluginView(
            isSelected = isSelected.value,
            onClick = ::onClick,
        )
    }

    override fun createPluginView(layoutInflater: LayoutInflater, parent: ViewGroup): View? {
        return super.createPluginView(layoutInflater, parent).also {
            isSelected.value = OVERLAY_TAG == overlayView?.tag
        }
    }

    override fun onDestroy() {
        overlay.removeOnOverlayViewChangedListener(this)
        hyperionMenu?.removeOnMenuStateChangedListener(this)
    }

    private fun onClick() {
        if (!isSelected.value) {
            val newOverlay = MeasurementOverlayView(context).apply {
                tag = OVERLAY_TAG
            }
            overlay.setOverlayView(newOverlay)
        } else {
            overlay.removeOverlayView()
        }
    }

    override fun onOverlayViewChanged(v: View?) {
        isSelected.value = OVERLAY_TAG == v?.tag
    }

    override fun onMenuStateChanged(menuState: MenuState) {
        if (menuState == MenuState.OPEN) {
            reloadPluginView()
        }
        if (menuState == MenuState.CLOSE) {
            if (isSelected.value) {
                val newOverlay = MeasurementOverlayView(context).apply {
                    tag = OVERLAY_TAG
                }
                overlay.setOverlayView(newOverlay)
            }
        }
    }

    companion object {
        private const val OVERLAY_TAG = "compose_measurement_overlay"
    }
}
