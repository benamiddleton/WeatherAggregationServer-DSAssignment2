# Weather Aggregation System

This system aggregates weather data from multiple content servers and serves it to clients upon request.
---

## Features

1. Content Servers: Send weather data to the aggregation server.
2. Aggregation Server: Collects data from content servers and responds to client requests.
3. Clients: Fetch weather data using GET requests.
4. PUT Requests: Add weather data from content servers.
5. Data Expiration: Weather data expires after 30 seconds.
6. Fault Tolerance: Content servers are replicated to ensure data availability.
7. Lamport Clocks: Synchronize timestamps across processes to maintain event order.

---

## Setup and Run

1. Compilation
    - Compile all Java files:
      ```bash
      javac *.java
      ```

2. Run Aggregation Server
    - Start the Aggregation Server to collect data:
      ```bash
      java AggregationServer
      ```

3. Run Content Server
    - Start a Content Server to send weather data:
      ```bash
      java ContentServer
      ```

4. Run GET Client
    - Fetch data using the GET request:
      ```bash
      java GETClient
      ```

---

## How it Works

1. PUT Requests: Content servers send weather data to the aggregation server using `PUT` requests.
2. GET Requests: Clients retrieve aggregated data with `GET` requests.
3. Data Expiration: Data older than 30 seconds is automatically removed.
4. Fault Tolerance: Replicated content servers ensure data availability even if one server fails.
5. Lamport Clocks: Ensure the proper ordering of events in a distributed system.

---

## Testing Instructions

see `testing.txt`.

---

## Error Handling

- System includes retries for failed communications, ensuring reliability.
- error codes are implemented for troubleshooting.

