package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 *
 * HTTP 422 is more semantically accurate than 404 here because:
 * - The request URL itself is valid and was found (no 404 on the endpoint).
 * - The JSON payload was syntactically well-formed (no 400 Bad Request).
 * - The issue is a semantic one: a referenced foreign key (roomId) inside the
 *   payload points to a resource that does not exist. The server understood the
 *   request but cannot process it due to a logical data integrity violation.
 * - 422 signals to the client: "your data structure is fine, but the content is wrong."
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", ex.getMessage());
        error.put("resourceType", ex.getResourceType());
        error.put("resourceId", ex.getResourceId());
        error.put("hint", "Ensure the referenced resource exists before creating a dependent entity.");

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
