# msg-splitter-prototype

Provides a basic prototype implementation of https://www.enterpriseintegrationpatterns.com/patterns/messaging/Sequencer.html

# Example

This component reads messages in the [CloudEvent](https://github.com/cloudevents/spec/blob/v0.2/json-format.md) format from an input topic and splits them.
At the moment only data properties which consist of a JSON array can be split in separate messages. The separate parts are then sent to an output topic
as separate CloudEvents.

## Example 1:
**Input:**
```JSON
{ "specversion" : "0.2", "type" : "com.example.someevent", "source" : "/mycontext", "id" : "C234-1234-1234", "time" : "2018-04-05T17:31:00Z", "contentType" : "application/json", "data" :  [ "Content1", "Content2", "Content3" ]  }
```
**Output:**
```JSON
{"type":"com.example.someevent","source":"/mycontext","id":"C234-1234-1234","time":"2018-04-05T17:31:00Z","schemaURL":null,"contentType":"application/json","data":"Content1","specVersion":"0.2"}
```
```JSON
{"type":"com.example.someevent","source":"/mycontext","id":"C234-1234-1234","time":"2018-04-05T17:31:00Z","schemaURL":null,"contentType":"application/json","data":"Content2","specVersion":"0.2"}
```
```JSON
{"type":"com.example.someevent","source":"/mycontext","id":"C234-1234-1234","time":"2018-04-05T17:31:00Z","schemaURL":null,"contentType":"application/json","data":"Content3","specVersion":"0.2"}
```

## 