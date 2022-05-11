package de.bund.digitalservice.useid

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream

@RestController
class QRCodeImageController {
    @GetMapping(
        path = ["/api/v1/qrcode/{imageSize}"],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun generateQRCodeImage(
        @PathVariable imageSize: Int,
        @RequestParam url: String?
    ): Mono<ByteArray> {
        return Mono.just(ByteArrayOutputStream())
            .doOnNext { pngOutputStream ->
                MatrixToImageWriter.writeToStream(
                    QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, imageSize, imageSize),
                    "PNG",
                    pngOutputStream
                )
            }
            .map(ByteArrayOutputStream::toByteArray)
    }
}
