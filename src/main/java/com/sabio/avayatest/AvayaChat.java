package com.sabio.avayatest;

import java.io.IOException;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AvayaChat {
	
	private static Logger LOG = LoggerFactory.getLogger(AvayaChat.class);
	
	private String baseAvayaURI = "https://10.0.254.120";
	private String avayaPort = ":9609";
	private String loginEndpoint = "/v1/login";
	private String chatCommandEndpoint = "/csportal/cometd/";
	private String handshakeEndpoint = "/csportal/cometd/handshake";
	private String escalateMediaEndpoint = "/v1/escalatemedia";
	
	ObjectMapper mapper = new ObjectMapper();
	String callId = "";
	String clientId = "";
	
	@Autowired
	private RestTemplate restTemplate;
	
	public void chatInitiation() throws IOException {
		
		login();
		escalateMedia();
		
		handshake();
		openConnection(clientId);
		initiateCall(clientId);
		
		getCallId(clientId);
	}
	
	private void login() {
		
		LOG.info("login called");
		
		String URI = baseAvayaURI + avayaPort + loginEndpoint;
		String body = "{\n" + 
				"  \"userrole\": \"guest\",\n" + 
				"  \"username\": \"Guest\",\n" + 
				"  \"language\": \"en\",\n" + 
				"  \"tenant\": \"DefaultTenant\"\n" + 
				"}";
		restTemplate.postForEntity(URI, body, String.class);
		
		LOG.info("login finished");
		
	}
	
	private void escalateMedia() {
		
		LOG.info("escalateMedia called");
		
		String URI = baseAvayaURI + avayaPort + escalateMediaEndpoint;
		String body = "{\n" + 
				"\"displayname\": \"Mike Burns\",\n" + 
				"\"mediatype\": \"LiveChat\",\n" + 
				"\"calltype\" : \"chatter\",\n" + 
				"\"question\": \"Is this working\", \"sendemailto\": \"jondoe@aicavaya.com\", \"sendtranscript\": true,\n" + 
				"\"eduvalues\": {\n" + 
				"    \"eduname1\": \"eduvalue1\",\n" + 
				"    \"eduname2\": \"eduvalue2\",\n" + 
				"    \"eduname3\": \"eduvalue3\"\n" + 
				"} }";
		restTemplate.postForEntity(URI, body, String.class);
		
		LOG.info("escalateMedia finished");
		
	}
	
	private void handshake() throws IOException {
		
		LOG.info("handshake called");
		
		String URI = baseAvayaURI + avayaPort + handshakeEndpoint;
		String body = "{version\":\"1.0\","
				+ "\"minimumVersion\":\"1.0\","
				+ "\"channel\":\"/meta/handshake\","
				+ "\"supportedConnectionTypes\":[\"long-polling\",\"callback-polling\"],"
				+ "\"advice\":{\"timeout\":60000,\"interval\":0},"
				+ "\"id\":\"2\"}";
		ResponseEntity<String> response = restTemplate.postForEntity(URI, body, String.class);
		
		JSONArray jsonArray = new JSONArray(response.getBody());
		clientId = jsonArray.getJSONObject(0).getString("clientId");
		
		LOG.info("handshake finished");

	}
	
	private void openConnection(String clientId) {

		LOG.info("openConnection called");
		
		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\","
				+ "\"data\":{\"command\":\"open\",\"time\":\"1427499550221\",\"timezoneoffset\n" + 
				"\":\"-330\"},\"id\":\"3\",\"clientId\":\""+clientId+"\"}";
		restTemplate.postForEntity(URI, body, String.class);
		
		LOG.info("openConnection finished");
		
	}
	
	private void initiateCall(String clientId) {
		
		LOG.info("initiateCall called");
		
		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\",\"data\":{\"command\":\"callinitiate\"},\"id\":\"7\",\"clientId\":\""
				+ clientId + "\"}";
		restTemplate.postForEntity(URI, body, String.class);
		
		LOG.info("initiateCall finished");
		
	}

	private void getCallId(String clientId) throws IOException {
	
		LOG.info("getCallId called");
		
		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\",\"data\":{\"command\":\"chatsend\",\"callid\":\"5515ea1f000000009493ae0a\n" + 
				"235c0002\",\"message\":\"\"},\"id\":\"19\",\"clientId\":\"" + clientId + "\"}";
		ResponseEntity<String> response = restTemplate.postForEntity(URI, body, String.class);
		
		JSONArray jsonArray = new JSONArray(response.getBody());
		callId = jsonArray.getJSONObject(0).getJSONObject("data").getString("clientId");
		
		LOG.info("getCallId finished");

	}
	
	public void sendMessage(String message) {
		
		LOG.info("sendMessage called");
		
		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\",\"data\":{\"command\":\"chatsend\",\"callid\":\"" +
		        callId + "\",\"message\":\"\"},\"id\":\"19\",\"clientId\":\"" + clientId + "\"}";
		restTemplate.postForEntity(URI, body, String.class);
		
		LOG.info("sendMessage finished");
		
	}
	
	
	public void listen(String clientId, String callId) {
		
		Boolean finished = false;
		
		do {
			
			
			
		} while(!finished);
		
		
	}


	public AvayaChat() {
		super();
	}
	
}
