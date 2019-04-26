package io.github.ustmico.m3msgsplitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
public class SplitterMessageListener {

    public SplitterMessageListener() {
        log.info("SplitterMessageListener");
        log.info("outputTopic" + outputTopic);
    }

    @Value(value = "${kafka.bootstrapServers}")
    private String adresse;

    @PostConstruct
    public void printTopic() {
        log.info("SplitterMessageListener");
        log.info("outputTopic" + outputTopic);
        log.info("adresse " + adresse);
    }

    @Autowired
    private KafkaTemplate<String, CloudEvent<JsonNode>> kafkaTemplate;

    @Value(value = "${kafka.outputTopic}")
    private String outputTopic;

    @Value(value = "${kafka.simpleMode}")
    private boolean simpleMode;

    @KafkaListener(topics = "${kafka.inputTopic}")
    public void receive(CloudEvent<JsonNode> cloudEvent) {
        log.info("received payload='{}'", cloudEvent);
        if (cloudEvent.getData().isPresent()) {
            JsonNode payload = cloudEvent.getData().get();
            log.info("Payload:" + payload);
            log.info(cloudEvent.getData().get().getClass().toString());
            if (simpleMode) {
                if (payload.isArray()) {
                    for (final JsonNode jsonPart : payload) {
                        log.info("Send message:" + jsonPart.toString());
                        final CloudEvent<JsonNode> cloudEventPart = new CloudEventBuilder<JsonNode>()
                                .type(cloudEvent.getType())
                                .id(cloudEvent.getId())
                                .source(cloudEvent.getSource())
                                .data(jsonPart)
                                .time(cloudEvent.getTime().get())
                                .build();
                        log.info("Build CloudEvent:" + cloudEventPart.toString());
                        kafkaTemplate.send(outputTopic, cloudEventPart);

                    }
                } else {
                    log.error("Could not split the message:" + payload.toString());
                    //TODO implement invalid message topic
                }
            }
        }
    }
}
