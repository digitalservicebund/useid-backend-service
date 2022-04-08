package de.bund.digitalservice.useid

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

@RestController
class PingController {

    @GetMapping(
        path = ["/sse"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun ping(): Flux<ServerSentEvent<Int>> {
        return Flux.interval(Duration.ofSeconds(1))
            .map { seq: Long ->
                Tuples.of(
                    seq,
                    ThreadLocalRandom.current().nextInt()
                )
            }
            .map { data: Tuple2<Long, Int> ->
                ServerSentEvent.builder<Int>()
                    .event("ping")
                    .id(data.t1.toString())
                    .data(data.t2)
                    .build()
            }
    }

    @PostMapping("/success")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun success(): Mono<Any> {
        println("==> SUCCESS")
        return Mono.empty()
    }
}
