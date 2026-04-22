# Smart Campus Sensor & Room Management API

A RESTful JAX-RS API for managing university campus rooms and IoT sensors, built with Jersey 2.41 deployed on Apache Tomcat 9.

---

## Table of Contents
1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [How to Build and Run](#how-to-build-and-run)
5. [Sample curl Commands](#sample-curl-commands)
6. [API Endpoints Reference](#api-endpoints-reference)
7. [Report: Question Answers](#report-question-answers)

---

## API Design Overview

The Smart Campus API follows a **resource-oriented RESTful architecture** using JAX-RS (Jersey 2.41). It exposes three core entities:

- **Room** – physical rooms in the campus (e.g., lecture halls, labs)
- **Sensor** – IoT sensors deployed inside rooms (temperature, CO2, occupancy)
- **SensorReading** – time-stamped historical measurements recorded by each sensor

### Resource Hierarchy

```
/api/v1/
├── /                          → Discovery endpoint (API metadata + links)
├── /rooms                     → Room collection
│   ├── GET    /rooms           → List all rooms
│   ├── POST   /rooms           → Create a room
│   ├── GET    /rooms/{id}      → Get room by ID
│   └── DELETE /rooms/{id}      → Delete room (blocked if sensors exist)
└── /sensors                   → Sensor collection
    ├── GET    /sensors         → List all sensors (optional ?type= filter)
    ├── POST   /sensors         → Register a sensor (validates roomId)
    ├── PUT    /sensors/{id}    → Update sensor value/status
    └── /sensors/{id}/readings → Sub-resource (reading history)
        ├── GET  /readings      → Fetch reading history
        └── POST /readings      → Append a new reading
```

### Design Principles Applied
- **Stateless:** Each request carries all necessary context; no server-side sessions
- **Uniform Interface:** Consistent use of HTTP verbs (GET, POST, PUT, DELETE)
- **HATEOAS:** Discovery endpoint provides navigable links to all resource collections
- **Layered Error Handling:** Custom `ExceptionMapper`s prevent raw stack traces from leaking
- **In-Memory Storage:** `HashMap` and `ArrayList` via a shared `DataStore` class (no database)

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 11 |
| Framework | JAX-RS (Jersey 2.41) |
| Server | Apache Tomcat 9 |
| Build Tool | Maven |
| Packaging | WAR |
| JSON Binding | Jersey Media JSON Binding |

---

## Project Structure

```
SmartCampusAPI/
├── src/main/java/com/example/smartcampusapi/
│   ├── JakartaRestConfiguration.java     # @ApplicationPath("/api/v1") config
│   ├── controller/
│   │   ├── DiscoverResource.java          # GET /api/v1/
│   │   ├── SensorRoomResource.java        # /api/v1/rooms
│   │   ├── SensorResource.java            # /api/v1/sensors
│   │   └── SensorReadingResource.java     # Sub-resource: /sensors/{id}/readings
│   ├── model/
│   │   ├── Room.java
│   │   ├── Sensor.java
│   │   └── SensorReading.java
│   ├── repository/
│   │   └── DataStore.java                 # In-memory HashMap/ArrayList storage
│   ├── exception/
│   │   ├── RoomNotEmptyException.java
│   │   ├── RoomNotEmptyExceptionMapper.java
│   │   ├── LinkedResourceNotFoundException.java
│   │   ├── LinkedResourceNotFoundExceptionMapper.java
│   │   ├── SensorUnavailableException.java
│   │   ├── SensorUnavailableExceptionMapper.java
│   │   └── GlobalExceptionMapper.java
│   └── filter/
│       └── ApiLoggingFilter.java
├── src/main/webapp/WEB-INF/
│   ├── web.xml
│   └── beans.xml
└── pom.xml
```

---

## How to Build and Run

### Prerequisites
- Java 11 or higher installed
- Apache Tomcat 9 installed
- NetBeans IDE (recommended) or Maven CLI
- Git

### Step 1: Clone the Repository
```bash
git clone https://github.com/<your-username>/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2: Build the Project

**Using NetBeans:**
1. Open NetBeans → File → Open Project → select the `SmartCampusAPI` folder
2. Right-click the project → **Clean and Build**
3. The WAR file will be generated at `target/SmartCampusAPI.war`

**Using Maven CLI:**
```bash
mvn clean package
```

### Step 3: Deploy to Tomcat 9

**Using NetBeans (Recommended):**
1. Right-click the project → **Properties** → **Run**
2. Ensure **Server** is set to `Apache Tomcat 9`
3. Click the **Run** (▶) button — NetBeans will deploy and start the server automatically

**Manually:**
1. Copy `target/SmartCampusAPI.war` to your Tomcat `webapps/` folder
2. Start Tomcat: `<tomcat-dir>/bin/startup.bat` (Windows) or `startup.sh` (Linux/Mac)

### Step 4: Verify the Server is Running

Open your browser and navigate to:
```
http://localhost:8081/SmartCampusAPI/api/v1/
```

You should see a JSON response like:
```json
{
  "version": "1.0.0",
  "description": "Smart Campus Sensor & Room Management API",
  "admin_contact": "amna@example.com",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### Step 5: Run Postman Tests
Import `SmartCampusAPI_Postman_Collection.json` into Postman and run the collection in order (tests 1–18 are sequential).

---

## Sample curl Commands

> Replace port `8081` with your actual Tomcat port if different.

### 1. Get API Discovery Info
```bash
curl -X GET http://localhost:8081/SmartCampusAPI/api/v1/
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 40}'
```

### 3. Register a Sensor linked to a Room
```bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "roomId": "LIB-301", "type": "TEMPERATURE", "status": "ACTIVE", "currentValue": 22.5}'
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8081/SmartCampusAPI/api/v1/sensors?type=TEMPERATURE"
```

### 5. Post a Sensor Reading
```bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.8}'
```

### 6. Get Reading History for a Sensor
```bash
curl -X GET http://localhost:8081/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```

### 7. Delete a Room (triggers 409 if sensors still assigned)
```bash
curl -X DELETE http://localhost:8081/SmartCampusAPI/api/v1/rooms/LIB-301
```

---

## API Endpoints Reference

| Method | Endpoint | Description | Success Code |
|--------|----------|-------------|-------------|
| GET | `/api/v1/` | Discovery info & links | 200 |
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a room | 201 |
| GET | `/api/v1/rooms/{id}` | Get room by ID | 200 |
| DELETE | `/api/v1/rooms/{id}` | Delete room | 204 |
| GET | `/api/v1/sensors` | List all sensors (optional `?type=`) | 200 |
| POST | `/api/v1/sensors` | Register a sensor | 201 |
| PUT | `/api/v1/sensors/{id}` | Update sensor value/status | 200 |
| GET | `/api/v1/sensors/{id}/readings` | Get reading history | 200 |
| POST | `/api/v1/sensors/{id}/readings` | Add a new reading | 201 |

---

## Report: Question Answers

### Part 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of every resource class for each incoming HTTP request** (per-request lifecycle). This is the default behaviour mandated by the JAX-RS specification and is used by Jersey. Each request gets its own isolated object, which eliminates thread-safety concerns within the resource class itself.

However, this has a significant implication for **in-memory data management**. Because each request creates a fresh resource object, instance variables on a resource class cannot be used to store shared state — they would be lost after each request. To solve this, all shared data (rooms, sensors, readings) must be stored in **static, shared data structures** such as the `HashMap` and `ArrayList` held in the `DataStore` class. Since static structures are shared across all instances and threads, **thread-safety must be considered**. In a production system, `ConcurrentHashMap` and `Collections.synchronizedList()` would be used, or access would be synchronised with `synchronized` blocks to prevent race conditions and data corruption under concurrent load.

---

### Part 1.2 – HATEOAS and Hypermedia

HATEOAS (Hypermedia as the Engine of Application State) is the principle that API responses should include navigable links to related resources, allowing clients to discover and interact with the API dynamically without relying solely on external documentation.

The Discovery endpoint (`GET /api/v1/`) demonstrates this by returning links to `/api/v1/rooms` and `/api/v1/sensors` directly in the response. This is powerful because:

1. **Reduced coupling:** Client applications do not need to hard-code URLs. They follow links provided by the server, so if the API's URL structure changes, clients that follow links are automatically updated.
2. **Self-documenting:** The API communicates its own capabilities. A developer exploring the API for the first time can navigate the entire set of resources from the entry point alone.
3. **Discoverability:** New resource collections can be added without breaking existing clients, as clients only act on links they receive.

Compared to static documentation (like a PDF spec), HATEOAS keeps the API and its navigation in sync at runtime, reducing the risk of documentation becoming outdated.

---

### Part 2.1 – Returning IDs vs Full Room Objects

When returning a list of rooms via `GET /api/v1/rooms`, there are two design choices:

**Returning only IDs** (e.g., `["R-101", "R-102"]`):
- Minimal payload size, fast response time
- Client must make N additional requests (one per room) to fetch details — known as the **N+1 problem**
- Higher total latency due to multiple round-trips

**Returning full room objects** (as implemented):
- Larger payload, but the client receives all data in a **single request**
- Eliminates N+1 requests, far more efficient for clients rendering a list view
- Preferred for most real-world use cases where the client needs room details immediately

For a campus management dashboard displaying a room list with names and capacities, returning full objects is far superior. Returning only IDs would be appropriate in scenarios where clients only need to reference IDs (e.g., for internal linking), or where the collection is extremely large and pagination with minimal data is required.

---

### Part 2.2 – Idempotency of DELETE

The DELETE operation **is idempotent by HTTP specification**, meaning the same request made multiple times should have the same effect as making it once. In this implementation:

- **First DELETE on a valid room (with no sensors):** The room is removed from `DataStore.rooms`, returns `204 No Content`.
- **Second DELETE on the same room ID:** The room no longer exists, so the code finds `null` and returns `404 Not Found`.

Strictly speaking, returning a different status code (204 vs 404) on repeated calls means the **server state is idempotent** (the room remains deleted), but the **response is not identical**. This is acceptable and standard — the HTTP specification states that idempotency refers to the server-side state effect, not the response code. The room being absent is the intended final state, and sending DELETE again does not create a different state. Most REST practitioners consider this correct behaviour.

---

### Part 3.1 – @Consumes Mismatch Behaviour

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST endpoint only accepts requests where the `Content-Type` header is `application/json`.

If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS will **automatically reject the request** before it even reaches the resource method. Jersey responds with **`415 Unsupported Media Type`**. No custom code is needed for this — it is handled entirely by the JAX-RS runtime during request matching. This protects the API from malformed input and makes the contract explicit: only JSON is accepted. It also means the method body never needs to validate the content format itself, simplifying business logic.

---

### Part 3.2 – @QueryParam vs Path-Based Filtering

The two design approaches for sensor type filtering are:

| Approach | Example URL |
|----------|-------------|
| Query Parameter (implemented) | `GET /api/v1/sensors?type=CO2` |
| Path-based | `GET /api/v1/sensors/type/CO2` |

**Why `@QueryParam` is superior for filtering:**

1. **Semantic clarity:** Path segments should identify a specific resource. A type filter is not an identifier — it is a search criterion. Using it in the path implies `CO2` is a resource, which it is not.
2. **Optionality:** Query parameters are naturally optional. `GET /api/v1/sensors` returns all sensors; `GET /api/v1/sensors?type=CO2` filters them. With path-based design, a separate route is needed for each case.
3. **Composability:** Multiple filters can be combined easily (`?type=CO2&status=ACTIVE`). Path-based filtering becomes unmaintainable with multiple parameters.
4. **REST convention:** The REST convention is that paths identify resources, and query strings refine or filter the representation returned.

---

### Part 4.1 – Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern delegates handling of a nested path to a separate class. In `SensorResource`, the method annotated with `@Path("/{sensorId}/readings")` returns a new `SensorReadingResource` instance instead of defining GET/POST directly:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

**Benefits:**

1. **Separation of concerns:** `SensorResource` manages the sensor collection. `SensorReadingResource` exclusively handles readings. Each class has a single, well-defined responsibility.
2. **Maintainability:** In a large API, placing all nested paths (e.g., `/sensors/{id}/readings`, `/sensors/{id}/alerts`, `/sensors/{id}/config`) into one massive class creates hundreds of methods that are hard to test and maintain.
3. **Reusability:** A sub-resource class can theoretically be reused or tested independently.
4. **Contextual passing:** The `sensorId` is passed to the sub-resource constructor, giving it the context it needs to filter data — a clean dependency injection pattern.
5. **Scalability:** New sub-resources (e.g., `/sensors/{id}/alerts`) can be added by creating a new class and adding one locator method, without touching existing code.

---

### Part 5.2 – HTTP 422 vs 404 for Missing References

When a client POSTs a new sensor with a `roomId` that does not exist:

- **404 Not Found** implies that the **URL/endpoint** being requested was not found on the server — but the URL `/api/v1/sensors` is perfectly valid and exists.
- **422 Unprocessable Entity** means the server **understood the request** (valid JSON, correct Content-Type, correct URL), but the **semantic content** of the payload is invalid — specifically, `roomId` references an entity that does not exist.

422 is more semantically accurate because the problem is not with the endpoint or the syntax of the request, but with the **referential integrity** of the data inside the payload. It tells the client: "Your request was well-formed, but I cannot process it because one of the values you provided refers to something that doesn't exist." This gives client developers a much clearer signal about what went wrong and how to fix it.

---

### Part 5.4 – Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers poses serious cybersecurity risks:

1. **Internal path disclosure:** Stack traces reveal full class names, package structures, and file paths (e.g., `com.example.smartcampusapi.repository.DataStore`), giving attackers a detailed map of the application's internal architecture.
2. **Library and version fingerprinting:** Stack traces often include third-party library names and version numbers (e.g., Jersey, Tomcat). Attackers can look up known CVEs (Common Vulnerabilities and Exposures) for those exact versions and craft targeted exploits.
3. **Business logic exposure:** Method names in a stack trace (e.g., `validateRoomExists`, `getSensorById`) reveal internal processing logic, helping attackers understand what validations exist and how to bypass them.
4. **Exception message leakage:** Exception messages often contain sensitive data like SQL queries, internal IDs, or configuration values that should never be exposed externally.

The `GlobalExceptionMapper<Throwable>` implemented in this project prevents all of the above by catching every unhandled exception and returning only a generic `500 Internal Server Error` with a safe, minimal JSON message — no stack trace, no internal detail.

---

### Part 5.5 – JAX-RS Filters vs Manual Logging

Using a JAX-RS filter (`ContainerRequestFilter` / `ContainerResponseFilter`) for logging is superior to inserting `Logger.info()` inside every resource method for several reasons:

1. **Cross-cutting concern separation:** Logging is not business logic. A filter keeps it entirely separate, so resource methods remain clean and focused solely on their responsibility.
2. **DRY principle (Don't Repeat Yourself):** With manual logging, every method (hundreds in a large API) needs the same boilerplate log statements. A single filter class handles all of them automatically.
3. **Consistency:** A filter guarantees every single request and response is logged in the same format. Manual logging risks inconsistency — developers may log different fields, at different levels, or forget to log some methods entirely.
4. **Maintainability:** Changing the log format requires editing one class, not every resource method across the codebase.
5. **Completeness:** Filters intercept requests before they reach resource methods, meaning even failed requests (e.g., 404s, 415s) are logged — something manual logging inside methods would miss entirely.
