package com.br.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.core.publisher.ConnectableFlux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;

import java.util.HashMap;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@EnableKafka
@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private final KafkaProducerProperties kafkaProducerProperties;

    public KafkaConfig(
            final KafkaProperties kafkaProperties,
            final KafkaConsumerProperties kafkaConsumerProperties,
            final KafkaProducerProperties kafkaProducerProperties
    ) {
        this.kafkaProperties = Objects.requireNonNull(kafkaProperties);
        this.kafkaConsumerProperties = Objects.requireNonNull(kafkaConsumerProperties);
        this.kafkaProducerProperties = Objects.requireNonNull(kafkaProducerProperties);
    }

    /**
     * Configuração do producer para utlização do
     * KafkaTemplate
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        final var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaProducerProperties.getAck());

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Configuração para utilizar o consumer
     *
     * @KafkaListener
     */
    @Bean
    public KafkaReceiver<String, Object> kafkaReceiver() {
        final var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerProperties.getClientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "%s-%s".formatted(kafkaConsumerProperties.getGroupId(), randomUUID()));
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerProperties.isEnableAutoCommit());

        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getSessionTimeoutMs());
        config.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getDefaultApiTimeoutMs());
        config.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getRequestTimeoutMs());
        config.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, kafkaConsumerProperties.getReconnectBackoffMaxMs());
        config.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, kafkaConsumerProperties.getReconnectBackoffMs());
        config.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaConsumerProperties.getRetryBackoffMs());

        return new DefaultKafkaReceiver<>(
                ConsumerFactory.INSTANCE,
                ReceiverOptions.<String, Object>create(config)
                        .subscription(singletonList(kafkaConsumerProperties.getTopic()))
        );
    }

    /**
     * Configuração de envio de dados
     * <p>
     * ServerSentEvent
     */
    @Bean
    public ConnectableFlux<ServerSentEvent<Object>> eventPublisher(final KafkaReceiver<String, Object> kafkaReceiver) {
        final var eventPublisher = kafkaReceiver.receive()
                .map(consumerRecord -> {

                    final var id = "sse.id";
                    final var comment = "sse event";
                    final var event = "sse.stream";
                    final var value = consumerRecord.value();

                    return ServerSentEvent
                            .builder(value)
                            .id(id)
                            .comment(comment)
                            .event(event)
                            .build();
                })
                .publish();

        eventPublisher.connect();

        return eventPublisher;
    }

}
