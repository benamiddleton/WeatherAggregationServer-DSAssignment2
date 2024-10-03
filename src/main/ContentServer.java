package main;

import java.io.*;
import java.net.*;

public class ContentServer {
    private static final String[] SERVERS = {"localhost:4567", "localhost:4568", "localhost:4569"};

    // Main method to start the ContentServer
    public static void main(String[] args) {
        WeatherData weatherData = parseInputFile("weather_input.txt");

        if (weatherData == null) {
            return;
        }

        // Try sending weather data to each server in the list
        for (String server : SERVERS) {
            if (sendWeatherData(server, weatherData)) {
                break;
            }
        }
    }

    // Method to send weather data to a given server
    public static boolean sendWeatherData(String serverAddress, WeatherData weatherData) {
        String[] serverDetails = serverAddress.split(":");
        String host = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);

        try (Socket socket = new Socket(host, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            LamportClock clock = new LamportClock();
            String weatherDataJson = convertWeatherDataToJson(weatherData);
            int contentLength = weatherDataJson.length();

            // Send PUT request with weather data
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Host: " + host + ":" + port);
            out.println("User-Agent: ATOMClient/1.0");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + contentLength);
            out.println("Lamport-Clock: " + clock.getTime());
            out.println();
            out.println(weatherDataJson);
            out.flush();

            // Read server response, check if the data was successfully stored
            String response = in.readLine();
            if (response != null && (response.startsWith("HTTP/1.1 200 OK") || response.startsWith("HTTP/1.1 201 Created"))) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  // Failed to send data
    }

    // Method to convert WeatherData object to JSON format
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

    // Method to parse the input file into a WeatherData object
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

            // Parse file content into individual variables
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue;

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

            if (id == null) {
                throw new IllegalArgumentException("No id found in input file. Invalid data.");
            }

            // Return populated WeatherData object
            return new WeatherData(id, name, state, time_zone, lat, lon,
                    local_date_time, local_date_time_full, air_temp, apparent_t, cloud, dewpt,
                    press, rel_hum, wind_dir, wind_spd_kmh, wind_spd_kt);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}


