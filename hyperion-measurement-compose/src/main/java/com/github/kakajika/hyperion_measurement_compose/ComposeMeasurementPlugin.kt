package com.github.kakajika.hyperion_measurement_compose

import com.google.auto.service.AutoService
import com.willowtreeapps.hyperion.plugin.v1.Plugin
import com.willowtreeapps.hyperion.plugin.v1.PluginModule

@AutoService(Plugin::class)
class ComposeMeasurementPlugin : Plugin() {
    override fun createPluginModule(): PluginModule {
        return ComposeMeasurementModule()
    }
}
