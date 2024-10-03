Weather Aggregation Server
===========================

Ben Middleton A1833500

This project simulates a distributed weather data aggregation system. It consists of three main components:
- **Content Server**: Sends weather data to the aggregation server.
- **Aggregation Server**: Receives and aggregates weather data from content servers and serves it to clients.
- **GET Client**: Retrieves the aggregated weather data from the aggregation server.


Contents & Explanation:
------------
1. **AggregationServer**:
   - Listens on a specified port (default: 4567) for incoming connections from the content server or GET client.
   - Handles both `PUT` requests to receive weather data and `GET` requests to retrieve aggregated weather data.
   - Uses a Lamport Clock to ensure consistent ordering of events across the distributed system.

2. **ContentServer**:
   - Reads weather data from the `weather_input.txt` file.
   - Sends the weather data to the aggregation server using an HTTP-like `PUT` request.
   - Attempts to send data to multiple servers, retrying if a connection fails.

3. **GETClient**:
   - Connects to the aggregation server to retrieve the aggregated weather data using an HTTP-like `GET` request.
   - Displays the received weather data to the console.

4. **LamportClock**:
   - Implements a logical clock used to ensure proper event ordering between different processes (Content Server, Aggregation Server, GET Client).

5. **WeatherData**:
   - A Java class representing the weather data with fields such as ID, name, state, temperature, etc.

6. **Unit Tests**:
   - Unit tests for the `AggregationServer`, `ContentServer`, and `GETClient` classes are located in the `Testing/` directory.
   - These tests verify that the components behave correctly by simulating requests and responses.

Compilation and Usage:
----------------------
### Compiling the Project:
**Using Command Line (Javac):**
   - Navigate to the `src` directory where the Java files are located.
   - Run the following command to compile all Java files:
     ```
     javac main/*.java
     ```
   - If you have external libraries, make sure to include them in the classpath:
     ```
     javac -cp ".;path/to/external/libs/*" main/*.java
     ```

### Running the Application:
1. **Running AggregationServer:**
   - Ensure that the Aggregation Server is running before starting the Content Server or GET Client.
   - From the command line, navigate to the `src` directory and run:
     ```
     java main.AggregationServer
     ```

2. **Running ContentServer:**
   - Ensure the Aggregation Server is running.
   - Start the Content Server by navigating to the `src` directory and running:
     ```
     java main.ContentServer
     ```
   - The Content Server will read the `weather_input.txt` file and attempt to send data to the Aggregation Server.

3. **Running GETClient:**
   - Start the GET Client to fetch aggregated weather data by navigating to the `src` directory and running:
     ```
     java main.GETClient
     ```

### Running Tests:
**Using Command Line:**
   - Navigate to the `Testing/` directory and compile the test classes using:
     ```
     javac -cp ".;path/to/junit.jar" Testing/*.java
     ```
   - To run the tests, use the following command with the JUnit library:
     ```
     java -cp ".;path/to/junit.jar" org.junit.runner.JUnitCore Testing.AggregationServerTest
     ```

Requirements:
-------------
- **Java 11+**: The code is compatible with Java 11 and newer versions.
- **JUnit** These libraries are required for running the unit tests.
- **Gson** Required for JSON parsing and handling within the project
- **Mockito & ByteBuddy**: Could be used in future testing


