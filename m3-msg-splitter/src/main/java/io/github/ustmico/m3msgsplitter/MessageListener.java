package io.github.ustmico.m3msgsplitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class MessageListener {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${kafka.outputTopic}")
    private String outputTopic;

    @KafkaListener(topics = "${kafka.inputTopic}")
    public void receive(String payload) {
        log.info("received payload='{}'", payload);
        //TODO Add splitting
        kafkaTemplate.send(outputTopic, payload.toUpperCase());
    }
}
