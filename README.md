# Smart Campus Sensor & Room Management API

A professional Jakarta EE RESTful web service built with **JAX-RS (Jersey 2.x)** designed to manage thousands of campus rooms and IoT sensors. This system implements high-performance in-memory data structures and a resilient error-handling strategy.

**Student:** Fathima Amnaah Aakiff — w2120196  
**Course:** Client-Server Architectures (5COSC022W)  
**University:** University of Westminster (IIT)

> **Video Demonstration:** The video showcases all 18 test cases running on a live Tomcat 9 server, with my student ID and clear verbal explanations.

---

##  Technology Stack

| Technology | Role |
|---|---|
| **Java 17 (LTS)** | Core language |
| **JAX-RS (Jersey 2.x)** | Framework for RESTful Web Services |
| **Apache Tomcat 9.0.x** | Servlet Container |
| **Maven** | Project Build & Dependency Management |
| **Postman** | API Testing and Documentation |

---

##  How to Build and Run

### Prerequisites
- JDK 17 installed and configured in your environment.
- Apache Tomcat 9.0.x configured within NetBeans or your preferred IDE.

### Step-by-Step Launch

1. **Clone the Project:**
   ```bash
   git clone https://github.com/YourUsername/SmartCampusAPI.git
   ```

2. **Build:**  
   In NetBeans, right-click the project and select **Clean and Build**. This generates the `SmartCampusAPI.war` file in the `target/` directory.

3. **Deploy:**  
   Right-click the project and select **Run**. NetBeans will deploy the application to Tomcat 9.

4. **Access:**  
   The API is live at: `http://localhost:8081/SmartCampusAPI/api/v1/`

5. **Testing:**  
   Import the `SmartCampusAPI_Postman_Collection.json` file into Postman and set the `baseUrl` variable to the address above.

---

##  Sample `curl` Commands

Industry-standard CLI interactions for testing core resources:

### 1. Root Discovery (HATEOAS)
```bash
curl -X GET http://localhost:8081/SmartCampusAPI/api/v1/
```

### 2. Create a Campus Room
```bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Study", "capacity": 50}'
```

### 3. Register a Sensor (Linked to Room)
```bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "roomId": "LIB-301", "type": "Temperature", "status": "ACTIVE", "currentValue": 22.5}'
```

### 4. Filter Sensors by Type (QueryParam)
```bash
curl -X GET "http://localhost:8081/SmartCampusAPI/api/v1/sensors?type=Temperature"
```

### 5. Post a Historical Sensor Reading (Sub-Resource)
```bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 24.2}'
```

---

##  Project Structure

```
SmartCampusAPI/
├── src/main/java/com/example/smartcampusapi/
│   ├── JakartaRestConfiguration.java          # JAX-RS Application config & base path
│   ├── controller/
│   │   ├── DiscoverResource.java              # GET /api/v1/ — discovery & HATEOAS
│   │   ├── SensorRoomResource.java            # /api/v1/rooms — Room CRUD
│   │   ├── SensorResource.java                # /api/v1/sensors — Sensor CRUD + locator
│   │   └── SensorReadingResource.java         # /api/v1/sensors/{id}/readings — sub-resource
│   ├── exception/
│   │   ├── GlobalExceptionMapper.java         # Catches all unhandled Throwables → 500
│   │   ├── LinkedResourceNotFoundException.java       # Sensor refs missing room
│   │   ├── LinkedResourceNotFoundExceptionMapper.java # → 422
│   │   ├── RoomNotEmptyException.java                 # Delete occupied room
│   │   ├── RoomNotEmptyExceptionMapper.java           # → 409
│   │   ├── SensorUnavailableException.java            # Reading to MAINTENANCE sensor
│   │   └── SensorUnavailableExceptionMapper.java      # → 503
│   ├── model/
│   │   ├── Room.java
│   │   ├── Sensor.java
│   │   ├── SensorReading.java
│   │   └── ErrorResponse.java
│   └── repository/
│       └── DataStore.java                     # In-memory static HashMap storage
├── SmartCampusAPI_Postman_Collection.json     # 18 Postman test cases
└── pom.xml
```

---

##  Conceptual Report

---

## Part 1 — Service Architecture & Setup

### Q1.1: What is the JAX-RS lifecycle of your resource classes, and what are the implications for thread safety with your chosen data storage?

By default in JAX-RS, resource classes such as `SensorRoomResource` and `SensorResource` are **request-scoped** — a brand-new instance is created for every single incoming HTTP request and discarded once the response is sent. This guarantees that any instance-level variables cannot leak state between concurrent clients, making the class instances themselves thread-safe.

