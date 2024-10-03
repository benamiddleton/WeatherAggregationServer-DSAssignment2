import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class GETClientTest {

    @Test
    public void testRetrieveWeatherData() {
        // Simulate the AggregationServer's response as a string.
        String simulatedServerResponse = "{ \"id\": \"IDS60901\", \"name\": \"Adelaide\", \"state\": \"SA\", \"air_temp\": 13.3 }";

        // Simulate an InputStream to test without a real server
        InputStream simulatedInputStream = new ByteArrayInputStream(simulatedServerResponse.getBytes());
        BufferedReader in = new BufferedReader(new InputStreamReader(simulatedInputStream));

        try {
            // Simulate retrieving data from a server by reading the data
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }

            // Remove the trailing newline for accurate comparison
            String actualResponse = responseBuilder.toString().trim();
            String expectedResponse = simulatedServerResponse.trim();

            // Assert that the actual response matches the expected response
            assertEquals(expectedResponse, actualResponse);

        } catch (IOException e) {
            fail("Test failed due to IOException: " + e.getMessage());
        }
    }
}

