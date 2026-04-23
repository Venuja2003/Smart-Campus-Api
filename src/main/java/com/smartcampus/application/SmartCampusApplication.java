package com.smartcampus.application;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application Entry Point.
 *
 * By default, JAX-RS resource classes follow a PER-REQUEST lifecycle:
 * a new instance is created for every incoming HTTP request and discarded afterward.
 * This means resource classes are NOT singletons by default.
 *
 * Because of this, shared mutable state (like our in-memory HashMaps) MUST NOT
 * be stored as instance fields in resource classes. Instead, we use a static
 * DataStore singleton to hold all data, ensuring it persists across requests
 * and is shared consistently. In a multi-threaded environment, these collections
 * should use ConcurrentHashMap to prevent race conditions.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception Mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Filters
        classes.add(LoggingFilter.class);

        // Jackson JSON support
        classes.add(JacksonFeature.class);

        return classes;
    }
}