However, the data storage layer — `DataStore` — uses **static `HashMap` fields** shared across all instances and all threads. This introduces a concurrency risk: if two clients simultaneously `POST` a new room, both threads could be mid-write at the same time, potentially corrupting the map. In a production-grade service this would be solved using `ConcurrentHashMap`, `synchronized` blocks, or a dedicated DAO layer backed by a relational database. For this in-memory coursework implementation the risk is acknowledged and accepted.

---

### Q1.2: Why did you implement a discovery endpoint, and what is HATEOAS? Why is it beneficial?

**What it is:** The `DiscoverResource` class is mapped to `@Path("/")` and responds to `GET /api/v1/` with a JSON payload containing API metadata (version, description, admin contact) and a `resources` map of hyperlinks to the primary collections.

```json
{
  "version": "1.0.0",
  "description": "Smart Campus Sensor & Room Management API",
  "admin_contact": "admin@campus.com",
  "resources": {
    "rooms":   "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

**HATEOAS (Hypermedia as the Engine of Application State)** is a REST constraint that requires the server to embed navigational links in every response, so that a client can discover the entire API by following links — just like browsing the web — without needing an external, hand-written routing document.

**Why it is beneficial:**
- **Self-documenting:** A new developer or client application can start at `/api/v1/` and discover every available resource without reading separate docs.
- **Decoupling:** If the server relocates an endpoint (e.g., `/sensors` moves to `/v2/sensors`), the client fetches the new link from the discovery response at runtime rather than having it hard-coded, reducing integration fragility.

---

## Part 2 — Room Management

### Q2.1: When returning room data, why do you return only `sensorIds` rather than the full embedded Sensor objects?

The `Room` model contains a `List<String> sensorIds` field rather than a `List<Sensor>` field. When `GET /api/v1/rooms` is called, the response returns the room with only the IDs of its associated sensors, not the full sensor payloads.

This is a deliberate **lazy-loading / reference-by-ID** design decision:

| Approach | Pros | Cons |
|---|---|---|
| **Embed full Sensor objects** (eager) | Single request for all data | Massive payload; clients receive unwanted sensor data ("over-fetching") |
| **Return `sensorIds` only** (lazy) | Small, fast payload | Client makes a second targeted request if sensor detail is needed |

Because most room-list consumers only need room metadata (name, location, capacity), returning IDs avoids over-fetching and keeps network payloads minimal. Clients that genuinely need sensor detail can make a subsequent `GET /api/v1/sensors/{id}` call — mirroring the REST principle of keeping resources independent and granular.

---

### Q2.2: Why must the HTTP `DELETE` operation be idempotent, and how does your implementation enforce this?

**Idempotency** means that making the same request *N* times produces exactly the same server state as making it once. The HTTP specification mandates that `DELETE` is idempotent: sending `DELETE /rooms/R-101` ten times should leave the server in the same state as sending it once (the room does not exist).

**How the implementation enforces this:**

```java
// SensorRoomResource.java — deleteRoom()
DataStore.rooms.remove(roomId);
return Response.noContent().build();   // 204 — no body, safe to retry
```

- The first call removes the room and returns **204 No Content**.
- Any subsequent call finds `room == null` and returns **404 Not Found** — the server state is still "room does not exist", which is idempotent from a resource-state perspective.
- Returning `204` (not `200`) is semantically correct because there is no entity body to return after deletion.

Additionally, the implementation protects **referential integrity**: if a room still has active sensors, `RoomNotEmptyException` is thrown (mapped to **409 Conflict**), preventing orphaned sensor records from accumulating in `DataStore`.

---

## Part 3 — Sensor Operations & Filtering

### Q3.1: How does your API validate sensor data, and how does it handle an unsupported `Content-Type`?

Two layers of validation protect the sensor creation endpoint (`POST /api/v1/sensors`):

1. **JAX-RS Framework — `@Consumes(MediaType.APPLICATION_JSON)`**  
   If a client sends a request with `Content-Type: application/xml` or any non-JSON type, the JAX-RS runtime rejects it **before the method is even invoked** and automatically returns **415 Unsupported Media Type**. No application code is needed to handle this case.

2. **Business Logic — Foreign-Key Validation**  
   Inside `createSensor()`, the provided `roomId` is looked up in `DataStore.rooms`. If the referenced room does not exist, `LinkedResourceNotFoundException` is thrown:

   ```java
   if (room == null) {
       throw new LinkedResourceNotFoundException(
           "Room with ID '" + sensor.getRoomId() + "' does not exist."
       );
   }
   ```

   The corresponding `LinkedResourceNotFoundExceptionMapper` maps this to **422 Unprocessable Entity**, correctly signalling that the payload was syntactically valid JSON but semantically invalid (the referenced resource does not exist).

---

### Q3.2: What is the difference between `@PathParam` and `@QueryParam`? When should each be used, and how does your API demonstrate this?

| | `@PathParam` | `@QueryParam` |
|---|---|---|
| **Location in URL** | Inside the URI path segment: `/sensors/{id}` | After `?` in the query string: `/sensors?type=CO2` |
| **Semantics** | Identifies a *specific, singular resource* | Acts as a *filter or modifier* on a collection |
| **Required?** | Typically required (absent = different route) | Optional — collection still returns if omitted |
| **REST best practice** | Use to navigate the resource hierarchy | Use for search, filter, sort, and pagination |

**How the API demonstrates both:**

```java
// @PathParam — identifies one specific sensor
@GET @Path("/{sensorId}")
public Response getSensor(@PathParam("sensorId") String sensorId) { ... }

