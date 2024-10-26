package com.vance.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vance.backend.services.scrappers.interfaces.DataPopulation; // Import the interface

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class ScrapeController {

    private static final Logger logger = LoggerFactory.getLogger(ScrapeController.class);

    @Autowired
    private DataPopulation populationService; // Use the interface type

    @GetMapping("/populate")
    public ResponseEntity<String> populateHistoricalData() {
        try {
            populationService.populateHistoricalData();
            logger.info("Historical data population initiated successfully.");
            return ResponseEntity.ok("Historical data has been populated successfully.");
        } catch (Exception e) {
            logger.error("Error occurred while populating historical data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to populate historical data: " + e.getMessage());
        }
    }
}
