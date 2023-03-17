package de.bund.digitalservice.useid.wellknown

import io.micrometer.core.annotation.Timed
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Timed
class WellKnownController(
    wellKnownProperties: WellKnownProperties,
) {
    private val iosConfig = wellKnownProperties.iosConfig
    private val androidConfig = wellKnownProperties.androidConfig

    @GetMapping(
        path = [".well-known/apple-app-site-association"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getAppleAppSiteAssociation(): ResponseEntity<IOSUniversalLink> {
        val appIds = listOf(iosConfig.appId, iosConfig.appIdPreview)
        val components = listOf(Component(iosConfig.pathUrl))
        val details = listOf(Details(appIds, components))
        val universalLink = UniversalLink(details)

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(IOSUniversalLink(universalLink))
    }

    @GetMapping(
        path = [".well-known/assetlinks.json"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getAndroidAppSiteAssociation(): ResponseEntity<AndroidAppLink> {
        val androidApp = AndroidAppLinkItem(
            listOf(androidConfig.relation),
            AppTarget(
                androidConfig.namespace,
                androidConfig.packageDefault.name,
                listOf(androidConfig.packageDefault.fingerprint),
            ),
        )
        val androidAppPreview = AndroidAppLinkItem(
            listOf(androidConfig.relation),
            AppTarget(
                androidConfig.namespace,
                androidConfig.packagePreview.name,
                listOf(androidConfig.packagePreview.fingerprint),
            ),
        )
        val androidAppLink = AndroidAppLink()
        androidAppLink.add(androidApp)
        androidAppLink.add(androidAppPreview)

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(androidAppLink)
    }
}
