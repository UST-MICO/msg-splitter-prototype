package io.github.ustmico.m3msgsplitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import io.github.ustmico.m3msgsplitter.kafka.CloudEventExtensionImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class SplitterMessageListener {


    @Autowired
    private KafkaTemplate<String, CloudEventExtensionImpl<JsonNode>> kafkaTemplate;

    @Value(value = "${kafka.outputTopic}")
    private String outputTopic;

    @Value(value = "${splitter.splittingMode}")
    private SplittingMode splittingMode;

    @Value(value = "${splitter.openFaaSFunction}")
    private String openFaaSFunction;

    @Value(value = "${splitter.groovyScript}")
    private String groovyScript;

    public static final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.inputTopic}")
    public void receive(CloudEventExtensionImpl<JsonNode> cloudEvent) {
        log.info("received payload='{}'", cloudEvent);
        if (cloudEvent.getData().isPresent()) {
            JsonNode payload = cloudEvent.getData().get();
            log.info("Payload:" + payload);
            log.info(cloudEvent.getData().get().getClass().toString());
            String sequenceId = UUID.randomUUID().toString();
            if (SplittingMode.SIMPLE.equals(splittingMode)) {
                sendJsonArray(cloudEvent, payload, sequenceId);
            } else if (SplittingMode.ADVANCED.equals(splittingMode)) {
                try {
                    Binding binding = new Binding();
                    binding.setVariable("cloudEvent", cloudEvent);
                    binding.setVariable("jsonData", objectMapper.writeValueAsString(payload));
                    log.debug("Using groovyScript:" + groovyScript);
                    GroovyShell shell = new GroovyShell(binding);
                    List<String> result = (List<String>) shell.evaluate(groovyScript);
                    int sequenceSize = result.size();
                    for (int i = 0; i < result.size(); i++) {
                        JsonNode jsonNode = objectMapper.readTree(result.get(i));
                        sendMessagePart(cloudEvent, jsonNode, sequenceId, i, sequenceSize);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Could not get valid json from payload", e);
                } catch (IOException e) {
                    log.error("Could not get valid json groovy script", e);
                }
            } else if (SplittingMode.OPEN_FAAS.equals(splittingMode)) {
                RestTemplate restTemplate = new RestTemplate();
                try {
                    //The function sends a json array as response
                    //TODO implement error handling
                    String response = restTemplate.postForObject(openFaaSFunction, objectMapper.writeValueAsString(payload), String.class);
                    log.info("OpenFaaS function response: {}", response);
                    JsonNode jsonArray = objectMapper.readTree(response);
                    sendJsonArray(cloudEvent, jsonArray, sequenceId);
                } catch (JsonProcessingException e) {
                    log.error("Could not get valid json from payload", e);
                } catch (IOException e) {
                    log.error("Could not get valid json groovy script", e);
                }
            }
        }
    }

    private void sendJsonArray(CloudEventExtensionImpl<JsonNode> cloudEvent, JsonNode payload, String sequenceId) {
        if (payload.isArray()) {
            int sequenceSize = payload.size();
            for (int i = 0; i < sequenceSize; i++) {
                sendMessagePart(cloudEvent, payload.get(i), sequenceId, i, sequenceSize);
            }
        } else {
            log.error("Could not split the message:" + payload.toString());
            //TODO implement invalid message topic
        }
    }

    private void sendMessagePart(CloudEventExtensionImpl<JsonNode> cloudEvent, JsonNode jsonPart, String sequenceId, int sequenceNumber, int sequenceSize) {
        log.info("Building message with content:" + jsonPart.toString());
        //TODO add a better solution when https://github.com/cloudevents/sdk-java/issues/31 is fixed
        MessageSplitterExtension messageSplitterExtension = new MessageSplitterExtension(sequenceId, sequenceNumber, sequenceSize);
        CloudEventExtensionImpl<JsonNode> cloudEventPart = new CloudEventExtensionImpl<JsonNode>(cloudEvent.getType(), cloudEvent.getSpecVersion(),
                cloudEvent.getSource(), cloudEvent.getId(), cloudEvent.getTime().orElse(ZonedDateTime.now()), cloudEvent.getSchemaURL().orElse(null),
                cloudEvent.getContentType().orElse("application/json"), jsonPart, Collections.singletonList(messageSplitterExtension));
        log.info("Build CloudEvent:" + cloudEventPart.toString());
        kafkaTemplate.send(outputTopic, cloudEventPart);
    }
}
