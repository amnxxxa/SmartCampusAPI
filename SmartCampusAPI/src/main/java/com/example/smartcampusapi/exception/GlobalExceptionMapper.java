/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author amnaa
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        Map<String, Object> errorDetails = new HashMap<>();
        
        // This creates a clean JSON error instead of a messy HTML 404/500 page
        errorDetails.put("status", "error");
        errorDetails.put("message", exception.getMessage());
        errorDetails.put("type", exception.getClass().getSimpleName());

        // Default to 500 Internal Server Error unless we specify otherwise
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorDetails)
                .build();
    }
}