// @QueryParam — optionally filters the whole collection
@GET
public List<Sensor> getSensors(@QueryParam("type") String type) {
    if (type != null && !type.isEmpty()) {
        return DataStore.sensors.values().stream()
               .filter(s -> s.getType().equalsIgnoreCase(type))
               .collect(Collectors.toList());
    }
    return new ArrayList<>(DataStore.sensors.values());
}
```

Using `@QueryParam` for filtering avoids URL bloat and keeps the core resource URI (`/sensors`) clean and stable, regardless of how many filter attributes are added in the future.

---

## Part 4 — Deep Nesting with Sub-Resources

### Q4.1: What is the Sub-Resource Locator pattern? Why did you use it instead of defining reading endpoints directly in `SensorResource`?

A **Sub-Resource Locator** is a method in a JAX-RS root resource class that returns *another resource object* (rather than a `Response`) and is annotated with `@Path` but **no HTTP method annotation**. The JAX-RS runtime then delegates further path matching and method dispatch to the returned object.

**Implementation:**

```java
// SensorResource.java — the locator method
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);   // hands off to the sub-resource
}
```

```java
// SensorReadingResource.java — the sub-resource (no @Path on the class itself)
public class SensorReadingResource {
    private final String sensorId;

    public SensorReadingResource(String sensorId) { this.sensorId = sensorId; }

    @GET  @Produces(APPLICATION_JSON)  public Response getReadings() { ... }
    @POST @Consumes(APPLICATION_JSON)  public Response addReading(SensorReading r) { ... }
}
```

**Why this pattern instead of putting everything in `SensorResource`:**

- **Single Responsibility Principle:** `SensorResource` handles sensor CRUD; `SensorReadingResource` handles reading history. Neither class grows into a "God class".
- **Isolation & Testability:** Reading-specific logic (MAINTENANCE status check, timestamp management) is contained in one class and can be unit-tested independently.
- **Scalability:** Adding new sub-resources (e.g., `/sensors/{id}/alerts`) requires creating a new class and one additional locator line — no modification to existing sensor logic.

---

### Q4.2: When a new reading is `POST`ed, what side effect occurs on the parent `Sensor`, and why?

When a client posts a new reading via `POST /api/v1/sensors/{sensorId}/readings`, the `SensorReadingResource.addReading()` method performs two operations:

1. **Stores the new `SensorReading`** in `DataStore.readings`.
2. **Updates the parent `Sensor`'s `currentValue`** to reflect the latest measurement:

```java
// SensorReadingResource.java — addReading()
reading.setSensorId(sensorId);
DataStore.readings.add(reading);

