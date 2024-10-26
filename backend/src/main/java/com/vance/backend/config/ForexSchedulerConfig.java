package com.vance.backend.config;

import com.vance.backend.services.scrappers.interfaces.YahooFinanceScraper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Configuration
@EnableScheduling
@EnableRetry
@RequiredArgsConstructor
public class ForexSchedulerConfig {

    @Autowired
    private final YahooFinanceScraper yahooFinanceScraper;

    private static final String[] FROM_CURRENCIES = {"GBP", "AED"};
    private static final String TO_CURRENCY = "INR";

    /* 
        testing the scheduler
        @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata") 
        public void testEveryMinute() {
            log.info("Running every minute at {}", LocalDateTime.now());
        }
    */

    @Scheduled(cron = "0 5 0 * * *")
    public void scheduleDailyScraping() {
        log.info("Starting daily forex data scraping at {}", LocalDateTime.now());
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(1);
        
        for (String fromCurrency : FROM_CURRENCIES) {
            scrapeDailyDataWithRetry(
                fromCurrency, 
                TO_CURRENCY,
                dateToUnixTimestamp(startDate),
                dateToUnixTimestamp(endDate)
            );
        }
    }
    
    @Scheduled(cron = "0 15 0 * * MON")
    public void scheduleWeeklyScraping() {
        log.info("Starting weekly forex data scraping at {}", LocalDateTime.now());
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(2); 
        
        for (String fromCurrency : FROM_CURRENCIES) {
            scrapeWeeklyDataWithRetry(
                fromCurrency,
                TO_CURRENCY,
                dateToUnixTimestamp(startDate),
                dateToUnixTimestamp(endDate)
            );
        }
    }
    
    // Run monthly on 1st day at 00:30 AM
    @Scheduled(cron = "0 30 0 1 * *")
    public void scheduleMonthlyScraping() {
        log.info("Starting monthly forex data scraping at {}", LocalDateTime.now());
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(2); 
        
        for (String fromCurrency : FROM_CURRENCIES) {
            scrapeMonthlyDataWithRetry(
                fromCurrency,
                TO_CURRENCY,
                dateToUnixTimestamp(startDate),
                dateToUnixTimestamp(endDate)
            );
        }
    }
    
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    private void scrapeDailyDataWithRetry(String fromCurrency, String toCurrency, long fromDate, long toDate) {
        try {
            log.info("Scraping daily data for {}-{}", fromCurrency, toCurrency);
            yahooFinanceScraper.scrapeDailyData(fromCurrency, toCurrency, fromDate, toDate);
            log.info("Successfully scraped daily data for {}-{}", fromCurrency, toCurrency);
        } catch (Exception e) {
            log.error("Error scraping daily data for {}-{}: {}", fromCurrency, toCurrency, e.getMessage());
            throw e; // Rethrow for retry mechanism
        }
    }
    
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    private void scrapeWeeklyDataWithRetry(String fromCurrency, String toCurrency, long fromDate, long toDate) {
        try {
            log.info("Scraping weekly data for {}-{}", fromCurrency, toCurrency);
            yahooFinanceScraper.scrapeWeeklyData(fromCurrency, toCurrency, fromDate, toDate);
            log.info("Successfully scraped weekly data for {}-{}", fromCurrency, toCurrency);
        } catch (Exception e) {
            log.error("Error scraping weekly data for {}-{}: {}", fromCurrency, toCurrency, e.getMessage());
            throw e; // Rethrow for retry mechanism
        }
    }
    
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    private void scrapeMonthlyDataWithRetry(String fromCurrency, String toCurrency, long fromDate, long toDate) {
        try {
            log.info("Scraping monthly data for {}-{}", fromCurrency, toCurrency);
            yahooFinanceScraper.scrapeMonthlyData(fromCurrency, toCurrency, fromDate, toDate);
            log.info("Successfully scraped monthly data for {}-{}", fromCurrency, toCurrency);
        } catch (Exception e) {
            log.error("Error scraping monthly data for {}-{}: {}", fromCurrency, toCurrency, e.getMessage());
            throw e; 
        }
    }
      
    private static long dateToUnixTimestamp(LocalDate date) {
        return date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
    }
}