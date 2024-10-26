package com.vance.backend.services.scrappers.impl;

import com.vance.backend.models.*;
import com.vance.backend.repos.*;
import com.vance.backend.services.scrappers.interfaces.DataPopulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DataPopulationService implements DataPopulation {

    private final YahooFinanceScraperService scraperService;

    @Override
    public void populateHistoricalData(String fromCurrency, String toCurrency, LocalDate startDate, LocalDate endDate) {
        long fromTimestamp = YahooFinanceScraperService.dateToUnixTimestamp(startDate);
        long toTimestamp = YahooFinanceScraperService.dateToUnixTimestamp(endDate);

        populateDataForPair(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
    }

    private void populateDataForPair(String fromCurrency, String toCurrency, long fromTimestamp, long toTimestamp) {
        scraperService.scrapeDailyData(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
        scraperService.scrapeWeeklyData(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
        scraperService.scrapeMonthlyData(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
    }
}
