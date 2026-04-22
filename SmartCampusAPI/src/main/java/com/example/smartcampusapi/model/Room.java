/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.model;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author amnaa
 */
public class Room {
    private String id; // Unique identifier 
    private String name; // Human-readable name 
    private int capacity; // Maximum occupancy 
    private List<String> sensorIds = new ArrayList<>(); // Collection of IDs 

    public Room() {} // No-arg constructor for JSON conversion

    // Getters and Setters 
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}

    

