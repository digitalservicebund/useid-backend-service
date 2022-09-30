package de.bund.digitalservice.useid.statics

import com.fasterxml.jackson.annotation.JsonProperty

data class IOSUniversalLink(
    @JsonProperty("applinks")
    val universalLink: UniversalLink
)

data class UniversalLink(
    @JsonProperty("details")
    val details: List<Details>
)

data class Details(
    @JsonProperty("appIDs")
    val appIDs: List<String>,
    @JsonProperty("components")
    val components: List<Component>
)

data class Component(
    @JsonProperty("/")
    val pathUrlName: String
)
