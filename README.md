# Smart Campus Sensor & Room Management API

A RESTful API built with **Java + JAX-RS (Jersey)** deployed on **Apache Tomcat**.  
This system provides full management of campus Rooms and IoT Sensors, including historical reading logs.

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Project Structure](#project-structure)
3. [Build & Run Instructions](#build--run-instructions)
4. [API Endpoints](#api-endpoints)
5. [Sample curl Commands](#sample-curl-commands)
6. [Conceptual Report — Question Answers](#conceptual-report--question-answers)

---

## API Overview

| Property | Value |
|---|---|
| Base URL | `http://localhost:8080/smart-campus-api/api/v1` |
| Format | JSON (`application/json`) |
| Data Storage | In-memory `ConcurrentHashMap` (no database) |
| Framework | JAX-RS 2.1 via Jersey 2.41 |
| Server | Apache Tomcat 9 |

### Resources

| Resource | Path |
|---|---|
| Discovery | `GET /api/v1` |
| Rooms | `/api/v1/rooms` |
| Sensors | `/api/v1/sensors` |
| Sensor Readings | `/api/v1/sensors/{sensorId}/readings` |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/
    ├── java/com/smartcampus/
    │   ├── application/
    │   │   ├── SmartCampusApplication.java   # JAX-RS app entry point
    │   │   └── DataStore.java                # Singleton in-memory data store
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   └── SensorReading.java
    │   ├── resource/
    │   │   ├── DiscoveryResource.java        # GET /api/v1
    │   │   ├── RoomResource.java             # /api/v1/rooms
    │   │   ├── SensorResource.java           # /api/v1/sensors
    │   │   └── SensorReadingResource.java    # /api/v1/sensors/{id}/readings
    │   ├── exception/
    │   │   ├── RoomNotEmptyException.java
    │   │   ├── LinkedResourceNotFoundException.java
    │   │   ├── SensorUnavailableException.java
    │   │   ├── RoomNotEmptyExceptionMapper.java
    │   │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   │   ├── SensorUnavailableExceptionMapper.java
    │   │   └── GlobalExceptionMapper.java
    │   └── filter/
    │       └── LoggingFilter.java
    └── webapp/WEB-INF/
        └── web.xml
```

---

## Build & Run Instructions

### Prerequisites

- Apache NetBeans 12 or higher
- Java JDK 11 or higher
- Apache Tomcat 9.x registered in NetBeans
- Postman (for testing the API endpoints)

### Step 1 — Clone or Download the Repository

Clone the repository or download and extract the ZIP, then note the folder location on your machine.

### Step 2 — Open the Project in NetBeans

Open NetBeans and go to **File → Open Project**. Navigate to the project folder (the one containing `pom.xml`) and click **Open Project**. NetBeans will automatically detect it as a Maven project and resolve all dependencies declared in `pom.xml`. Wait for the dependency download to complete — progress is shown in the bottom status bar.

### Step 3 — Register Apache Tomcat in NetBeans (if not already done)

Go to **Tools → Servers → Add Server**. Select **Apache Tomcat or TomEE** and click **Next**. Browse to the root folder of your Tomcat 9 installation (e.g., `C:\apache-tomcat-9.0.x` on Windows or `/opt/tomcat` on macOS/Linux) and click **Finish**.

### Step 4 — Set Tomcat as the Project Server

Right-click the project in the **Projects** panel and select **Properties**. Under the **Run** category, set the **Server** to the Apache Tomcat instance registered in Step 3. Set the **Context Path** to `/smart-campus-api` and click **OK**.

### Step 5 — Clean and Build the Project

Right-click the project and select **Clean and Build**. NetBeans will compile the source files and package everything into `target/smart-campus-api.war`. Confirm there are no build errors in the **Output** tab.

### Step 6 — Run the Project

Right-click the project and select **Run**. NetBeans will deploy the WAR file to Tomcat and start the server automatically. The **Output** tab will show Tomcat startup logs. Once you see `INFO: Server startup in [X] ms`, the API is live.

### Step 7 — Verify the API is Running

Open Postman or a browser and send a GET request to:

```
http://localhost:8080/smart-campus-api/api/v1
```

A JSON response containing API metadata and resource links confirms the server is running correctly.

---

## API Endpoints

### Discovery

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1` | Returns API metadata and resource links |

### Rooms

| Method | Path | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (fails if sensors present) | 200 |

### Sensors

| Method | Path | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors` | List all sensors | 200 |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type | 200 |
| POST | `/api/v1/sensors` | Register a new sensor | 201 |
| GET | `/api/v1/sensors/{sensorId}` | Get sensor by ID | 200 |
| DELETE | `/api/v1/sensors/{sensorId}` | Delete sensor | 200 |

### Sensor Readings

| Method | Path | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading | 201 |

### Error Codes

| Code | Meaning | Trigger |
|---|---|---|
| 400 | Bad Request | Missing required fields |
| 403 | Forbidden | POST reading to MAINTENANCE sensor |
| 404 | Not Found | Resource ID does not exist |
| 409 | Conflict | Delete room with sensors / duplicate ID |
| 415 | Unsupported Media Type | Wrong Content-Type sent |
| 422 | Unprocessable Entity | Sensor references non-existent roomId |
| 500 | Internal Server Error | Unexpected runtime error |

---

## Sample curl Commands

> **Base URL:** `http://localhost:8080/smart-campus-api/api/v1`  
> All POST requests require `-H "Content-Type: application/json"`

---

### 1. Get API Discovery Info

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1
```

**Expected:** 200 OK with API metadata and `_links` map.

---

### 2. List All Rooms

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

**Expected:** 200 OK with JSON array of all rooms.

---

### 3. Create a New Room

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CONF-201",
    "name": "Conference Room B",
    "capacity": 20
  }'
```

**Expected:** 201 Created with the new room object.

---

### 4. Get a Single Room by ID

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

**Expected:** 200 OK with room details for `LIB-301`.

---

### 5. Attempt to Delete a Room That Has Sensors (409 Conflict)

```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

**Expected:** 409 Conflict — room still has sensors assigned.

---

### 6. List All Sensors

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors
```

**Expected:** 200 OK with all sensors.

---

### 7. Filter Sensors by Type

```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

**Expected:** 200 OK with only Temperature sensors.

---

### 8. Register a New Sensor (with valid roomId)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-002",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 400.0,
    "roomId": "CONF-201"
  }'
```

**Expected:** 201 Created with the new sensor object.

---

### 9. Register a Sensor with Non-Existent roomId (422 Error)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-999",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 21.0,
    "roomId": "DOES-NOT-EXIST"
  }'
```

**Expected:** 422 Unprocessable Entity — roomId not found.

---

### 10. Get Sensor Reading History

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

**Expected:** 200 OK with reading history for sensor `TEMP-001`.

---

### 11. Post a New Reading (updates sensor's currentValue)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 24.7}'
```

**Expected:** 201 Created. The sensor's `currentValue` is updated to `24.7`.

---

### 12. Post Reading to MAINTENANCE Sensor (403 Forbidden)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 15.0}'
```

**Expected:** 403 Forbidden — sensor `OCC-001` is in MAINTENANCE status.

---

### 13. Delete a Sensor (then retry room deletion)

```bash
# First delete the sensors in LIB-301
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001

# Now delete the empty room — should succeed
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

**Expected:** 200 OK on the room deletion once all sensors are removed.

---

## Conceptual Report — Question Answers

---

### Part 1.1 — Project & Application Configuration

**Question: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

By default, JAX-RS follows a per-request lifecycle, meaning a new instance of each resource class is created for every incoming HTTP request and discarded once the response is sent. The runtime does not treat resource classes as singletons. Because of this, any data stored as an instance field inside a resource class would be lost immediately after the request completes, making it impossible to share state such as the Room or Sensor collections across requests. To solve this, a `DataStore` singleton is used — instantiated once at application startup and referenced by all resource classes via `DataStore.getInstance()`. Since multiple requests can arrive simultaneously on different threads, the collections are backed by `ConcurrentHashMap` rather than a plain `HashMap`, which prevents race conditions and data corruption from concurrent read and write operations.

---

### Part 1.2 — The "Discovery" Endpoint

**Question: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

HATEOAS stands for Hypermedia As The Engine Of Application State. This means that API responses must provide links to other related resources and possible next steps instead of expecting the client application to know the appropriate URL ahead of time. HATEOAS is seen as a hallmark of advanced REST design due to the original definition of REST, which says that a RESTful API should be self-descriptive. In a RESTful API, one must only have the root URL, and from there, one can navigate through all the features of the API based on the links within the response, similar to browsing through a website. Unlike static documentation, using HATEOAS decreases the level of coupling between the client application and the server by relying on links instead of pre-defined URL patterns. It also improves discoverability and evolvability, as new resources or changed URLs can be exposed through links without breaking existing clients or requiring documentation updates.

---

### Part 2.1 — Room Resource Implementation

**Question: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.**

Returning only IDs produces a smaller initial payload, which reduces bandwidth on the first request. However, it forces the client to make a separate GET request for every ID to retrieve usable details — a pattern known as the N+1 problem — which results in significantly more HTTP round trips, higher latency, and increased client-side complexity. Returning full room objects resolves this by delivering all necessary data in a single request. While the payload is larger, room objects in this domain are small and the total count manageable, so the overhead is negligible compared to the cost of multiple additional requests. For very large datasets, the recommended approach is to return full objects alongside server-side pagination rather than falling back to ID-only responses.

---

### Part 2.2 — Room Deletion & Safety Logic

**Question: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

Yes, the DELETE operation is idempotent in this implementation. As defined in RFC 7231, idempotency means that sending the same request multiple times produces the same server state as sending it once — it does not require identical response codes across calls. On the first DELETE request for a room, the room is removed from the data store and a `200 OK` is returned. On any subsequent identical request, the room no longer exists, so the server returns `404 Not Found`. The server state in both cases is the same — the room is absent — and no further state change occurs on repeated calls. The difference in response code does not violate idempotency, since RFC 7231 defines it strictly in terms of side effects on server state rather than response uniformity.

---

### Part 3.1 — Sensor Resource & Integrity

**Question: We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation restricts the POST method to only accept requests carrying a `Content-Type: application/json` header. If a client sends a request with a mismatched content type such as `text/plain` or `application/xml`, the JAX-RS runtime performs content negotiation before the method body is invoked at all. Since no resource method is found that matches both the request path and the supplied content type, the runtime automatically rejects the request and returns an HTTP 415 Unsupported Media Type response without executing any application logic. This prevents the server from attempting to deserialise an incompatible payload, which would otherwise cause runtime parsing errors or produce silently corrupted data objects.

---

### Part 3.2 — Filtered Retrieval & Search

**Question: You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?**

Query parameters are semantically intended for filtering, searching, and sorting an existing collection, whereas path segments are designed to identify a specific, distinct resource. Using a path-based approach such as `/sensors/type/CO2` implies that `type` is a sub-resource of sensors, which is architecturally misleading. It also creates a URL conflict with the existing `/sensors/{sensorId}` pattern, since the router cannot reliably distinguish whether a segment like `CO2` is a sensor ID or a filter keyword. Query parameters avoid this entirely, are inherently optional — when omitted, the full collection is returned — and compose naturally for multiple filters such as `?type=CO2&status=ACTIVE`. This approach also aligns with established industry conventions followed by major APIs including GitHub and Stripe, where path segments identify resources and query parameters refine or filter them.

---

### Part 4.1 — The Sub-Resource Locator Pattern

**Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?**

The Sub-Resource Locator pattern delegates handling of nested paths to dedicated classes rather than accumulating every route in a single parent resource. This directly supports the Single Responsibility Principle — `SensorResource` handles sensor management while `SensorReadingResource` focuses exclusively on reading history, making each class shorter, more focused, and easier to maintain. In a large API where resources nest deeply, placing every path in one class would produce an unmanageable file with thousands of lines of mixed concerns. Separating into sub-resource classes also improves testability, since each class can be instantiated and tested in isolation without constructing the full resource hierarchy. At runtime, JAX-RS resolves the path dynamically by invoking the locator method to obtain the sub-resource instance, then continues dispatching the remaining path into that instance's annotated methods.

---

### Part 5.2 — Dependency Validation (422 Unprocessable Entity)

**Question: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

HTTP 404 is intended to indicate that the request URL itself could not be resolved to a resource on the server. In this scenario, the endpoint `/api/v1/sensors` exists and responded correctly, so returning 404 would be misleading. HTTP 400 Bad Request is closer but typically signals a syntactic problem such as malformed JSON, whereas the payload here is structurally valid and parsed without issue. HTTP 422 Unprocessable Entity is the most accurate choice because it signals that the request was understood — the URL was valid and the JSON well-formed — but the server cannot process it due to a semantic violation: a `roomId` referenced inside a valid payload points to a resource that does not exist. This distinction matters to client developers because 422 immediately communicates that the fix lies in the content of the request, not its structure or the URL used.

---

### Part 5.4 — The Global Safety Net (500)

**Question: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

Showing the original Java stack trace to consumers is a type of information leak that may help the attacker significantly. The stack trace provides specific server implementation details, library names, and versions, like Jersey or Tomcat, which can be checked against known vulnerabilities from the CVE database. Fully qualified names for classes and packages show the internal implementation details of the software without having access to the source code. File system absolute paths present in the stack trace show the file system structure on the server. In some cases, the message associated with an exception provides the exact value that caused the problem, thus leaking sensitive user data or even business data. The combination of line number information and reverse engineering of the compiled code helps determine specific code paths that are vulnerable. The GlobalExceptionMapper addresses all of these risks by logging the full trace securely on the server for diagnostic purposes while returning only a generic 500 response to the client.

---

### Part 5.5 — API Request & Response Logging Filters

**Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?**

Using a JAX-RS filter for logging is preferable to manual per-method logging because it enforces separation of concerns — resource classes should contain only business logic, while logging is an infrastructure responsibility that does not belong mixed into domain code. A single filter automatically covers every endpoint in the API, meaning a newly added resource class receives logging with no additional code changes. Manual logging, by contrast, is inherently inconsistent and error-prone: different developers format log statements differently, and endpoints are routinely missed. Filters also centralise maintenance — changing the log format or adding a request correlation ID requires modifying one class rather than updating every resource method across the codebase. The same rationale applies equally to other cross-cutting concerns such as authentication, CORS handling, and rate limiting, all of which are better placed in dedicated filters than scattered through business logic.
