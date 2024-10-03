package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AggregationServer {
    private static final int PORT = 4567;
    private static final int EXPIRY_TIME = 30000;

    // Stores the weather data, time, and Lamport clock
    private static ConcurrentHashMap<String, Long> dataStore = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> weatherData = new ConcurrentHashMap<>();
    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("AggregationServer: Started on port " + PORT);

        // Timer to expunge expired data every 30 seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                expungeExpiredData();
            }
        }, 0, EXPIRY_TIME);

        // Server loop to accept and handle client requests
        while (true) {
            System.out.println("AggregationServer: Waiting for client connection...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("AggregationServer: Client connected from " + clientSocket.getInetAddress());
            new Thread(new RequestHandler(clientSocket)).start();
        }
    }

    // Expunge data older than 30 seconds
    public static synchronized void expungeExpiredData() {
        long currentTime = System.currentTimeMillis();
        System.out.println("AggregationServer: Checking for expired data...");
        for (Map.Entry<String, Long> entry : dataStore.entrySet()) {
            if (currentTime - entry.getValue() > EXPIRY_TIME) {
                System.out.println("AggregationServer: Expiring data for station ID: " + entry.getKey());
                weatherData.remove(entry.getKey());
                dataStore.remove(entry.getKey());
            }
        }
    }

    // Handle invalid requests and return 400 Bad Request
    public static void handleInvalidRequest(PrintWriter out) {
        System.out.println("AggregationServer: Invalid request received.");
        out.println("HTTP/1.1 400 Bad Request");
        out.println(); // End of headers
        out.flush();
    }

    // Class to handle PUT and GET requests from clients
    static class RequestHandler implements Runnable {
        private Socket clientSocket;

        public RequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                System.out.println("AggregationServer: Reading client request...");
                String request = in.readLine(); // Read the request line
                String header;
                int contentLength = 0;
                int clientClockTime = 0;

                // Reading headers
                System.out.println("AggregationServer: Reading headers...");
                while (!(header = in.readLine()).isEmpty()) {
                    System.out.println("AggregationServer: Header - " + header);

                    if (header.startsWith("Lamport-Clock:")) {
                        clientClockTime = Integer.parseInt(header.split(":")[1].trim());
                        clock.update(clientClockTime);
                        System.out.println("AggregationServer: Updated Lamport clock to: " + clock.getTime());
                    } else if (header.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(header.split(":")[1].trim());
                        System.out.println("AggregationServer: Content-Length is " + contentLength);
                    }
                }

                // Determine if it's a GET or PUT request
                if (request.startsWith("PUT")) {
                    System.out.println("AggregationServer: Handling PUT request...");
                    handlePutRequest(in, out, contentLength);
                } else if (request.startsWith("GET")) {
                    System.out.println("AggregationServer: Handling GET request...");
                    handleGetRequest(out);
                } else {
                    handleInvalidRequest(out); // Handle invalid requests
                }

                out.flush();  // Ensure the response is sent
                System.out.println("AggregationServer: Request processing completed.");
            } catch (IOException e) {
                System.out.println("AggregationServer: Internal Server Error: " + e.getMessage());
            }
        }

        // Handle PUT requests from clients (store data)
        private void handlePutRequest(BufferedReader in, PrintWriter out, int contentLength) throws IOException {
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            String jsonData = new String(body);
            System.out.println("AggregationServer: Received body data:\n" + jsonData);

            // Process the body data
            String[] fields = jsonData.split(",");
            String stationId = fields[0].split(":")[1].replace("\"", "").trim();

            boolean isNewStation = !weatherData.containsKey(stationId);
            weatherData.put(stationId, jsonData);
            dataStore.put(stationId, System.currentTimeMillis());
            clock.increment();

            if (isNewStation) {
                out.println("HTTP/1.1 201 Created");
                System.out.println("AggregationServer: New data added for station ID: " + stationId);
            } else {
                out.println("HTTP/1.1 200 OK");
                System.out.println("AggregationServer: Updated data for station ID: " + stationId);
            }
            out.println(); // End of headers
        }

        // Handle GET requests from clients (return data)
        private void handleGetRequest(PrintWriter out) {
            System.out.println("AggregationServer: Serving GET request...");
            if (weatherData.isEmpty()) {
                out.println("HTTP/1.1 204 No Content");
                System.out.println("AggregationServer: No data to return.");
            } else {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println(); // End of headers

                for (String data : weatherData.values()) {
                    out.println(data);
                }
                System.out.println("AggregationServer: Finished serving GET request.");
            }
        }
    }
}
