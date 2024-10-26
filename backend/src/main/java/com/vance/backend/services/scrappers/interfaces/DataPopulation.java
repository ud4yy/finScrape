package com.vance.backend.services.scrappers.interfaces;

import java.time.LocalDate;

public interface DataPopulation {
    void populateHistoricalData(String fromCurrency, String toCurrency, LocalDate startDate, LocalDate endDate);
}
