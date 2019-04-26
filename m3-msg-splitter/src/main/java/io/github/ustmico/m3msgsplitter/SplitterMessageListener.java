package io.github.ustmico.m3msgsplitter;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

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
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${kafka.outputTopic}")
    private String outputTopic;

    @Value(value = "${kafka.simpleMode}")
    private boolean simpleMode;

    @KafkaListener(topics = "${kafka.inputTopic}")
    public void receive(CloudEvent<Object> cloudEvent) {
        log.info("received payload='{}'", cloudEvent);
        if (cloudEvent.getData().isPresent()) {
            String payload = cloudEvent.getData().get().toString();
            log.info("Payload:"+payload);
            //TODO Add splitter functionality for the cloud event data element
            /*List<String> splitPayload = new LinkedList<>();
            log.info(cloudEvent.getData().get().getClass().toString());
           if (simpleMode) {
                try (JsonReader jsonReader = Json.createReader(new StringReader(payload))) {
                    JsonArray jsonPayloadArray = jsonReader.readArray();
                    log.info("Json array size:" + jsonPayloadArray.size());
                    for (JsonValue jsonSplit : jsonPayloadArray) {
                        splitPayload.add(jsonSplit.toString());
                    }
                }
            }
            for (String messagePayload : splitPayload) {
                log.debug("Send message:" + messagePayload);
                kafkaTemplate.send(outputTopic, messagePayload);
            }*/
        }
    }
}
