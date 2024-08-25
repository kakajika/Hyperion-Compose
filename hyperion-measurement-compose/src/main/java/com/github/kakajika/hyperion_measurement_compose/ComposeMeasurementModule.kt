package com.github.kakajika.hyperion_measurement_compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.kakajika.hyperion_compose_plugin.ComposePluginModule
import com.github.kakajika.hyperion_measurement_compose.overlay.MeasurementOverlayView
import com.github.kakajika.hyperion_measurement_compose.theme.HyperionBlue
import com.github.kakajika.hyperion_measurement_compose.theme.HyperionText
import com.willowtreeapps.hyperion.plugin.v1.MenuState
import com.willowtreeapps.hyperion.plugin.v1.OnMenuStateChangedListener
import com.willowtreeapps.hyperion.plugin.v1.OnOverlayViewChangedListener

class ComposeMeasurementModule : ComposePluginModule(),
    OnOverlayViewChangedListener,
    OnMenuStateChangedListener {
    override fun onCreate() {
        overlay.addOnOverlayViewChangedListener(this)
        hyperionMenu?.addOnMenuStateChangedListener(this)
    }

    @Composable
    override fun PluginViewContent() {
        val tint = if (view.isSelected) HyperionBlue else HyperionText
        Row(
            modifier = Modifier
                .height(131.dp)
                .fillMaxWidth()
                .clickable(onClick = ::onClick)
                .padding(dimensionResource(id = com.willowtreeapps.hyperion.plugin.R.dimen.hype_plugin_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier
                    .padding(end = 28.dp)
                    .size(dimensionResource(id = com.willowtreeapps.hyperion.plugin.R.dimen.hype_plugin_icon_size)),
                painter = painterResource(id = R.drawable.ic_measurement),
                colorFilter = ColorFilter.tint(tint),
                contentDescription = "Compose Measurement",
            )
            Column {
                Text(
                    text = "Compose Measurement",
                    fontSize = 16.sp,
                    color = tint,
                )
                Text(
                    text = "Tap Composables to measure the distances between them.",
                    color = tint,
                )
            }
        }
    }

    override fun createPluginView(layoutInflater: LayoutInflater, parent: ViewGroup): View? {
        return super.createPluginView(layoutInflater, parent).also {
            view.isSelected = OVERLAY_TAG == overlayView?.tag
        }
    }

    override fun onDestroy() {
        overlay.removeOnOverlayViewChangedListener(this)
        hyperionMenu?.removeOnMenuStateChangedListener(this)
    }

    override fun isStandalone(): Boolean {
        return false
    }

    private fun onClick() {
        if (!view.isSelected) {
            val newOverlay = MeasurementOverlayView(context).apply {
                tag = OVERLAY_TAG
            }
            overlay.setOverlayView(newOverlay)
        } else {
            overlay.removeOverlayView()
        }
    }

    override fun onOverlayViewChanged(v: View?) {
        view.isSelected = OVERLAY_TAG == v?.tag
        reloadPluginView()
    }

    override fun onMenuStateChanged(menuState: MenuState) {
        if (menuState == MenuState.CLOSE) {
            if (view.isSelected) {
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
