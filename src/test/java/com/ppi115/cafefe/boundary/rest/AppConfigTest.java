package com.ppi115.cafefe.boundary.rest;

import jakarta.ws.rs.ApplicationPath;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {
    @Test
    void rutaBase() {
        assertEquals("resources/v1", appconfig.class.getAnnotation(ApplicationPath.class).value());
    }
}
