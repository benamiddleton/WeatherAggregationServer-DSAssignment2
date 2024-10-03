package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AggregationServer {
    private static final int PORT = 4567;
    private static final int EXPIRY_TIME = 30000;

    private static ConcurrentHashMap<String, Long> dataStore = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> weatherData = new ConcurrentHashMap<>();
    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        new Timer().schedule(new TimerTask() {
            public void run() {
                expungeExpiredData();
            }
        }, 0, EXPIRY_TIME);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new RequestHandler(clientSocket)).start();
        }
    }

    public static synchronized void expungeExpiredData() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : dataStore.entrySet()) {
            if (currentTime - entry.getValue() > EXPIRY_TIME) {
                weatherData.remove(entry.getKey());
                dataStore.remove(entry.getKey());
            }
        }
    }

    public static void handleInvalidRequest(PrintWriter out) {
        out.println("HTTP/1.1 400 Bad Request");
        out.println();
        out.flush();
    }

    static class RequestHandler implements Runnable {
        private Socket clientSocket;

        public RequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();
                String header;
                int contentLength = 0;
                int clientClockTime = 0;

                while (!(header = in.readLine()).isEmpty()) {
                    if (header.startsWith("Lamport-Clock:")) {
                        clientClockTime = Integer.parseInt(header.split(":")[1].trim());
                        clock.update(clientClockTime);
                    } else if (header.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(header.split(":")[1].trim());
                    }
                }

                if (request.startsWith("PUT")) {
                    handlePutRequest(in, out, contentLength);
                } else if (request.startsWith("GET")) {
                    handleGetRequest(out);
                } else {
                    handleInvalidRequest(out);
                }

                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handlePutRequest(BufferedReader in, PrintWriter out, int contentLength) throws IOException {
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            String jsonData = new String(body);

            String[] fields = jsonData.split(",");
            String stationId = fields[0].split(":")[1].replace("\"", "").trim();

            boolean isNewStation = !weatherData.containsKey(stationId);
            weatherData.put(stationId, jsonData);
            dataStore.put(stationId, System.currentTimeMillis());
            clock.increment();

            if (isNewStation) {
                out.println("HTTP/1.1 201 Created");
            } else {
                out.println("HTTP/1.1 200 OK");
            }
            out.println();
        }

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
