# msg-splitter-prototype

Provides a basic prototype implementation of https://www.enterpriseintegrationpatterns.com/patterns/messaging/Sequencer.html

# Example

This component reads messages in the [CloudEvent](https://github.com/cloudevents/spec/blob/v0.2/json-format.md) format from an input topic and splits them.
At the moment only data properties which consist of a JSON array can be split in separate messages using the simple mode. The separate parts are then sent to an output topic
as separate CloudEvents. For more advanced splitting operations use the advanced mode.

## Splitting Modes

### Simple Mode

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

### Advanced Mode (Experimental)

Sometimes it is necessary to put more logic into message splitting than the simple mode allows. For example, some messages are not formatted as a JSON array or the resulting messages
would be incomplete and not self-contained without further modifications.

For example, if an order message should be split into separate items to use parallel processing for the items, but each separate message has to include the order number. The example message could look like this:
```JSON
{
  "specversion": "0.2",
  "type": "com.example.someevent",
  "source": "/mycontext",
  "id": "C234-1234-1234",
  "time": "2018-04-05T17:31:00Z",
  "contentType": "application/json",
  "data": {
    "orderNumber": "A112345443",
    "items": [
      {
        "partName": "Wheel",
        "price": "199",
        "quantity": 4
      },
      {
        "partName": "Bolt",
        "price": "5",
        "quantity": 200
      },
      {
        "partName": "Door",
        "price": "405",
        "quantity": 4
      }
    ]
  }
}
```
The simple mode would not support this message because it is not a JSON array and because it can't transform the content of the message. To support this transformation we included support for
[Apache Groovy](http://groovy-lang.org/).
To allow meaningful splitting it is possible to configure a Groovy script which gets access to the data element and the whole message itself.
The example could be realized with the following script:
```Groovy
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

print jsonData
def jsonSlurper = new JsonSlurper()
def jsonObject = jsonSlurper.parseText(jsonData)
def items = [];
jsonObject.items.each{ 
    item -> item.orderNumber = jsonObject.orderNumber
    items.add(JsonOutput.toJson(item))
}
println items
return items
```
This Groovy script iterates over each item and adds the `orderNumber`. Then it returns the list of items as separate parts. 
The data element of the CloudEvent message is provided for each script via the variable `jsonData` and the whole message is provided via the variable `cloudEvent`.
The component expects a list of strings as a return type. Each string has to be valid JSON. 
The resulting messages look like this:

```JSON
{
  "type": "com.example.someevent",
  "source": "/mycontext",
  "id": "C234-1234-1234",
  "time": "2018-04-05T17:31:00Z",
  "schemaURL": null,
  "contentType": "application/json",
  "data": {
    "partName": "Wheel",
    "price": "199",
    "quantity": 4,
    "orderNumber": "A112345443"
  },
  "specVersion": "0.2"
}
```

```JSON
{
  "type": "com.example.someevent",
  "source": "/mycontext",
  "id": "C234-1234-1234",
  "time": "2018-04-05T17:31:00Z",
  "schemaURL": null,
  "contentType": "application/json",
  "data": {
    "partName": "Bolt",
    "price": "5",
    "quantity": 200,
    "orderNumber": "A112345443"
  },
  "specVersion": "0.2"
}
```

```JSON
{
  "type": "com.example.someevent",
  "source": "/mycontext",
  "id": "C234-1234-1234",
  "time": "2018-04-05T17:31:00Z",
  "schemaURL": null,
  "contentType": "application/json",
  "data": {
    "partName": "Door",
    "price": "405",
    "quantity": 4,
    "orderNumber": "A112345443"
  },
  "specVersion": "0.2"
}
```