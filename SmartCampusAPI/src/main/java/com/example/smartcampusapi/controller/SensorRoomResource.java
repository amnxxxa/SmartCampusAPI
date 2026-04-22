/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.controller;

import com.example.smartcampusapi.exception.RoomNotEmptyException;
import com.example.smartcampusapi.model.Room;
import com.example.smartcampusapi.repository.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author amnaa
 */

@Path("/rooms")
public class SensorRoomResource {

    // GET /api/v1/rooms - List all rooms
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return new ArrayList<>(DataStore.rooms.values());
    }

    // POST /api/v1/rooms - Create a new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required\"}")
                    .build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} - Get a specific room by ID
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} - Decommission a room
    // Part 5.1: Throws RoomNotEmptyException if sensors are still assigned
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\"}")
                    .build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted. It still has " +
                room.getSensorIds().size() + " active sensor(s) assigned to it."
            );
        }

        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}