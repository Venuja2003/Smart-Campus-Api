package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter that logs every incoming request and outgoing response.
 *
 * WHY USE FILTERS INSTEAD OF MANUAL LOGGING?
 * Using a cross-cutting filter for logging is far superior to manually inserting
 * Logger.info() into every resource method because:
 *
 * 1. Separation of Concerns: Business logic in resource classes stays clean and focused.
 *    Logging is an infrastructure concern, not a business one.
 *
 * 2. DRY Principle: A single filter covers every endpoint automatically. Adding a new
 *    resource class immediately gets logging for free — no code changes needed.
 *
 * 3. Consistency: All log entries follow the same format. Manual logging is prone
 *    to inconsistency (different developers, different formats, forgotten calls).
 *
 * 4. Maintainability: Changing the log format or log level requires modifying one
 *    file, not dozens of resource classes.
 *
 * 5. Reliability: It is impossible to accidentally forget to log a new endpoint.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Logs every incoming HTTP request: method and URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(String.format("[REQUEST]  --> %s %s", method, uri));
    }

    /**
     * Logs every outgoing HTTP response: status code.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();
        LOGGER.info(String.format("[RESPONSE] <-- %d | %s %s", status, method, uri));
    }
}
