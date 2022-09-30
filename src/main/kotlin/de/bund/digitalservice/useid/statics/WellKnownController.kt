package de.bund.digitalservice.useid.statics

import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

private const val IOS_APPID = "VDTVKQ35RL.de.bund.digitalservice.UseID"
private const val IOS_APPID_PREVIEW = "VDTVKQ35RL.de.bund.digitalservice.UseID-Preview"
private const val IOS_PATH_URL = "/eID-Client"

private const val ANDROID_RELATION = "delegate_permission/common.handle_all_urls"
private const val ANDROID_NAMESPACE = "android_app"
private const val ANDROID_PACKAGE = "de.digitalService.useID"
private const val ANDROID_PACKAGE_FINGERPRINT = "38:3A:63:60:60:BE:0B:E9:40:AB:F8:67:EA:BB:64:C8:91:99:0B:DC:01:D8:3D:34:89:A6:29:E4:1D:3B:85:9F"
private const val ANDROID_PACKAGE_PREVIEW = "de.digitalService.useID.Preview"
private const val ANDROID_PACKAGE_PREVIEW_FINGERPRINT = "15:7E:42:A7:92:6B:A5:CA:9E:B0:29:8E:88:EE:81:0D:C3:13:E9:B5:84:41:50:28:8A:88:17:B4:14:40:FB:42"

@Controller
class WellKnownController {

    private val log = KotlinLogging.logger {}

    @GetMapping(
        path = [".well-known/apple-app-site-association"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAppleAppSiteAssociation(): Mono<ResponseEntity<IOSUniversalLink>> {
        val appIds = listOf(IOS_APPID, IOS_APPID_PREVIEW)
        val components = listOf(Component(IOS_PATH_URL))
        val details = listOf(Details(appIds, components))
        val universalLink = UniversalLink(details)

        return Mono.just(
            ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(IOSUniversalLink(universalLink))
        )
            .doOnError {
                log.error("Failed to return iOS Universal Link config", it)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().build()
            )
    }

    @GetMapping(
        path = [".well-known/assetlinks.json"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAndroidAppSiteAssociation(): Mono<ResponseEntity<AndroidAppLink>> {
        val androidApp = AndroidAppLinkItem(
            listOf(ANDROID_RELATION),
            AppTarget(
                ANDROID_NAMESPACE,
                ANDROID_PACKAGE,
                listOf(ANDROID_PACKAGE_FINGERPRINT)
            )
        )
        val androidAppPreview = AndroidAppLinkItem(
            listOf(ANDROID_RELATION),
            AppTarget(
                ANDROID_NAMESPACE,
                ANDROID_PACKAGE_PREVIEW,
                listOf(ANDROID_PACKAGE_PREVIEW_FINGERPRINT)
            )
        )
        val androidAppLink = AndroidAppLink()
        androidAppLink.add(androidApp)
        androidAppLink.add(androidAppPreview)

        return Mono.just(
            ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(androidAppLink)
        )
            .doOnError {
                log.error("Failed to return Android App Links config", it)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().build()
            )
    }
}
