package com.example.smartcampusapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configures Jakarta RESTful Web Services for the application.
 */
@ApplicationPath("/api/v1") 
public class JakartaRestConfiguration extends Application {
    // we leave this empty. The server will automatically scan for your Resource classes.
}
