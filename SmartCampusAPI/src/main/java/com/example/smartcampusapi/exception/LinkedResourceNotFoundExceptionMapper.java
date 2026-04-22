package com.example.smartcampusapi.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception mapper to handle LinkedResourceNotFoundException.
 * Maps it to HTTP 422 Unprocessable Entity.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 422); // Unprocessable Entity
        error.put("error", "Unprocessable Entity");
        error.put("message", exception.getMessage());

        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
