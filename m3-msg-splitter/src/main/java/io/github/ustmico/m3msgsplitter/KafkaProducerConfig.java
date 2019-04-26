package io.github.ustmico.m3msgsplitter;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.CloudEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {


    @Value(value = "${kafka.bootstrapServers}")
    private String bootstrapAddress;

    @Bean
    public ProducerFactory<String, CloudEvent<JsonNode>> producerFactory() {
        Map<String, Object> configProps = putConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<Object, Object> invalidMessageProducerFactory() {
        Map<String, Object> configProps = putConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private Map<String, Object> putConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                CloudEventSerializer.class);
        return configProps;
    }

    @Bean
    public KafkaTemplate<String, CloudEvent<JsonNode>> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<Object, Object> invalidMessageTemplate() {
        return new KafkaTemplate<Object, Object>(invalidMessageProducerFactory());
    }
}
