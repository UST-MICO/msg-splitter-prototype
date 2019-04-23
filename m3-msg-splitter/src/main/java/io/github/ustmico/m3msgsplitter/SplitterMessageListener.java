package io.github.ustmico.m3msgsplitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import javax.json.*;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class SplitterMessageListener {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${kafka.outputTopic}")
    private String outputTopic;

    @Value(value = "${kafka.simpleMode}")
    private boolean simpleMode;

    @KafkaListener(topics = "${kafka.inputTopic}")
    public void receive(String payload) {
        log.info("received payload='{}'", payload);
        List<String> splitPayload = new LinkedList<>();

        if(simpleMode){
            try(JsonReader jsonReader = Json.createReader(new StringReader(payload))){
                JsonArray jsonPayloadArray = jsonReader.readArray();
                log.info("Json array size:" + jsonPayloadArray.size());
                for(JsonValue jsonSplit : jsonPayloadArray){
                    splitPayload.add(jsonSplit.toString());
                }
            }
        }
        for(String messagePayload : splitPayload){
            log.debug("Send message:"+ messagePayload);
            kafkaTemplate.send(outputTopic, messagePayload);
        }
    }
}
