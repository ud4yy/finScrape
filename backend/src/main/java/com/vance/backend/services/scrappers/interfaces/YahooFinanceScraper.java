package com.vance.backend.services.scrappers.interfaces;


import com.vance.backend.models.ExchangeRate;
import com.vance.backend.models.MonthlyExchangeRate;
import com.vance.backend.models.WeeklyExchangeRate;

import java.util.List;

public interface YahooFinanceScraper {

    List<ExchangeRate> scrapeDailyData(String fromCurrency, String toCurrency, long fromDate, long toDate);

    List<WeeklyExchangeRate> scrapeWeeklyData(String fromCurrency, String toCurrency, long fromDate, long toDate);

    List<MonthlyExchangeRate> scrapeMonthlyData(String fromCurrency, String toCurrency, long fromDate, long toDate);
}
