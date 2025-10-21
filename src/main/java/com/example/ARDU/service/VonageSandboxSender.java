package com.example.ARDU.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

/**
 * Simple HTTP client to send messages to the Vonage Messages Sandbox.
 * Returns message_uuid on success or throws a RuntimeException with the response body on non-2xx.
 */
@Component
public class VonageSandboxSender {

    private final String apiKey;
    private final String apiSecret;
    private final String sandboxFrom; // e.g. 14157386102 (no +)
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public VonageSandboxSender(
            @Value("${vonage.api.key}") String apiKey,
            @Value("${vonage.api.secret}") String apiSecret,
            @Value("${vonage.sandbox.from-number}") String sandboxFrom
    ) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.sandboxFrom = sandboxFrom != null ? sandboxFrom.trim() : sandboxFrom;
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Send a WhatsApp text via Vonage sandbox endpoint.
     *
     * @param toNumberNoPlus recipient number WITHOUT leading '+' (e.g. 917411619592)
     * @param text           message content
     * @return message_uuid on success
     * @throws Exception on network/parse errors or if sandbox returns non-2xx
     */
    public String sendWhatsAppSandbox(String toNumberNoPlus, String text) throws Exception {
        // Debug print
        System.out.println("SANDBOX SEND: from=" + sandboxFrom + " to=" + toNumberNoPlus + " text=" + text);

        String url = "https://messages-sandbox.nexmo.com/v1/messages";

        JsonNode payload = mapper.createObjectNode()
                .put("from", sandboxFrom)
                .put("to", toNumberNoPlus)
                .put("message_type", "text")
                .put("text", text)
                .put("channel", "whatsapp");

        String json = mapper.writeValueAsString(payload);
        String basicAuth = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic " + basicAuth)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        int status = resp.statusCode();
        String body = resp.body();

        System.out.println("Vonage sandbox status: " + status);
        System.out.println("Vonage sandbox response: " + body);

        if (status >= 200 && status < 300) {
            // parse message_uuid
            JsonNode node = mapper.readTree(body);
            if (node.has("message_uuid")) {
                return node.get("message_uuid").asText();
            } else if (node.has("messages") && node.get("messages").isArray()
                    && node.get("messages").size() > 0 && node.get("messages").get(0).has("message_uuid")) {
                return node.get("messages").get(0).get("message_uuid").asText();
            } else {
                // fallback to returning full body
                return body;
            }
        } else {
            throw new RuntimeException("Vonage sandbox returned status " + status + ": " + body);
        }
    }
}
