/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.controller;

import com.example.smartcampusapi.exception.LinkedResourceNotFoundException;
import com.example.smartcampusapi.model.Room;
import com.example.smartcampusapi.model.Sensor;
import com.example.smartcampusapi.model.SensorReading;
import com.example.smartcampusapi.repository.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 *
 * @author amnaa
 */

@Path("/sensors")
public class SensorResource {

    // GET /api/v1/sensors - List all sensors, or filter by ?type=CO2
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return DataStore.sensors.values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(DataStore.sensors.values());
    }

    // POST /api/v1/sensors - Register a new sensor, validates roomId exists
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        Room room = DataStore.rooms.get(sensor.getRoomId());

        // Part 5.2: Throw LinkedResourceNotFoundException instead of inline response
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + "' does not exist. Sensor cannot be registered."
            );
        }

        DataStore.sensors.put(sensor.getId(), sensor);

        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // PUT /api/v1/sensors/{sensorId} - Update sensor value and status
    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedData) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }

        sensor.setCurrentValue(updatedData.getCurrentValue());
        sensor.setStatus(updatedData.getStatus());

        // record a reading when value is updated via PUT
        SensorReading reading = new SensorReading(sensorId, updatedData.getCurrentValue());
        DataStore.readings.add(reading);

        return Response.ok(sensor).build();
    }

    // NOT defining @GET directly here
    // GET/POST /api/v1/sensors/{sensorId}/readings(Part 4.1)
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
