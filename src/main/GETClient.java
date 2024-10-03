package main;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;

public class GETClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 4567;

    public static void main(String[] args) {
        System.out.println("GETClient: Attempting to connect to server at " + SERVER_ADDRESS + ":" + PORT);
        String data = retrieveWeatherData(SERVER_ADDRESS + ":" + PORT);
        if (data != null) {
            System.out.println("GETClient: Received weather data:\n" + data);
        } else {
            System.out.println("GETClient: Failed to retrieve data.");
        }
    }

    // Retrieve weather data from the server and return it as a JSON string
    public static String retrieveWeatherData(String serverAddress) {
        String[] serverDetails = serverAddress.split(":");
        String host = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);

        StringBuilder responseBuilder = new StringBuilder();
        try (Socket socket = new Socket(host, port)) {
            System.out.println("GETClient: Connected to server " + host + " on port " + port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            LamportClock clock = new LamportClock();
            System.out.println("GETClient: Sending GET request with Lamport clock value: " + clock.getTime());

            out.println("GET /weather.json HTTP/1.1");
            out.println("User-Agent: ATOMClient/1/0");
            out.println(clock.getTime());
            out.println(); // Empty line to indicate end of headers

            // Read and store the weather data in a StringBuilder
            System.out.println("GETClient: Waiting for response...");
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                responseBuilder.append(responseLine).append("\n");
            }

            System.out.println("GETClient: Finished reading response.");
            return responseBuilder.toString().trim(); // Return the gathered response

        } catch (IOException e) {
            System.out.println("GETClient: Failed to retrieve data from server: " + e.getMessage());
            return null;
        }
    }
}


