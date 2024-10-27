package com.vance.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vance.backend.dto.ForexDataResponse;
import com.vance.backend.services.ForexPdfService;
import com.vance.backend.services.ForexService;
import com.vance.backend.services.scrappers.interfaces.DataPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class ScrapeController {

    private static final Logger logger = LoggerFactory.getLogger(ScrapeController.class);

    @Autowired
    private DataPopulation populationService;
    @Autowired
    private ForexService forexService;
    @Autowired
    private ForexPdfService forexPdfService;

    @PostMapping("/populate")
    public ResponseEntity<String> populateHistoricalData(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            populationService.populateHistoricalData(fromCurrency, toCurrency, start, end);
            logger.info("Historical data population initiated successfully for pair: {} to {}", fromCurrency, toCurrency);
            return ResponseEntity.ok("Historical data has been populated successfully for: " + fromCurrency + " to " + toCurrency);
        } catch (Exception e) {
            logger.error("Error occurred while populating historical data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to populate historical data: " + e.getMessage());
        }
    }

    @GetMapping("/forex-data")
    public ResponseEntity<ForexDataResponse> getForexData(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String period) {
        try {
            ForexDataResponse response = forexService.getForexData(from, to, period);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error fetching forex data: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/forex-pdf")
    public ResponseEntity<byte[]> downloadForexPdf(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        try {
            byte[] pdfBytes = forexPdfService.generateForexReport(fromCurrency, toCurrency);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", 
                        "attachment; filename=\"" + fromCurrency + "_" + toCurrency + "_report.pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error generating PDF report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
