/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.repository;

import com.example.smartcampusapi.model.Room;
import com.example.smartcampusapi.model.Sensor;
import com.example.smartcampusapi.model.SensorReading;
import java.util.List;                                 
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;      
import java.util.Map;
/**
 *
 * @author amnaa
 */
public class DataStore {
   // Thread-safe maps to prevent race conditions
    public static Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static List<SensorReading> readings = new java.util.concurrent.CopyOnWriteArrayList<>();
}
