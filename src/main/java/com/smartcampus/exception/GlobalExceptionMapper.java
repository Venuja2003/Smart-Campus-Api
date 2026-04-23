package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global catch-all exception mapper for any unhandled Throwable.
 *
 * SECURITY NOTE: Exposing raw Java stack traces to API consumers is dangerous because:
 * 1. Stack traces reveal internal class names, package structure, and framework versions,
 *    allowing attackers to identify known vulnerabilities in those specific versions.
 * 2. They expose file paths and line numbers, giving attackers a roadmap to target
 *    specific code locations.
 * 3. They may leak sensitive data from method parameters that appear in exception messages.
 * 4. Framework-specific traces reveal the exact server/container technology stack,
 *    narrowing down exploit options for the attacker.
 *
 * This mapper ensures only a safe, generic message is returned to the client
 * while the full error is logged securely on the server side.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log the full stack trace on the server side for diagnostics
        LOGGER.log(Level.SEVERE, "Unhandled exception occurred: " + ex.getMessage(), ex);

        // Return a safe, generic response to the client - never expose stack traces
        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
