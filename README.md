Smart Campus Sensor & Room Management API
A professional Jakarta EE RESTful web service built with JAX-RS (Jersey 2.x) designed to manage thousands of campus rooms and IoT sensors. This system implements high-performance in-memory data structures and a resilient error-handling strategy.

Student: Fathima Amnaah Aakiff - w2120196

Course: Client-Server Architectures (5COSC022W)

University: University of Westminster (IIT)

The video showcases all 18 test cases running on a live Tomcat 9 server, with my student ID and clear verbal explanations.

🛠 Technology Stack
Java 17 (LTS)

JAX-RS (Jersey 2.x) - Framework for RESTful Web Services

Apache Tomcat 9.0.x - Servlet Container

Maven - Project Build and Dependency Management

Postman - API Testing and Documentation

🚀 How to Build and Run
Prerequisites
JDK 17 installed and configured in your environment.

Apache Tomcat 9.0.x configured within NetBeans or your preferred IDE.

Step-by-Step Launch
Clone the Project:

Bash
git clone https://github.com/YourUsername/SmartCampusAPI.git
Build:
In NetBeans, right-click the project and select Clean and Build. This generates the SmartCampusAPI.war file in the target directory.

Deploy:
Right-click the project and select Run. NetBeans will deploy the application to Tomcat 9.

Access:
The API is live at: http://localhost:8081/SmartCampusAPI/api/v1/.

Testing:
Import the SmartCampusAPI_Postman_Collection.json file into Postman and set the baseUrl variable to the address above.

📡 Sample curl Commands
Industry-standard CLI interactions for testing core resources:

1. Root Discovery (HATEOAS)

Bash
curl -X GET http://localhost:8081/SmartCampusAPI/api/v1/
2. Create a Campus Room

Bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Study", "capacity": 50}'
3. Register a Sensor (Linked to Room)

Bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "roomId": "LIB-301", "type": "Temperature", "status": "ACTIVE", "currentValue": 22.5}'
4. Filter Sensors by Type (QueryParam)

Bash
curl -X GET "http://localhost:8081/SmartCampusAPI/api/v1/sensors?type=Temperature"
5. Post a Historical Sensor Reading (Sub-Resource)

Bash
curl -X POST http://localhost:8081/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 24.2}'
📝 Conceptual Report (Question Answers)
Part 1: Service Architecture & Setup
Q1.1: Explain the default lifecycle of a JAX-RS Resource class and its impact on thread safety. In JAX-RS, resource classes are request-scoped by default. A new instance is instantiated for every incoming request and destroyed upon response delivery. While this makes the class instance thread-safe, it prevents data persistence in standard instance variables. Consequently, our DataStore utilizes static fields shared across all threads. To prevent race conditions during concurrent writes (e.g., multiple sensors updating at once), we acknowledge that production systems require ConcurrentHashMap or synchronization blocks to maintain data integrity.

Q1.2: Why implement a "Discovery" endpoint? How does HATEOAS benefit client developers? HATEOAS (Hypermedia as the Engine of Application State) provides navigational links within JSON responses, making the API self-documenting. By implementing a Discovery endpoint at GET /api/v1/, we decouple the client from hard-coded URLs. If an endpoint structure changes, the client discovers the new path dynamically at runtime, reducing fragility compared to relying on static external documentation.

Part 2: Room Management
Q2.1: What are the implications of returning only IDs versus full room objects? Returning only sensorIds is a lazy-loading approach that keeps network bandwidth usage low and responses fast. However, returning full objects (as implemented here) solves the N+1 problem, where a client would otherwise have to make N additional requests to fetch details for every room in a list. For management dashboards, full-object retrieval is far more efficient despite the larger initial payload.

Q2.2: Is the DELETE operation idempotent? Provide a justification. Yes, the DELETE operation is idempotent. The first successful request removes the resource (204 No Content). Any subsequent identical requests will find the resource missing and return 404 Not Found. While the status codes differ, the final server state (the room being absent) remains the same regardless of repeated calls, satisfying the idempotency requirement.

Part 3: Sensor Operations & Linking
Q3.1: Explain the consequences of an unsupported Content-Type mismatch. Using @Consumes(MediaType.APPLICATION_JSON) ensures the API only processes JSON. If a client sends text/plain or application/xml, the JAX-RS runtime intercepts this mismatch before the method body is even executed and automatically returns HTTP 415 Unsupported Media Type.

Q3.2: Contrast @QueryParam with path-based filtering. @QueryParam is superior for filtering because it keeps the URI path focused on resource identity rather than optional search criteria. Path-based filtering (e.g., /type/CO2) implies a resource hierarchy that doesn't exist and becomes unmanageable when combining multiple filters (e.g., type AND status).

Part 4: Deep Nesting with Sub-Resources
Q4.1: Discuss the architectural benefits of the Sub-Resource Locator pattern. Delegating logic to a separate SensorReadingResource class adheres to the Single Responsibility Principle. It prevents a "God class" from managing all operations, making the code more modular, testable, and easier to scale as new nested features (like sensor alerts) are added.

Part 5: Error Handling, Mapping & Logging
Q5.2: Why is HTTP 422 more semantically accurate than 404 for missing references? A 404 implies the requested URL is missing. However, when a roomId inside a JSON body is missing, the URL is valid, but the semantic content is unprocessable. HTTP 422 accurately informs the developer that the syntax was correct, but the internal reference is broken.

Q5.4: Explain the cybersecurity risks of exposing Java stack traces. Stack traces expose internal package paths, method names, and third-party library versions. Attackers use this "fingerprinting" to identify known CVE vulnerabilities in specific versions of Jersey or Tomcat. Our GlobalExceptionMapper prevents this by returning a sanitized 500 Internal Server Error.

Q5.5: Why use JAX-RS filters for cross-cutting concerns? Filters handle cross-cutting concerns like logging in a single, dedicated place. This ensures consistency, adheres to the DRY (Don't Repeat Yourself) principle, and captures interactions that fail before even reaching a resource method (such as 415 errors), which manual logging would miss.

## Project Structure

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
│   │   ├── LinkedResourceNotFoundEx# Smart Campus Sensor & Room Management API

A professional Jakarta EE RESTful web service built with **JAX-RS (Jersey 2.x)** designed to manage a comprehensive campus-wide infrastructure of rooms and IoT sensors. This system demonstrates industry-standard practices, including resource nesting, resilient error handling, and HATEOAS-driven discovery.

**Student:** Fathima Amnaah Aakiff - w2120196  
**Course:** Client-Server Architectures (5COSC022W)  
**University:** University of Westminster (IIT)

---

## 📺 Video Demonstration
**Watch the Postman Test Demonstration:** [Link to your Video (OneDrive/YouTube)]  
*Note: As per specification requirements, this video demonstrates all 18 test cases on a live Tomcat 9 server with my physical presence and clear verbal explanation.*

---

## 🛠 Technology Stack
* **Java 17**
* **JAX-RS (Jersey 2.x)** - Core REST Framework
* **Apache Tomcat 9.0.x** - Servlet Container
* **Maven** - Build Tool
* **Postman** - API Testing Collection

---

## 🚀 How to Build and Run

### Prerequisites
* JDK 17 installed.
* Apache Tomcat 9.0.x configured in NetBeans.

### Step-by-Step Launch
1. **Clone the Project:**
   ```bash
   git clone [https://github.com/YourUsername/SmartCampusAPI.git](https://github.com/YourUsername/SmartCampusAPI.git)eption.java       # Sensor refs missing room
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
