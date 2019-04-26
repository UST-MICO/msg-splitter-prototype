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
    public void receive(CloudEvent<JsonNode> cloudEvent) {
        log.info("received payload='{}'", cloudEvent);
        if (cloudEvent.getData().isPresent()) {
            JsonNode payload = cloudEvent.getData().get();
            log.info("Payload:"+payload);
            log.info(cloudEvent.getData().get().getClass().toString());
           if (simpleMode) {
               if(payload.isArray()){
                   for (final JsonNode jsonPart : payload) {
                       log.debug("Send message:" + jsonPart.toString());
                       kafkaTemplate.send(outputTopic, jsonPart.toString());
                   }
               }else{
                   log.error("Could not split the message:"+payload.toString());
                   //TODO implement invalid message topic
               }
            }
        }
    }
}
