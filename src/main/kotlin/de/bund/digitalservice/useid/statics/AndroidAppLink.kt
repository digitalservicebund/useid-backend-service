package de.bund.digitalservice.useid.statics

import com.fasterxml.jackson.annotation.JsonProperty

class AndroidAppLink : ArrayList<AndroidAppLinkItem>()

data class AndroidAppLinkItem(
    @JsonProperty("relation")
    val relation: List<String>,
    @JsonProperty("target")
    val appTarget: AppTarget
)

data class AppTarget(
    @JsonProperty("namespace")
    val namespace: String,
    @JsonProperty("package_name")
    val packageName: String,
    @JsonProperty("sha256_cert_fingerprints")
    val fingerprint: List<String>
)
