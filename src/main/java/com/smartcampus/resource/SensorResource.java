package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource class managing /api/v1/sensors
 *
 * @Consumes(MediaType.APPLICATION_JSON) BEHAVIOUR:
 * If a client sends a POST with Content-Type: text/plain or application/xml,
 * JAX-RS will reject the request before it even reaches the method body.
 * The runtime returns HTTP 415 Unsupported Media Type automatically, because no
 * matching resource method is found for that content type. This protects the
 * server from attempting to deserialize incompatible payloads.
 *
 * QUERY PARAM vs PATH SEGMENT for filtering:
 * Using @QueryParam for filtering (?type=CO2) is superior to path-based filtering
 * (/sensors/type/CO2) because:
 * - Query params are semantically optional — they filter an existing collection
 *   rather than identifying a distinct sub-resource.
 * - /sensors/type/CO2 implies "type" is a sub-resource of "sensors", which is
 *   architecturally misleading and conflicts with /sensors/{sensorId}.
 * - Query parameters are the standard convention for search/filter/sort across
 *   all major REST APIs (GitHub, Twitter, Google).
 * - Multiple filters compose naturally: ?type=CO2&status=ACTIVE
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     * Returns all sensors, optionally filtered by type.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = store.getSensors().values();

        List<Sensor> result;
        if (type != null && !type.trim().isEmpty()) {
            result = allSensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        } else {
            result = new ArrayList<>(allSensors);
        }

        return Response.ok(result).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a specific sensor by ID.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the referenced roomId exists.
     * Throws LinkedResourceNotFoundException (422) if roomId does not exist.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Sensor 'id' field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Sensor 'roomId' field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Validate that the referenced room exists
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        if (store.getSensors().containsKey(sensor.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 409);
            error.put("error", "Conflict");
            error.put("message", "A sensor with ID '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        // Default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);

        // Link sensor to the room
        room.getSensorIds().add(sensor.getId());

        // Initialise empty readings list for this sensor
        store.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sensor registered successfully.");
        response.put("sensor", sensor);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Removes a sensor and unlinks it from its room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Unlink from room
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        store.getSensors().remove(sensorId);
        store.getSensorReadings().remove(sensorId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sensor '" + sensorId + "' deleted successfully.");
        response.put("deletedSensorId", sensorId);

        return Response.ok(response).build();
    }

    /**
     * Sub-Resource Locator for /api/v1/sensors/{sensorId}/readings
     *
     * SUB-RESOURCE LOCATOR PATTERN BENEFITS:
     * Rather than defining every nested path in one giant SensorResource class,
     * we delegate to a dedicated SensorReadingResource. This provides:
     *
     * 1. Single Responsibility: Each class has one focused purpose. SensorResource
     *    manages sensors; SensorReadingResource manages reading history.
     *
     * 2. Manageable complexity: Large APIs with many sub-resources would become
     *    unmanageable in a single class (thousands of lines). Separate classes
     *    keep each file focused and readable.
     *
     * 3. Reusability: The sub-resource class can potentially be reused from
     *    multiple parent resource paths.
     *
     * 4. Testability: Each class can be unit tested in isolation.
     *
     * JAX-RS resolves the path dynamically at runtime — the locator method
     * returns an instance, and JAX-RS continues dispatching the request into
     * that instance's annotated methods.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
