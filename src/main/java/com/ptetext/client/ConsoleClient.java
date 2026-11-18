package com.ptetext.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Scanner;



public class ConsoleClient {
    
    static private final String BASE= "http://localhost:8080/";
    static private final HttpClient client = HttpClient.newHttpClient();
    static private final ObjectMapper mapper = new ObjectMapper();
    
    static private final Scanner in = new Scanner(System.in);
    
    static private String sessionId, myName;
    static private int myId;

    public static void main(String[] args) throws Exception {
        System.out.println("PTEText console client");
        
        String body = mapper.writeValueAsString(Map.of("username", "alice", "password", "password123"));
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create("http://localhost:8080/api/auth/login"))
                                        .header("Content-Type", "application/json")
                                        .POST(HttpRequest.BodyPublishers.ofString(body))
                                        .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        
        //login success check
        if(response.statusCode() != 200){
            System.out.println("Login failed. "+ response.body());
            return;
        }
        
        JsonNode root = mapper.readTree(response.body());
        sessionId = root.get("sessionId").asText();
        myName = root.path("user").path("displayName").asText();
        myId = root.path("user").path("userId").asInt();
        
        System.out.println("Logged in as "+ myName);
        
    }
}
