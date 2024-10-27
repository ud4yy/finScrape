package com.vance.backend.services;

import com.vance.backend.dto.ForexDataResponse;
import com.vance.backend.dto.ForexDataResponse.*;
import com.vance.backend.models.*;
import com.vance.backend.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

@Service
public class ForexService {
    
    @Autowired
    private CurrencyPairRepository currencyPairRepository;
    
    @Autowired
    private ExchangeRateRepository exchangeRateRepository;
    
    @Autowired
    private WeeklyExchangeRateRepository weeklyExchangeRateRepository;
    
    @Autowired
    private MonthlyExchangeRateRepository monthlyExchangeRateRepository;

    public ForexDataResponse getForexData(String fromCurrency, String toCurrency, String period) {
        validateInputs(fromCurrency, toCurrency, period);
        
        CurrencyPair currencyPair = currencyPairRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);
        if (currencyPair == null) {
            throw new IllegalArgumentException("Currency pair not found");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, period);
        
        List<ForexDataPoint> dailyData = fetchDailyData(currencyPair, startDate, endDate);
        List<ForexDataPoint> weeklyData = fetchWeeklyData(currencyPair, startDate, endDate);
        List<ForexDataPoint> monthlyData = fetchMonthlyData(currencyPair, startDate, endDate);
        
        AggregateStatistics aggregates = calculateAggregates(dailyData);
        
        return new ForexDataResponse(
            fromCurrency,
            toCurrency,
            period,
            aggregates,
            new ForexTimeSeriesData(dailyData, weeklyData, monthlyData)
        );
    }

    private List<ForexDataPoint> fetchDailyData(CurrencyPair currencyPair, LocalDate startDate, LocalDate endDate) {
        return exchangeRateRepository.findByCurrencyPairAndDateBetween(currencyPair, startDate, endDate)
            .stream()
            .map(this::mapToDataPoint)
            .collect(Collectors.toList());
    }

    private List<ForexDataPoint> fetchWeeklyData(CurrencyPair currencyPair, LocalDate startDate, LocalDate endDate) {
        return weeklyExchangeRateRepository.findByCurrencyPairAndWeekStartBetween(currencyPair, startDate, endDate)
            .stream()
            .map(this::mapToDataPoint)
            .collect(Collectors.toList());
    }

    private List<ForexDataPoint> fetchMonthlyData(CurrencyPair currencyPair, LocalDate startDate, LocalDate endDate) {
        return monthlyExchangeRateRepository.findByCurrencyPairAndMonthStartBetween(currencyPair, startDate, endDate)
            .stream()
            .map(this::mapToDataPoint)
            .collect(Collectors.toList());
    }

    private AggregateStatistics calculateAggregates(List<ForexDataPoint> data) {
        if (data.isEmpty()) {
            return new AggregateStatistics(0.0, 0.0, 0.0);
        }

        DoubleSummaryStatistics closeStats = data.stream()
            .mapToDouble(ForexDataPoint::getClosePrice)
            .summaryStatistics();
            
        return new AggregateStatistics(
            closeStats.getMax(),
            closeStats.getMin(),
            closeStats.getAverage()
        );
    }
    
    private void validateInputs(String fromCurrency, String toCurrency, String period) {
        if (fromCurrency == null || fromCurrency.length() != 3) {
            throw new IllegalArgumentException("Invalid from currency");
        }
        if (toCurrency == null || toCurrency.length() != 3) {
            throw new IllegalArgumentException("Invalid to currency");
        }
        if (!isValidPeriod(period)) {
            throw new IllegalArgumentException("Invalid period");
        }
    }
    
    private boolean isValidPeriod(String period) {
        return period != null && period.matches("^[1-9][0-9]*(D|W|M|Y)$");
    }
    
    private LocalDate calculateStartDate(LocalDate endDate, String period) {
        char unit = period.charAt(period.length() - 1);
        int amount = Integer.parseInt(period.substring(0, period.length() - 1));
        
        return switch (unit) {
            case 'D' -> endDate.minusDays(amount);
            case 'W' -> endDate.minusWeeks(amount);
            case 'M' -> endDate.minusMonths(amount);
            case 'Y' -> endDate.minusYears(amount);
            default -> throw new IllegalArgumentException("Invalid period unit");
        };
    }
    
    private List<ForexDataPoint> fetchDataPoints(CurrencyPair currencyPair, LocalDate startDate, LocalDate endDate, String period) {
        if (period.endsWith("D") || period.equals("1M")) {
            // Use daily data for periods up to 1 month
            return exchangeRateRepository.findByCurrencyPairAndDateBetween(currencyPair, startDate, endDate)
                .stream()
                .map(this::mapToDataPoint)
                .collect(Collectors.toList());
        } else if (period.endsWith("M") && Integer.parseInt(period.substring(0, period.length() - 1)) <= 6) {
            // Use weekly data for periods up to 6 months
            return weeklyExchangeRateRepository.findByCurrencyPairAndWeekStartBetween(currencyPair, startDate, endDate)
                .stream()
                .map(this::mapToDataPoint)
                .collect(Collectors.toList());
        } else {
            // Use monthly data for longer periods
            return monthlyExchangeRateRepository.findByCurrencyPairAndMonthStartBetween(currencyPair, startDate, endDate)
                .stream()
                .map(this::mapToDataPoint)
                .collect(Collectors.toList());
        }
    }
    
    private ForexDataPoint mapToDataPoint(ExchangeRate rate) {
        return new ForexDataPoint(
            rate.getDate(),
            rate.getOpenPrice(),
            rate.getHighPrice(),
            rate.getLowPrice(),
            rate.getClosePrice()
        );
    }
    
    private ForexDataPoint mapToDataPoint(WeeklyExchangeRate rate) {
        return new ForexDataPoint(
            rate.getWeekStart(),
            rate.getOpenPrice(),
            rate.getHighPrice(),
            rate.getLowPrice(),
            rate.getClosePrice()
        );
    }
    
    private ForexDataPoint mapToDataPoint(MonthlyExchangeRate rate) {
        return new ForexDataPoint(
            rate.getMonthStart(),
            rate.getOpenPrice(),
            rate.getHighPrice(),
            rate.getLowPrice(),
            rate.getClosePrice()
        );
    }
}
