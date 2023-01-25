package de.bund.digitalservice.useid.tracking.matomo

import org.springframework.context.ApplicationEvent

class MatomoEvent(source: Any, val category: String, val action: String, val name: String, val sessionId: String?, val userAgent: String?) : ApplicationEvent(source)
