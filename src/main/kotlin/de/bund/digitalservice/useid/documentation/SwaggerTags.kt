package de.bund.digitalservice.useid.documentation

import io.swagger.v3.oas.annotations.tags.Tag

@Tag(
    name = "Widget",
    description = "Those endpoints are called by the web widget included as an iframe in the eServer web page.",
)
annotation class WidgetTag

@Tag(
    name = "eID-Client",
    description = "Those endpoints are called by the eID-Client, i.e. the BundesIdent app.",
)
annotation class EIDClientTag

@Tag(
    name = "eService",
    description = "Those endpoints are called by the eService.",
)
annotation class EServiceTag

@Tag(
    name = "Refresh",
    description = "The refresh endpoint redirects the caller to the refresh address of the respective service.",
)
annotation class RefreshTag
