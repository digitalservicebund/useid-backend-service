package de.bund.digitalservice.useid

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks.Many
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.security.SecureRandom
import java.time.Duration

@RestController
class PingController {
    @Autowired
    @NonNull
    private val eventNotifications: Many<SuccessEvent>? = null

    @GetMapping(
        path = ["/sse"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun ping(): Flux<ServerSentEvent<Int>> {
        return Flux.interval(Duration.ofSeconds(1))
            .takeUntilOther(eventNotifications!!.asFlux())
            .map { seq: Long ->
                Tuples.of(
                    seq,
                    SecureRandom().nextInt()
                )
            }
            .map { data: Tuple2<Long, Int> ->
                ServerSentEvent.builder<Int>()
                    .event("ping")
                    .id(data.t1.toString())
                    .data(data.t2)
                    .build()
            }
            .concatWith(
                Mono.just(
                    ServerSentEvent.builder<Int>()
                        .event("close")
                        .data(0)
                        .build()
                )
            )
    }

    @PostMapping("/success")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun success(): Mono<Any> {
        println("==> SUCCESS")
        eventNotifications!!.tryEmitNext(SuccessEvent(this))
        return Mono.empty()
    }
}