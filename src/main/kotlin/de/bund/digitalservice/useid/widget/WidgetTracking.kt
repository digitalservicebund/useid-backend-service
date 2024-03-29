package de.bund.digitalservice.useid.widget

import org.springframework.stereotype.Component

@Component
class WidgetTracking {

    class Categories {
        val widget = "Widget"
    }

    class Actions {
        val loaded = "loaded"
        val buttonPressed = "buttonPressed"
        val error = "error"
    }

    class Names {
        val widget = "widget"
        val startIdent = "startIdent"
        val incompatible = "unsupportedOS"
        val fallback = "fallback"
    }

    val categories = Categories()
    val actions = Actions()
    val names = Names()
}
