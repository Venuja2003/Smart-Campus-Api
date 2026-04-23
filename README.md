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

- Java 11 or higher
- Maven 3.6+
- Apache Tomcat 9.x

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the WAR file

```bash
mvn clean package
```

This produces `target/smart-campus-api.war`.

### Step 3 — Deploy to Tomcat

Copy the WAR file into Tomcat's `webapps` directory:

```bash
cp target/smart-campus-api.war /path/to/tomcat/webapps/
```

### Step 4 — Start Tomcat

```bash
/path/to/tomcat/bin/startup.sh       # Linux/macOS
/path/to/tomcat/bin/startup.bat      # Windows
```

### Step 5 — Verify the API is Running

```bash
curl http://localhost:8080/smart-campus-api/api/v1
```

You should see the discovery JSON response with API metadata and links.

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

### Part 1.1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS follows a **per-request lifecycle**: a brand new instance of each resource class is created for every incoming HTTP request and discarded after the response is sent. The runtime does **not** treat resource classes as singletons.

**Impact on in-memory data management:**  
Because each request gets a fresh resource instance, any data stored as instance fields in a resource class would be lost immediately after the request completes. This means shared, persistent state such as our Room and Sensor collections **must not** live inside resource classes.

To solve this, we use a **`DataStore` singleton** — a single shared object that holds all `ConcurrentHashMap` collections. The `DataStore` is instantiated once when the application starts and is referenced via `DataStore.getInstance()` from every resource class. Since multiple HTTP requests can arrive simultaneously, we use `ConcurrentHashMap` instead of a plain `HashMap` to prevent race conditions and data corruption from concurrent read/write operations.

---

### Part 1.2 — HATEOAS and Hypermedia in REST

**HATEOAS** (Hypermedia As The Engine Of Application State) means that API responses include hyperlinks pointing to related or next-available actions, making the API self-describing and navigable.

**Why it is a hallmark of advanced REST design:**  
A truly RESTful API should not require clients to consult external static documentation to discover URLs. Instead, every response carries the links a client needs to take the next logical action. For example, a Room response might include a `_links` object with `sensors` pointing to `/api/v1/rooms/LIB-301/sensors`.

**Benefits over static documentation:**
- **Discoverability:** Clients start at one root URL and navigate the entire API from responses alone.
- **Reduced coupling:** Clients follow links rather than hardcoding URLs, so server-side URL changes don't break clients.
- **Self-documentation:** New developers can explore the API without reading a separate spec.
- **Evolvability:** New capabilities can be added and exposed via links without requiring client updates.

---

### Part 2.1 — Returning IDs vs Full Objects in List Responses

**Returning only IDs** (`["LIB-301", "LAB-101"]`):
- Minimal payload size, very fast for large collections.
- Forces the client to make **N additional GET requests** to fetch details for each room — the classic "N+1 problem" — causing high network overhead and latency.
- Appropriate for extremely large datasets where the client rarely needs all details.

**Returning full objects** (our approach):
- Clients receive everything needed in a single request.
- Slightly larger payload, but for a domain like campus rooms (small objects, manageable count) this is negligible.
- Reduces total number of HTTP round trips significantly.

**Conclusion:** Returning full objects is the right choice here given the small data size. For very large collections, the best practice is to return full objects with server-side **pagination** (e.g., `?page=1&size=20`).

---

### Part 2.2 — Is DELETE Idempotent?

**Yes, DELETE is idempotent** in this implementation — but with a nuance.

**Idempotency** means that making the same request multiple times produces the same server state as making it once. After deleting room `CONF-201`, the room is absent from the data store. Sending the same `DELETE /rooms/CONF-201` request again does not change that state — the room is still absent.

However, the **HTTP response code differs**:
- First call: `200 OK` (room deleted successfully).
- Subsequent calls: `404 Not Found` (room does not exist).

This is acceptable and correct. RFC 7231 defines idempotency in terms of **server state**, not response codes. The state outcome is identical (room is gone), satisfying the idempotency requirement.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that a method only accepts requests with `Content-Type: application/json`.

**If a client sends `Content-Type: text/plain` or `application/xml`:**  
JAX-RS inspects the incoming `Content-Type` header before invoking any method. If no resource method matches both the path and the content type, the runtime automatically returns **HTTP 415 Unsupported Media Type** — without the method body ever executing. This protects the server from attempting to deserialize incompatible payloads, which would otherwise cause runtime errors or silent data corruption.

---

### Part 3.2 — Query Parameter vs Path Segment for Filtering

