package com.ptetext.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsoleClient {

    static private final String BASE = "http://localhost:8080";
    static private final HttpClient client = HttpClient.newHttpClient();
    static private final ObjectMapper mapper = new ObjectMapper();

    static private final Scanner in = new Scanner(System.in);

    static private String sessionId, myName;
    static private int myId;

    public static void main(String[] args) throws Exception {

        //delete later when project is done. Its vibecoded slop for my dumbass
        DevServer.ensureRunning();


        System.out.println("PTEText console client");

        boolean running = true;
        while(running){
            try{
                if(sessionId == null){
                System.out.println("1. Log in\n2. Register\n0. Quit");
                switch(ask("> ")){
                    case "0":
                        running = false;
                        break;
                    case "1":
                        login();
                        break;
                    case "2":
                        System.out.println("Nothing yet.");
                        break;
                    default:
                        System.out.println("Unknown entry.");
                }
                } else{
                    System.out.println("[ "+ myName +" ]");
                    switch(ask("> ")){
                        case "0" -> running = false;
                        case "1" -> System.out.println("Nothing yet.");
                        case "2" -> System.out.println("Nothing yet.");
                        case "3" -> System.out.println("Nothing yet.");
                        case "4" -> System.out.println("Nothing yet.");
                        case "5" -> System.out.println("Nothing yet.");
                        case "6" -> System.out.println("Nothing yet.");
                    }
                }
            }catch (Exception e){
                System.out.println("Error: "+ e.getMessage());
            }
        }

    }

    //call method
    static HttpResponse<String> call(String method, String path, Object body) throws Exception{
        HttpRequest.BodyPublisher publisher;
        if(body == null){
            publisher = HttpRequest.BodyPublishers.noBody();
        } else{
            publisher = HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
        }
       
        
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(BASE + path))
                                        .header("Content-Type", "application/JSON")
                                        .timeout(Duration.ofSeconds(10))
                                        .method(method,publisher)
                                        .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    //error msg method
    static String errorText(HttpResponse<String> r){
        try {
            String errMessage = mapper.readTree(r.body()).path("error").asText();
            if(!errMessage.isEmpty()){
                return errMessage;
            }
        } catch (Exception ignored) {
            System.out.println("Body was empty or not a JSON");
        }
        return "HTTP " + r.statusCode();
    }

    //success check
    static boolean success(HttpResponse<String> r){
        if(r.statusCode() /100 == 2){
            return true;
        }

        if(r.statusCode() == 401 && sessionId != null){
            sessionId = null;
            myName = null;
            myId = 0;
            System.out.println("Session Expired.");
        } else{
            System.out.println("Error: "+errorText(r));
        }
        return false;
    }

    //login method
    static void login() throws Exception{
        String username = ask("Username: ");
        String password = ask("Password: ");
        HttpResponse<String> response = call("POST", "/api/auth/login", 
                                            Map.of("username", username, "password", password));

        if(!success(response)){
            return;
        }

        JsonNode root = mapper.readTree(response.body());
        sessionId = root.get("sessionId").asText();
        myName = root.path("user").path("displayName").asText();
        myId = root.path("user").path("userId").asInt();

        System.out.println("Logged in as " + myName);
    }

    //menu loop
    static String ask(String prompt){
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    static long askNumber(String prompt){
        try {
            return Long.parseLong(ask(prompt));
        } catch (Exception NumberFormatException) {
            return -1;
        }
    }

    
}
