package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler implements com.openfaas.model.IHandler {
	
	Logger log = LoggerFactory.getLogger(this.getClass().getName());
	//Move to environment variable
	private static final String groovyScript = "import groovy.json.JsonSlurper\r\nimport groovy.json.JsonOutput\r\n\r\nprint jsonData\r\ndef jsonSlurper = new JsonSlurper()\r\ndef jsonObject = jsonSlurper.parseText(jsonData)\r\ndef items = [];\r\njsonObject.items.each{ \r\n    item -> item.orderNumber = jsonObject.orderNumber\r\n    items.add(item)\r\n}\r\nprintln items\r\nreturn JsonOutput.toJson(items)";

    public IResponse Handle(IRequest req) {
		String jsonBody = req.getBody();
		log.info("Request Body: {}",jsonBody);
		Binding binding = new Binding();
        binding.setVariable("jsonData", jsonBody);
		GroovyShell shell = new GroovyShell(binding);
		String result = (String) shell.evaluate(groovyScript);
		log.info("Result is:" + result);
        Response res = new Response();
	    res.setBody(result);
	    return res;
    }
}