**Query parameter approach** (`GET /sensors?type=CO2`) is superior because:

1. **Semantic correctness:** Query parameters are designed for filtering, searching, and sorting an existing collection. A path segment (`/sensors/type/CO2`) implies `type` is a distinct sub-resource of `sensors`, which is architecturally misleading.

2. **No URL conflicts:** `/sensors/{sensorId}` already uses the first path segment for sensor IDs. Adding `/sensors/type/CO2` creates an ambiguous conflict with `/sensors/CO2` (is "CO2" a sensor ID or a type?).

3. **Composability:** Multiple filters combine naturally and readably: `?type=CO2&status=ACTIVE`. Path-based filtering does not compose cleanly.

4. **REST convention:** All major REST APIs (GitHub, Twitter, Stripe) use query parameters for filtering and path segments for resource identity. Following this convention improves developer familiarity.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern delegates request handling for nested paths to a dedicated class, rather than defining every nested route in one monolithic resource class.

**Benefits:**

1. **Single Responsibility Principle:** `SensorResource` manages sensor CRUD; `SensorReadingResource` manages reading history. Each class has one clear purpose.

2. **Manageable complexity:** In a large campus API with many nested resources (rooms → zones → sensors → readings → alerts), defining every path in one class would produce thousands of lines of tangled code. Separate classes keep each file focused.

3. **Testability:** Each sub-resource class can be unit tested independently with its own mock dependencies.

4. **Reusability:** A sub-resource class could theoretically be reused from multiple parent paths if needed.

5. **Encapsulation:** The parent resource handles authentication and validation of the parent entity (does the sensor exist?), then passes a clean context to the child resource to handle its own logic.

---

### Part 5.2 — Why HTTP 422 Is More Accurate Than 404

When a `POST /sensors` request contains a `roomId` that does not exist:

- **404 Not Found** is semantically wrong here because it implies the **endpoint URL itself** was not found, which is misleading — the `/api/v1/sensors` endpoint exists and responded.

- **400 Bad Request** is closer but implies a syntactic issue (malformed JSON), whereas the JSON is perfectly valid.

- **422 Unprocessable Entity** is the most accurate choice because:
  - The request URL was valid (endpoint found ✓).
  - The JSON payload was syntactically correct (parseable ✓).
  - The issue is **semantic**: the referenced foreign key (`roomId`) inside a valid payload points to a non-existent resource — a **logical data integrity violation**.
  - 422 tells the client precisely: "Your request structure is fine, but the content cannot be processed due to a business rule violation."

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Returning raw Java stack traces to external API consumers poses serious security risks:

1. **Technology fingerprinting:** Stack traces reveal the exact server framework (Jersey, Tomcat), Java version, and library names/versions. Attackers can cross-reference these with known CVE databases to find exploitable vulnerabilities in those specific versions.

2. **Internal architecture exposure:** Package names and class names (e.g., `com.smartcampus.resource.SensorResource`) reveal the internal project structure, making it easier to craft targeted attacks.

3. **File path leakage:** Stack traces often include absolute file paths (e.g., `/home/deploy/smart-campus/src/...`), revealing server directory structures useful for path traversal attacks.

4. **Sensitive data in exception messages:** Exception messages sometimes include the actual values that caused the error (e.g., SQL query fragments, user input), inadvertently leaking business data.

5. **Attack road map:** Line numbers in stack traces let attackers correlate with decompiled bytecode to pinpoint exactly where to inject malicious input.

**Mitigation:** The `GlobalExceptionMapper` catches all unhandled exceptions, logs the full stack trace securely on the server (for developers), and returns only a generic safe message to the client.

---

### Part 5.5 — Why Use Filters for Cross-Cutting Concerns

Using a JAX-RS filter (`LoggingFilter`) for logging is far superior to manually adding `Logger.info()` inside every resource method:

1. **DRY Principle:** One filter class covers every endpoint automatically. A new resource class added tomorrow gets logging for free with zero code changes.

2. **Separation of Concerns:** Resource classes contain only business logic. Logging is an infrastructure concern and belongs in dedicated infrastructure code.

3. **Consistency:** All log entries share the same format. Manual logging leads to inconsistent formats across developers and methods.

4. **Reliability:** It is impossible to accidentally forget to log a new endpoint. Manual approaches always risk omissions.

5. **Maintainability:** Changing the log format, adding a request ID, or switching to a structured logging format requires modifying one file instead of dozens of resource classes.

This same principle applies to other cross-cutting concerns such as authentication, CORS headers, rate limiting, and request tracing — all implemented cleanly as filters without polluting business logic.
