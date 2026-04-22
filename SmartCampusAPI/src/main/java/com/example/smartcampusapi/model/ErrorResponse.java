/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.model;

/**
 *
 * @author amnaa
 */
public class ErrorResponse {
    private int status;
    private String error;
    private String message;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
    // Standard Getters
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
