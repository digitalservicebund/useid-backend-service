package de.bund.digitalservice.useid.widget

import org.springframework.stereotype.Component

@Component
class WidgetTracking {

    class Categories {
        val widget = "Widget"
        val abtesting = "abtesting"
    }

    class Actions {
        val loaded = "loaded"
        val buttonPressed = "buttonPressed"
        val error = "error"
        val abtest = "MitigateFallback"
    }

    class Names {
        val widget = "widget"
        val startIdent = "startIdent"
        val incompatible = "unsupportedOS"
        val fallback = "fallback"
        val abtestOriginal = "original"
        val abtestVariation1 = "Variation1"
    }

    val categories = Categories()
    val actions = Actions()
    val names = Names()
}
