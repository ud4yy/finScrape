package com.vance.backend.services.scrappers.impl;

import com.vance.backend.models.*;
import com.vance.backend.repos.*;
import com.vance.backend.services.scrappers.interfaces.DataPopulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataPopulationService implements DataPopulation {

    private final YahooFinanceScraperService scraperService;

    public void populateHistoricalData() {
        List<String[]> currencyPairs = Arrays.asList(
            new String[]{"GBP", "INR"},
            new String[]{"AED", "INR"}
        );

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        for (String[] pair : currencyPairs) {
            populateDataForPair(pair[0], pair[1], startDate, endDate);
        }
    }

    private void populateDataForPair(String fromCurrency, String toCurrency, LocalDate startDate, LocalDate endDate) {
        long fromTimestamp = YahooFinanceScraperService.dateToUnixTimestamp(startDate);
        long toTimestamp = YahooFinanceScraperService.dateToUnixTimestamp(endDate);
        scraperService.scrapeDailyData(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
        scraperService.scrapeWeeklyData(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
        scraperService.scrapeMonthlyData(fromCurrency, toCurrency, fromTimestamp, toTimestamp);
    }
}