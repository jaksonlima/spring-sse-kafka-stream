package com.br.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.util.Objects;

@RestController
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private final ConnectableFlux<ServerSentEvent<Object>> eventPublisher;
    private final ApplicationContext context;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConsumerProperties kafkaConsumerProperties;

    public Controller(
            final ConnectableFlux<ServerSentEvent<Object>> eventPublisher,
            final ApplicationContext context,
            final KafkaTemplate<String, Object> kafkaTemplate,
            final KafkaConsumerProperties kafkaConsumerProperties
    ) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.context = Objects.requireNonNull(context);
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.kafkaConsumerProperties = Objects.requireNonNull(kafkaConsumerProperties);
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<?> subscribe() {
        eventPublisher.doOnSubscribe(subscription -> {
                    log.info("[ON_SUBSCRIBE]");
                })
                .doOnCancel(() -> {
                    log.info("[ON_CANCEL]");
                })
                .doOnError(e -> {
                    log.error("[ON_ERROR= {}]", e.toString());
                })
                .doFinally(signalType -> {
                    log.info("[FINALLY] [SIGNAL_TYPE= {}]", signalType.name());
                });

        return eventPublisher;
    }

    @GetMapping(path = "/shutdown")
    public ResponseEntity<?> shutdown() {
        ((ConfigurableApplicationContext) context).close();

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/published")
    public ResponseEntity<?> published(@RequestBody Object message) {
        kafkaTemplate.send(kafkaConsumerProperties.getTopic(), message);

        return ResponseEntity.noContent().build();
    }

}
