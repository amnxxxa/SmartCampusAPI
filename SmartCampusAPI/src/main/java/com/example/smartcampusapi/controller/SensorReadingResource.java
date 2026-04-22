/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.controller;

import com.example.smartcampusapi.exception.SensorUnavailableException;
import com.example.smartcampusapi.model.Sensor;
import com.example.smartcampusapi.model.SensorReading;
import com.example.smartcampusapi.repository.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;
/**
 *
 * @author amnaa
 */

public class SensorReadingResource {

    private final String sensorId;

    // Constructor receives the sensorId from the parent SensorResource locator
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings - Fetch full reading history
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }

        List<SensorReading> history = DataStore.readings.stream()
                .filter(r -> r.getSensorId().equals(sensorId))
                .collect(Collectors.toList());

        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings - Add new reading
    // Also triggers update of parent Sensor's currentValue (Part 4.2 side effect)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }

        // Part 5.3: Throw SensorUnavailableException if sensor is in MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor " + sensorId + " is currently under MAINTENANCE and cannot accept new readings."
            );
        }

        // Link this reading to the sensor
        reading.setSensorId(sensorId);
        DataStore.readings.add(reading);

        // Part 4.2 Side Effect: update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}