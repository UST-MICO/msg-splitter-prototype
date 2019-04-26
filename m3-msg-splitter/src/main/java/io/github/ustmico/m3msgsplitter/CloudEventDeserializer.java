package io.github.ustmico.m3msgsplitter;

import io.cloudevents.CloudEvent;
import io.cloudevents.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class CloudEventDeserializer implements Deserializer<CloudEvent> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public CloudEvent<Object> deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            String message = new String(data, StandardCharsets.UTF_8);
            log.info("Trying to parse the message:" + message);
            return Json.decodeCloudEvent(message);
        } catch (IllegalStateException e) {
            throw new SerializationException("Could not create an CloudEvent message", e);
        }
    }

    @Override
    public void close() {

    }
}
