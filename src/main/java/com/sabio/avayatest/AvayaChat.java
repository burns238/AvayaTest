package com.sabio.avayatest;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AvayaChat {

	private static Logger LOG = LoggerFactory.getLogger(AvayaChat.class);

	private String baseAvayaURI = "http://10.0.254.120";
	private String avayaPort = ":9609";
	private String loginEndpoint = "/csportal/v1/login";
	private String chatCommandEndpoint = "/csportal/cometd/";
	private String handshakeEndpoint = "/csportal/cometd/handshake";
	private String escalateMediaEndpoint = "/csportal/v1/escalatemedia";
	private String pollingEndpoint = "/csportal/cometd/connect";

	private Boolean chatAvailable = false;
	
	private String set_cookie = "";

	private String callId = "";
	private String clientId = "";

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

		RestTemplate restTemplate = new RestTemplate();
		
		String URI = baseAvayaURI + avayaPort + loginEndpoint;
		String body = "{" + "  \"userrole\": \"guest\"," + "  \"username\": \"Guest\","
				+ "  \"language\": \"en\"," + "  \"tenant\": \"DefaultTenant\"" + "}";
		LOG.info(body);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");      
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
		set_cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		LOG.info(response.getBody().toString());
		LOG.info("login finished");

	}

	private void escalateMedia() {

		LOG.info("escalateMedia called");

		String URI = baseAvayaURI + avayaPort + escalateMediaEndpoint;
		String body = "{" + "\"displayname\": \"Mike Burns\"," + "\"mediatype\": \"LiveChat\","
				+ "\"calltype\" : \"chatter\","
				+ "\"question\": \"Is this working\", \"sendemailto\": \"jondoe@aicavaya.com\", \"sendtranscript\": true,"
				+ "\"eduvalues\": {" + "    \"eduname1\": \"eduvalue1\"," + "    \"eduname2\": \"eduvalue2\","
				+ "    \"eduname3\": \"eduvalue3\"" + "} }";
		HttpHeaders headers = initialiseHeaders();
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
		LOG.info(response.getBody().toString());
		LOG.info("escalateMedia finished");

	}

	private HttpHeaders initialiseHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("cookie", set_cookie);
		return headers;
	}

	private void handshake() throws IOException {

		LOG.info("handshake called");

		String URI = baseAvayaURI + avayaPort + handshakeEndpoint;
		String body = "{\"version\":\"1.0\",\"minimumVersion\":\"1.0\",\"channel\":\"/meta/handshake\",\"supportedConnectionTypes\":[\"long-polling\",\"callback-polling\"],\"advice\":{\"timeout\":60000,\"interval\":0},\"id\":\"2\"}";
		HttpHeaders headers = initialiseHeaders(); 
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
		JSONArray jsonArray = new JSONArray(response.getBody());
		clientId = jsonArray.getJSONObject(0).getString("clientId");
		LOG.info(response.getBody().toString());
		LOG.info("handshake finished");

	}

	private void openConnection(String clientId) {

		LOG.info("openConnection called");

		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\","
				+ "\"data\":{\"command\":\"open\",\"time\":\"1427499550221\",\"timezoneoffset"
				+ "\":\"-330\"},\"id\":\"3\",\"clientId\":\"" + clientId + "\"}";
		HttpHeaders headers = initialiseHeaders();      
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
		LOG.info(response.getBody().toString());
		LOG.info("openConnection finished");

	}

	private void initiateCall(String clientId) {

		LOG.info("initiateCall called");

		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\",\"data\":{\"command\":\"callinitiate\"},\"id\":\"7\",\"clientId\":\""
				+ clientId + "\"}";
		HttpHeaders headers = initialiseHeaders();     
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
		LOG.info(response.getBody().toString());
		LOG.info("initiateCall finished");

	}

	private void getCallId(String clientId) throws IOException {

		LOG.info("getCallId called");

		String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
		String body = "{\"channel\":\"/service/csportalchat\",\"data\":{\"command\":\"chatsend\",\"callid\":\"5515ea1f000000009493ae0a"
				+ "235c0002\",\"message\":\"\"},\"id\":\"19\",\"clientId\":\"" + clientId + "\"}";
		HttpHeaders headers = initialiseHeaders();     
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);

		JSONArray jsonArray = new JSONArray(response.getBody());
		LOG.info(jsonArray.toString());
		String dataString = jsonArray.getJSONObject(0).getString("data");
		callId = new JSONObject(dataString).getString("callId");
		LOG.info(response.getBody().toString());
		LOG.info("getCallId finished");

	}

	public ArrayList<String> sendMessage(String message) {

		LOG.info("sendMessage called");
		ArrayList<String> messages = new ArrayList<String>();

		if (chatAvailable) {

			String URI = baseAvayaURI + avayaPort + chatCommandEndpoint;
			String body = "{\"channel\":\"/service/csportalchat\",\"data\":{\"command\":\"chatsend\",\"callid\":\""
					+ callId + "\",\"message\":\"\"},\"id\":\"19\",\"clientId\":\"" + clientId + "\"}";
			HttpHeaders headers = initialiseHeaders();      
			HttpEntity<String> request = new HttpEntity<>(body, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
			LOG.info(response.getBody().toString());
			messages = handleEvents(response);
		} else {
			LOG.info("Agent not available");
			messages.add("Agent not available");
		}
		LOG.info("sendMessage finished");
		return messages;
	}

	public ArrayList<String> pollEvents() {

		String URI = baseAvayaURI + avayaPort + pollingEndpoint;
		String body = " {\"channel\":\"/meta/connect\",\"connectionType\":\"long-polling\","
				+ "\"advice\":{\"timeout\":0},\"id\":\"5\",\"clientId\":\"" + clientId + "\"}";
		HttpHeaders headers = initialiseHeaders();     
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(URI, request, String.class);
		LOG.info(response.getBody().toString());
		return handleEvents(response);

	}

	ArrayList<String> handleEvents(ResponseEntity<String> response) {

		ArrayList<String> messages = new ArrayList<String>();

		JSONArray jsonArray = new JSONArray(response.getBody());

		for (int i = 0; i < jsonArray.length(); i++) {

			String event = jsonArray.getJSONObject(i).getJSONObject("data").getString("event");

			if (event.equals("callDisconnected")) {
				LOG.info("Chat disconnected");
				messages.add("disconnected");
			} else if (event.equals("personEntered")) {
				LOG.info("The agent has joined the chat");
				messages.add("chatAvailable");
			} else {
				String message = jsonArray.getJSONObject(0).getJSONObject("data").getJSONObject("chatmesg")
						.getString("message");
				messages.add(message);
				LOG.info(message);
			}
		}

		return messages;
	}

	public AvayaChat() {
		super();
	}

}
