package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AggregationServer {
    private static final int PORT = 4567;
    private static final int EXPIRY_TIME = 30000;

    // Store timestamps, weather data and lamport clocks
    private static ConcurrentHashMap<String, Long> dataStore = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> weatherData = new ConcurrentHashMap<>();
    private static LamportClock clock = new LamportClock();

    // Main method to start the AggregationServer
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        // expunge expired data every 30 seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                expungeExpiredData();
            }
        }, 0, EXPIRY_TIME);

        // loop to accept and handle client requests concurrently
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new RequestHandler(clientSocket)).start();
        }
    }

    // Method to remove expired weather data from the data store
    public static synchronized void expungeExpiredData() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : dataStore.entrySet()) {
            if (currentTime - entry.getValue() > EXPIRY_TIME) {
                weatherData.remove(entry.getKey());
                dataStore.remove(entry.getKey());
            }
        }
    }

    // Method to handle invalid HTTP requests
    public static void handleInvalidRequest(PrintWriter out) {
        out.println("HTTP/1.1 400 Bad Request");
        out.println(); // End of headers
        out.flush();
    }

    // Nested class to handle client requests in separate threads
    static class RequestHandler implements Runnable {
        private Socket clientSocket;

        public RequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        // Run method that processes the client's request
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();  // Read the request line
                String header;
                int contentLength = 0;
                int clientClockTime = 0;

                // Process request headers
                while (!(header = in.readLine()).isEmpty()) {
                    if (header.startsWith("Lamport-Clock:")) {
                        clientClockTime = Integer.parseInt(header.split(":")[1].trim());
                        clock.update(clientClockTime);
                    } else if (header.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(header.split(":")[1].trim());
                    }
                }

                // Handle PUT or GET requests
                if (request.startsWith("PUT")) {
                    handlePutRequest(in, out, contentLength);
                } else if (request.startsWith("GET")) {
                    handleGetRequest(out);
                } else {
                    handleInvalidRequest(out);
                }

                out.flush(); // Ensure response is sent
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to handle PUT requests and store weather data
        private void handlePutRequest(BufferedReader in, PrintWriter out, int contentLength) throws IOException {
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            String jsonData = new String(body);

            String[] fields = jsonData.split(",");
            String stationId = fields[0].split(":")[1].replace("\"", "").trim();

            // Store data and timestamp then increment clock
            boolean isNewStation = !weatherData.containsKey(stationId);
            weatherData.put(stationId, jsonData);
            dataStore.put(stationId, System.currentTimeMillis());
            clock.increment();

            // Respond with appropriate status code
            if (isNewStation) {
                out.println("HTTP/1.1 201 Created");
            } else {
                out.println("HTTP/1.1 200 OK");
            }
            out.println();
        }

        // Method to handle GET requests and return weather data
        private void handleGetRequest(PrintWriter out) {
            if (weatherData.isEmpty()) {
                out.println("HTTP/1.1 204 No Content");
            } else {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();

                for (String data : weatherData.values()) {
                    out.println(data);
                }
            }
        }
    }
}

