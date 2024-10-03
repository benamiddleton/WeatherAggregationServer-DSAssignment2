import org.junit.jupiter.api.Test;
import main.ContentServer;
import main.WeatherData;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class ContentServerTest {

    @Test
    public void testParseInputFile() {
        // Simulate the input file content in a string format.
        String input = "id:IDS60901\n" +
                "name:Adelaide\n" +
                "state:SA\n" +
                "time_zone:CST\n" +
                "lat:-34.9\n" +
                "lon:138.6\n" +
                "local_date_time:15/04:00pm\n" +
                "local_date_time_full:20230715160000\n" +
                "air_temp:13.3\n" +
                "apparent_t:9.5\n" +
                "cloud:Partly cloudy\n" +
                "dewpt:5.7\n" +
                "press:1023.9\n" +
                "rel_hum:60\n" +
                "wind_dir:S\n" +
                "wind_spd_kmh:15\n" +
                "wind_spd_kt:8\n";

        // Create a temporary file and write the input content into it.
        File tempFile = null;
        try {
            tempFile = File.createTempFile("weather_input", ".txt");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(input);
            writer.close();

            // Call the method to parse the file
            WeatherData weatherData = ContentServer.parseInputFile(tempFile.getAbsolutePath());

            // Assert values from the input file
            assertNotNull(weatherData);
            assertEquals("IDS60901", weatherData.getId());
            assertEquals("Adelaide", weatherData.getName());
            assertEquals("SA", weatherData.getState());
            assertEquals("CST", weatherData.getTime_zone());
            assertEquals(-34.9, weatherData.getLat());
            assertEquals(138.6, weatherData.getLon());
            assertEquals("15/04:00pm", weatherData.getLocal_date_time());
            assertEquals("20230715160000", weatherData.getLocal_date_time_full());
            assertEquals(13.3, weatherData.getAir_temp());
            assertEquals(9.5, weatherData.getApparent_t());
            assertEquals("Partly cloudy", weatherData.getCloud());
            assertEquals(5.7, weatherData.getDewpt());
            assertEquals(1023.9, weatherData.getPress());
            assertEquals(60, weatherData.getRel_hum());
            assertEquals("S", weatherData.getWind_dir());
            assertEquals(15, weatherData.getWind_spd_kmh());
            assertEquals(8, weatherData.getWind_spd_kt());

            System.out.println("testParseInputFile: Passed");

        } catch (IOException e) {
            fail("Test failed due to IOException: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}

