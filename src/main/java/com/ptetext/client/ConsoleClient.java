package com.ptetext.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Scanner;



public class ConsoleClient {
    
    static private final String BASE= "http://localhost:8080/";
    
    
    static private final Scanner in = new Scanner(System.in);
    
    static private String sessionId, myName;
    static private int myId;

    public static void main(String[] args) throws Exception {
        System.out.println("PTEText console client");
        
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        
        String body = mapper.writeValueAsString(Map.of("username", "alice", "password", "password123"));
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create("http://localhost:8080/api/auth/login"))
                                        .header("Content-Type", "application/json")
                                        .POST(HttpRequest.BodyPublishers.ofString(body))
                                        .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
}
