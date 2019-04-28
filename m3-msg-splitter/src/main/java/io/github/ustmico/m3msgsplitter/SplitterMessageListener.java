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
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
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

    @Value(value = "${splitter.groovyScript}")
    private String groovyScript;

    public static final ObjectMapper objectMapper = new ObjectMapper();

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
                        sendMessagePart(cloudEvent, jsonPart);
                    }
                } else {
                    log.error("Could not split the message:" + payload.toString());
                    //TODO implement invalid message topic
                }
            }else{
                try {
                    Binding binding = new Binding();
                    binding.setVariable("cloudEvent", cloudEvent);
                    binding.setVariable("jsonData", objectMapper.writeValueAsString(payload));
                    log.debug("Using groovyScript:"+groovyScript);
                    GroovyShell shell = new GroovyShell(binding);
                    List<String> result = (List<String>) shell.evaluate(groovyScript);
                    for(final String jsonPart : result){
                        JsonNode jsonNode = objectMapper.readTree(jsonPart);
                        sendMessagePart(cloudEvent, jsonNode);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Could not get valid json from payload",e);
                } catch (IOException e) {
                    log.error("Could not get valid json groovy script",e);
                }
            }
        }
    }

    private void sendMessagePart(CloudEvent<JsonNode> cloudEvent, JsonNode jsonPart) {
        log.info("Building message with content:" + jsonPart.toString());
        final CloudEvent<JsonNode> cloudEventPart = new CloudEventBuilder<JsonNode>()
                .type(cloudEvent.getType())
                .id(cloudEvent.getId())
                .source(cloudEvent.getSource())
                .data(jsonPart)
                .time(cloudEvent.getTime().get())
                .contentType(cloudEvent.getContentType().get())
                .build();
        log.info("Build CloudEvent:" + cloudEventPart.toString());
        kafkaTemplate.send(outputTopic, cloudEventPart);
    }
}
