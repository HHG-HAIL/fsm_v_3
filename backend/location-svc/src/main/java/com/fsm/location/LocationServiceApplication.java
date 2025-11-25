package com.fsm.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Location Service Application - Tracks technician positions for the FSM system.
 * 
 * This service is part of the Location Services bounded context and provides
 * real-time tracking of technician locations for dispatching and map display.
 */
@SpringBootApplication
public class LocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}
