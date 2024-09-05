package com.github.kakajika.hyperion_measurement_compose

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
import com.github.kakajika.hyperion_measurement_compose.theme.HyperionBlue
import com.github.kakajika.hyperion_measurement_compose.theme.HyperionText

@Composable
internal fun ComposeMeasurementPluginView(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (isSelected) HyperionBlue else HyperionText
    Row(
        modifier = modifier
            .height(131.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