// Side effect: keep the parent sensor's live value in sync
sensor.setCurrentValue(reading.getValue());
```

**Why this side effect is correct design:**  
The `Sensor.currentValue` field is meant to represent the *most recent live reading* of that sensor. Without this update, `GET /api/v1/sensors/{id}` would always return a stale `currentValue` even after dozens of new readings had been logged. By synchronising the parent at write-time, any subsequent `GET` on the sensor instantly reflects the latest measurement without requiring an additional join or aggregation query.

---

## Part 5 — Error Handling, Mapping & Logging

### Q5.1: Why do you return `422 Unprocessable Entity` instead of `404 Not Found` when a sensor references a non-existent room?

When `POST /api/v1/sensors` is called with a `roomId` that does not exist in `DataStore`, the API throws `LinkedResourceNotFoundException`, which is mapped to **422 Unprocessable Entity**.

**Why `422` and not `404`:**

- **`404 Not Found`** means the *URL itself* was not found on the server. Returning `404` on `POST /api/v1/sensors` would falsely imply the `/sensors` endpoint does not exist — which is misleading, because it does.
- **`422 Unprocessable Entity`** means: *"I received your request at the correct URL, I parsed the JSON without error, but the semantic content of the body is invalid — specifically, the `roomId` field references a resource that doesn't exist."*

| Status | Meaning to client | Correct action |
|---|---|---|
| `404` | URL is wrong | Fix the URL |
| `422` | URL is correct, body data is wrong | Fix the `roomId` in the request body |

Similarly, **`409 Conflict`** is returned (via `RoomNotEmptyExceptionMapper`) when attempting to delete a room that still has sensors, because it is a business-rule conflict, not a routing error.

---

### Q5.2: What is the purpose of the `GlobalExceptionMapper<Throwable>`, and why is hiding internal error details a security requirement?

The `GlobalExceptionMapper` is annotated with `@Provider` so Jersey auto-discovers and registers it. It implements `ExceptionMapper<Throwable>`, meaning it catches **every unhandled exception** that escapes any resource method — including unexpected `NullPointerException`, `IllegalArgumentException`, and any other runtime crashes.

```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", "error");
        errorDetails.put("message", exception.getMessage());
        errorDetails.put("type", exception.getClass().getSimpleName());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(errorDetails)
                       .build();
    }
}
```

**Security justification — why hiding stack traces matters:**

Without this mapper, an unhandled exception causes the servlet container (Tomcat) to return a raw HTML page containing the full Java stack trace. That stack trace exposes:

- **Internal package paths** (`com.example.smartcampusapi.repository.DataStore`) — revealing software architecture.
- **Library versions** — allowing an attacker to cross-reference known CVEs for that exact version.
- **Class and method names** — giving attackers a precise map of application internals to target.

The `GlobalExceptionMapper` replaces this information leak with a clean, controlled JSON response (`500 Internal Server Error`) that tells the client something went wrong **without disclosing why or how** — a fundamental principle of **Secure by Default** API design.

> **Demonstration endpoint:** `GET /api/v1/error` (in `DiscoverResource`) deliberately throws a `RuntimeException`. Without the global mapper this would dump a stack trace; with it, the client receives a tidy JSON `500` error.

---

### Q5.3: How does `SensorUnavailableException` protect data integrity when a sensor is under maintenance?

When `POST /api/v1/sensors/{sensorId}/readings` is called, the `SensorReadingResource.addReading()` method checks the sensor's status before accepting the reading:

```java
if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
    throw new SensorUnavailableException(
        "Sensor " + sensorId + " is currently under MAINTENANCE and cannot accept new readings."
    );
}
```

The `SensorUnavailableExceptionMapper` maps this to **503 Service Unavailable**, which correctly communicates:

> *"The server is reachable and the endpoint is valid, but this specific resource is temporarily out of service."*

**Why this matters for data integrity:** Accepting readings from a sensor in MAINTENANCE mode risks recording inaccurate or calibration-drift data into the historical log. By blocking writes at the API layer, the historical `DataStore.readings` list remains a reliable, trustworthy record of operational sensor output only.

---

### Q5.4: What cybersecurity risks arise from exposing Java stack traces in API responses?

Stack traces expose internal package paths, method names, and third-party library versions. Attackers use this **"fingerprinting"** to identify known CVE vulnerabilities in specific versions of Jersey or Tomcat. Our `GlobalExceptionMapper` prevents this by returning a sanitized `500 Internal Server Error` response, adhering to the **Principle of Least Privilege** — the client receives only what it needs to know: that an error occurred.

---

### Q5.5: Why use JAX-RS filters for cross-cutting concerns like logging?

Filters handle cross-cutting concerns like logging in a single, dedicated place. This ensures consistency, adheres to the **DRY (Don't Repeat Yourself)** principle, and crucially captures interactions that fail **before** even reaching a resource method (such as `415 Unsupported Media Type` errors), which manual logging inside each method would entirely miss.

---

##  Summary of HTTP Status Codes Used

| Code | Meaning | When Used |
|---|---|---|
| `200 OK` | Success with body | `GET` requests returning data |
| `201 Created` | Resource successfully created | `POST /rooms`, `POST /sensors`, `POST /readings` |
| `204 No Content` | Success, no body | `DELETE /rooms/{id}` |
| `400 Bad Request` | Malformed request | Missing required fields (e.g., Room ID) |
| `404 Not Found` | Resource does not exist | `GET`/`DELETE` on unknown ID |
| `409 Conflict` | Business rule violation | Deleting a room that still has sensors |
| `415 Unsupported Media Type` | Wrong `Content-Type` | Sending XML to a JSON endpoint |
| `422 Unprocessable Entity` | Invalid reference in body | Sensor references a non-existent room |
| `500 Internal Server Error` | Unexpected server crash | Caught by `GlobalExceptionMapper` |
| `503 Service Unavailable` | Resource temporarily unavailable | Posting a reading to a MAINTENANCE sensor |
