package com.github.kakajika.hyperion_measurement_compose.overlay

import com.github.kakajika.hyperion_compose_plugin.inspect.ScannableView

fun ScannableView.isEquivalent(other: ScannableView?): Boolean = when (this) {
    is ScannableView.AndroidView -> {
        if (other !is ScannableView.AndroidView) {
            false
        } else {
            this.view == other.view
        }
    }
    is ScannableView.ComposeView -> {
        if (other !is ScannableView.ComposeView) {
            false
        } else {
            displayName == other.displayName &&
                    bounds == other.bounds &&
                    callChain == other.callChain &&
                    children.toList().isEquivalent(other.children.toList())
        }
    }
    else -> false
}

fun List<ScannableView>.isEquivalent(other: List<ScannableView>): Boolean {
    if (size != other.size) {
        return false
    }
    return zip(other).all { (a, b) -> a.isEquivalent(b) }
}
