package main;

import java.io.*;
import java.net.*;

public class ContentServer {
    private static final String[] SERVERS = {"localhost:4567", "localhost:4568", "localhost:4569"}; // Replicated aggregation servers

    public static void main(String[] args) {
        System.out.println("ContentServer: Parsing weather input file.");
        WeatherData weatherData = parseInputFile("weather_input.txt"); // Parse input file

        if (weatherData == null) {
            System.err.println("ContentServer: Failed to parse weather input file.");
            return;
        }

        // Try sending weather data to each server in the list, retry on failure
        for (String server : SERVERS) {
            System.out.println("ContentServer: Attempting to send weather data to " + server);
            if (sendWeatherData(server, weatherData)) {
                System.out.println("ContentServer: Successfully sent weather data to " + server);
                break;
            } else {
                System.out.println("ContentServer: Failed to send weather data to " + server + ". Retrying with next server...");
            }
        }
    }

    // Method to send weather data to an aggregation server
    public static boolean sendWeatherData(String serverAddress, WeatherData weatherData) {
        String[] serverDetails = serverAddress.split(":");
        String host = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);

        try (Socket socket = new Socket(host, port)) {
            System.out.println("ContentServer: Connected to aggregation server " + host + " on port " + port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Use Lamport clock to maintain order of events
            LamportClock clock = new LamportClock();

            // Convert WeatherData to JSON-like string and calculate content length
            String weatherDataJson = convertWeatherDataToJson(weatherData);
            int contentLength = weatherDataJson.length();

            System.out.println("ContentServer: Sending PUT request with Lamport clock value: " + clock.getTime());
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Host: " + host + ":" + port);
            out.println("User-Agent: ATOMClient/1.0");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + contentLength);
            out.println("Lamport-Clock: " + clock.getTime());
            out.println(); // Blank line to indicate end of headers
            out.println(weatherDataJson); // Body
            out.flush();  // Ensure all data is sent

            // Read server response
            System.out.println("ContentServer: Waiting for server response...");
            String response = in.readLine();
            System.out.println("ContentServer: Server response: " + response);

            if (response != null && (response.startsWith("HTTP/1.1 200 OK") || response.startsWith("HTTP/1.1 201 Created"))) {
                return true;
            }
        } catch (IOException e) {
            System.out.println("ContentServer: Error sending to " + serverAddress + ": " + e.getMessage());
        }
        return false;
    }

    // Converts WeatherData object to a JSON-like string format
    public static String convertWeatherDataToJson(WeatherData weatherData) {
        return "{\n" +
                "    \"id\": \"" + weatherData.getId() + "\",\n" +
                "    \"name\": \"" + weatherData.getName() + "\",\n" +
                "    \"state\": \"" + weatherData.getState() + "\",\n" +
                "    \"time_zone\": \"" + weatherData.getTime_zone() + "\",\n" +
                "    \"lat\": " + weatherData.getLat() + ",\n" +
                "    \"lon\": " + weatherData.getLon() + ",\n" +
                "    \"local_date_time\": \"" + weatherData.getLocal_date_time() + "\",\n" +
                "    \"local_date_time_full\": \"" + weatherData.getLocal_date_time_full() + "\",\n" +
                "    \"air_temp\": " + weatherData.getAir_temp() + ",\n" +
                "    \"apparent_t\": " + weatherData.getApparent_t() + ",\n" +
                "    \"cloud\": \"" + weatherData.getCloud() + "\",\n" +
                "    \"dewpt\": " + weatherData.getDewpt() + ",\n" +
                "    \"press\": " + weatherData.getPress() + ",\n" +
                "    \"rel_hum\": " + weatherData.getRel_hum() + ",\n" +
                "    \"wind_dir\": \"" + weatherData.getWind_dir() + "\",\n" +
                "    \"wind_spd_kmh\": " + weatherData.getWind_spd_kmh() + ",\n" +
                "    \"wind_spd_kt\": " + weatherData.getWind_spd_kt() + "\n" +
                "}";
    }

    // Parses the input file into a WeatherData object
    public static WeatherData parseInputFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String id = null, name = null, state = null, time_zone = null;
            double lat = 0, lon = 0;
            String local_date_time = null, local_date_time_full = null;
            double air_temp = 0, apparent_t = 0, dewpt = 0, press = 0;
            int rel_hum = 0;
            String cloud = null, wind_dir = null;
            int wind_spd_kmh = 0, wind_spd_kt = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue; // Skip malformed lines

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "id":
                        id = value;
                        break;
                    case "name":
                        name = value;
                        break;
                    case "state":
                        state = value;
                        break;
                    case "time_zone":
                        time_zone = value;
                        break;
                    case "lat":
                        lat = Double.parseDouble(value);
                        break;
                    case "lon":
                        lon = Double.parseDouble(value);
                        break;
                    case "local_date_time":
                        local_date_time = value;
                        break;
                    case "local_date_time_full":
                        local_date_time_full = value;
                        break;
                    case "air_temp":
                        air_temp = Double.parseDouble(value);
                        break;
                    case "apparent_t":
                        apparent_t = Double.parseDouble(value);
                        break;
                    case "cloud":
                        cloud = value;
                        break;
                    case "dewpt":
                        dewpt = Double.parseDouble(value);
                        break;
                    case "press":
                        press = Double.parseDouble(value);
                        break;
                    case "rel_hum":
                        rel_hum = Integer.parseInt(value);
                        break;
                    case "wind_dir":
                        wind_dir = value;
                        break;
                    case "wind_spd_kmh":
                        wind_spd_kmh = Integer.parseInt(value);
                        break;
                    case "wind_spd_kt":
                        wind_spd_kt = Integer.parseInt(value);
                        break;
                }
            }

            // Ensure id is present, as per requirements
            if (id == null) {
                throw new IllegalArgumentException("No id found in input file. Invalid data.");
            }

            // Create and return the populated WeatherData object
            return new WeatherData(id, name, state, time_zone, lat, lon,
                    local_date_time, local_date_time_full, air_temp, apparent_t, cloud, dewpt,
                    press, rel_hum, wind_dir, wind_spd_kmh, wind_spd_kt);

        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing a numerical value: " + e.getMessage());
            return null;
        }
    }
}



