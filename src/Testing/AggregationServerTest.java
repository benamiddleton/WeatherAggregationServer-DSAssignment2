import main.AggregationServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.BindException;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationServerTest {

    private Thread serverThread;

    @BeforeEach
    public void setUp() throws IOException {
        // Start AggregationServer in a separate thread
        serverThread = new Thread(() -> {
            try {
                AggregationServer.main(new String[]{});
            } catch (BindException e) {
                // Handle BindException and print a friendly message
                System.err.println("Server is already running on port 4567. This is not an issue for testing.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Wait a bit for the server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPutRequestHandling() throws IOException {
        // Simulate a client sending a PUT request to the AggregationServer
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send the PUT request
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Length: 51");
            out.println("Lamport-Clock: 0");
            out.println();
            out.println("{\"id\": \"IDS60901\", \"name\": \"Adelaide\", \"state\": \"SA\"}");

            // Read the response from the server
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine).append("\n");
            }

            // Check the response contains the expected status code
            assertTrue(response.toString().contains("HTTP/1.1 201 Created"));
            System.out.println("testPutRequestHandling: Passed");
        }
    }

    @Test
    public void testGetRequestHandling() throws IOException {
        // Simulate a client sending a GET request to the AggregationServer
        try (Socket socket = new Socket("localhost", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send the GET request
            out.println("GET /weather.json HTTP/1.1");
            out.println("Lamport-Clock: 1");
            out.println();

            // Read the response from the server
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine).append("\n");
            }

            // Check if the server responded with the correct weather data
            assertTrue(response.toString().contains("Adelaide"));
            System.out.println("testGetRequestHandling: Passed");
        }
    }
}

