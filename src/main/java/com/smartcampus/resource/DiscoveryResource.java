package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();
        response.put("apiName", "Smart Campus Sensor & Room Management API");
        response.put("version", "v1");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("contact", "admin@smartcampus.ac.uk");
        response.put("status", "operational");

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensorReadingsExample", "/api/v1/sensors/{sensorId}/readings");
        response.put("_links", links);

        return Response.ok(response).build();
    }
}