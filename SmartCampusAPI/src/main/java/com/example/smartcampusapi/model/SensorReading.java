/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.model;

import java.util.UUID;
/**
 *
 * @author amnaa
 */

public class SensorReading {
    private String id;
    private String sensorId;
    private double value;
    private long timestamp; // Epoch ms 

    public SensorReading() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(String sensorId, double value) {
        this();
        this.sensorId = sensorId;
        this.value = value;
    }

    public String getId() { return id; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public long getTimestamp() { return timestamp; }
}
