package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.config.EventConfig
import de.bund.digitalservice.useid.config.EventStatusConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.lang.NonNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks.Many
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("/v1")
class PingController {
    @Autowired
    @NonNull
    private val eventNotifications: Many<EventConfig.SuccessEvent>? = null

    @GetMapping(
        path = ["/sse"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun ping(): Flux<ServerSentEvent<String>> {
        val uuid: String = UUID.randomUUID().toString()
        val initialEvent = Flux.just(
            ServerSentEvent.builder<String>()
                .event(EventStatusConfig.READY.eventName)
                .data(uuid)
                .build()
        )
        return initialEvent.concatWith(
            Flux.interval(Duration.ofSeconds(1))
                .takeUntilOther(eventNotifications!!.asFlux())
                .takeUntil { seq: Long -> seq >= 5 }
                .map { seq: Long ->
                    Tuples.of(seq, uuid)
                }
                .map { data: Tuple2<Long, String> ->
                    ServerSentEvent.builder<String>()
                        .event(EventStatusConfig.IN_PROGRESS.eventName)
                        .id(data.t1.toString())
                        .data(uuid)
                        .build()
                }
                .concatWith(
                    Mono.just(
                        ServerSentEvent.builder<String>()
                            .event(EventStatusConfig.FINISHED.eventName)
                            .data(uuid)
                            .build()
                    )
                )
        )
    }

    @PostMapping("/success")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun success(): Mono<Any> {
        println("==> SUCCESS")
        eventNotifications!!.tryEmitNext(EventConfig.SuccessEvent(this))
        return Mono.empty()
    }
}
