package main;

import java.io.*;
import java.net.*;

public class GETClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 4567;

    public static void main(String[] args) {
        String data = retrieveWeatherData(SERVER_ADDRESS + ":" + PORT);
        if (data != null) {
            System.out.println("Received weather data:\n" + data);
        } else {
            System.out.println("Failed to retrieve data.");
        }
    }

    // Method to retrieve weather data from the AggregationServer
    public static String retrieveWeatherData(String serverAddress) {
        String[] serverDetails = serverAddress.split(":");
        String host = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);

        StringBuilder responseBuilder = new StringBuilder();
        try (Socket socket = new Socket(host, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            LamportClock clock = new LamportClock();

            // Send GET request to the server
            out.println("GET /weather.json HTTP/1.1");
            out.println("User-Agent: ATOMClient/1.0");
            out.println(clock.getTime());
            out.println();

            // Read the response from the server
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                responseBuilder.append(responseLine).append("\n");
            }

            return responseBuilder.toString().trim();

        } catch (IOException e) {
            return null;
        }
    }
}

