/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampusapi.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
 
import java.io.IOException;
import java.util.logging.Logger;
/**
 *
 * @author amnaa
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
 
    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());
 
    // Runs before every request reaches a resource method
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
            "[REQUEST]  Method: %s | URI: %s",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri().toString()
        ));
    }
 
    // Runs after every response is built
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
            "[RESPONSE] Method: %s | URI: %s | Status: %d",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri().toString(),
            responseContext.getStatus()
        ));
    }
}
