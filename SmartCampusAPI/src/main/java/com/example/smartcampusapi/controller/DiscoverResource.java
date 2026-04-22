/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author amnaa
 */
@Path("/")
public class DiscoverResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo() {
        Map<String, Object> response = new HashMap<>();

        // Essential API metadata
        response.put("version", "1.0.0");
        response.put("description", "Smart Campus Sensor & Room Management API");
        response.put("admin_contact", "admin@campus.com");

        // Map of primary resource collections (HATEOAS)
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        response.put("resources", links);

        return Response.ok(response).build();
    }

    @GET
    @Path("/error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerError() {
        // This simulates a severe server crash to test the GlobalExceptionMapper
        // (Section 5.2)
        throw new RuntimeException("Simulated Database Connection Failure!");
    }
}
