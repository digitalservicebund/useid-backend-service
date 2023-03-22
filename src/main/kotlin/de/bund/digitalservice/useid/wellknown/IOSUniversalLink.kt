package de.bund.digitalservice.useid.wellknown

import com.fasterxml.jackson.annotation.JsonProperty

data class IOSUniversalLink(
    @JsonProperty("applinks")
    val universalLink: UniversalLink,
    @JsonProperty("webcredentials")
    val webcredentials: Apps,
)

data class Apps(
    @JsonProperty("apps")
    val details: List<String>,
)

data class UniversalLink(
    @JsonProperty("details")
    val details: List<Details>,
)

data class Details(
    @JsonProperty("appIDs")
    val appIDs: List<String>,
    @JsonProperty("components")
    val components: List<Component>,
)

data class Component(
    @JsonProperty("/")
    val pathUrlName: String,
)
