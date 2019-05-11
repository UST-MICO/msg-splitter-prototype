package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class Handler implements com.openfaas.model.IHandler {
	
	//Move to environment variable
	private static final String groovyScript = "import groovy.json.JsonSlurper\r\nimport groovy.json.JsonOutput\r\n\r\nprint jsonData\r\ndef jsonSlurper = new JsonSlurper()\r\ndef jsonObject = jsonSlurper.parseText(jsonData)\r\ndef items = [];\r\njsonObject.items.each{ \r\n    item -> item.orderNumber = jsonObject.orderNumber\r\n    items.add(item)\r\n}\r\nprintln items\r\nreturn JsonOutput.toJson(items)";

    public IResponse Handle(IRequest req) {
		String jsonBody = req.getBody();
		Binding binding = new Binding();
        binding.setVariable("jsonData", jsonBody);
		GroovyShell shell = new GroovyShell(binding);
		String result = (String) shell.evaluate(groovyScript);
        Response res = new Response();
	    res.setBody(result);
	    return res;
    }
}
