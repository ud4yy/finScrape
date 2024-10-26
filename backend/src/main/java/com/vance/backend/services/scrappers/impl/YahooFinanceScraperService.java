package com.vance.backend.services.scrappers.impl;
import com.vance.backend.services.scrappers.interfaces.YahooFinanceScraper;
import com.vance.backend.models.*;
import com.vance.backend.repos.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YahooFinanceScraperService implements YahooFinanceScraper {

    private static final String BASE_URL = "https://finance.yahoo.com/quote/%s/history/?period1=%d&period2=%d&frequency=%s";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private final CurrencyPairRepository currencyPairRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final WeeklyExchangeRateRepository weeklyExchangeRateRepository;
    private final MonthlyExchangeRateRepository monthlyExchangeRateRepository;

    public enum Frequency {
        DAILY("1d"),
        WEEKLY("1wk"),
        MONTHLY("1mo");

        private final String value;

        Frequency(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private CurrencyPair getCurrencyPair(String fromCurrency, String toCurrency) {
        CurrencyPair currencyPair = currencyPairRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);
        if (currencyPair == null) {
            currencyPair = currencyPairRepository.save(new CurrencyPair(fromCurrency, toCurrency));
        }
        return currencyPair;
    }

    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date: " + dateStr, e);
        }
    }
    
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public static long dateToUnixTimestamp(LocalDate date) {
        return date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
    }

    @Override
    public List<ExchangeRate> scrapeDailyData(String fromCurrency, String toCurrency, long fromDate, long toDate) {
        return scrapeForexData(fromCurrency, toCurrency, fromDate, toDate, Frequency.DAILY);
    }

    @Override
    public List<WeeklyExchangeRate> scrapeWeeklyData(String fromCurrency, String toCurrency, long fromDate, long toDate) {
        String quote = fromCurrency + toCurrency + "%3DX";
        String url = String.format(BASE_URL, quote, fromDate, toDate, Frequency.WEEKLY.getValue());
        List<WeeklyExchangeRate> weeklyRates = new ArrayList<>();

        try {
            CurrencyPair currencyPair = getCurrencyPair(fromCurrency, toCurrency);
            Document doc = fetchDocument(url);

            Elements rows = doc.select("div.table-container.yf-h2urb6 table tbody tr");
            for (Element row : rows) {
                try {
                    Elements cells = row.select("td");
                    if (cells.size() >= 7) {
                        WeeklyExchangeRate rate = new WeeklyExchangeRate();
                        rate.setWeekStart(parseDate(cells.get(0).text()));
                        rate.setOpenPrice(parseDouble(cells.get(1).text()));
                        rate.setHighPrice(parseDouble(cells.get(2).text()));
                        rate.setLowPrice(parseDouble(cells.get(3).text()));
                        rate.setClosePrice(parseDouble(cells.get(4).text()));
                        rate.setCurrencyPair(currencyPair);

                        weeklyRates.add(rate);
                        weeklyExchangeRateRepository.save(rate);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing weekly row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scrape weekly forex data: " + e.getMessage(), e);
        }

        return weeklyRates;
    }

    @Override
    public List<MonthlyExchangeRate> scrapeMonthlyData(String fromCurrency, String toCurrency, long fromDate, long toDate) {
        String quote = fromCurrency + toCurrency + "%3DX";
        String url = String.format(BASE_URL, quote, fromDate, toDate, Frequency.MONTHLY.getValue());
        List<MonthlyExchangeRate> monthlyRates = new ArrayList<>();

        try {
            CurrencyPair currencyPair = getCurrencyPair(fromCurrency, toCurrency);
            Document doc = fetchDocument(url);

            Elements rows = doc.select("div.table-container.yf-h2urb6 table tbody tr");
            for (Element row : rows) {
                try {
                    Elements cells = row.select("td");
                    if (cells.size() >= 7) {
                        MonthlyExchangeRate rate = new MonthlyExchangeRate();
                        rate.setMonthStart(parseDate(cells.get(0).text()));
                        rate.setOpenPrice(parseDouble(cells.get(1).text()));
                        rate.setHighPrice(parseDouble(cells.get(2).text()));
                        rate.setLowPrice(parseDouble(cells.get(3).text()));
                        rate.setClosePrice(parseDouble(cells.get(4).text()));
                        rate.setCurrencyPair(currencyPair);

                        monthlyRates.add(rate);
                        monthlyExchangeRateRepository.save(rate);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing monthly row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scrape monthly forex data: " + e.getMessage(), e);
        }

        return monthlyRates;
    }

    private List<ExchangeRate> scrapeForexData(String fromCurrency, String toCurrency, long fromDate, long toDate, Frequency frequency) {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        String quote = fromCurrency + toCurrency + "%3DX";
        String url = String.format(BASE_URL, quote, fromDate, toDate, frequency.getValue());

        try {
            CurrencyPair currencyPair = getCurrencyPair(fromCurrency, toCurrency);
            Document doc = fetchDocument(url);

            Elements rows = doc.select("div.table-container.yf-h2urb6 table tbody tr");
            for (Element row : rows) {
                try {
                    Elements cells = row.select("td");
                    if (cells.size() >= 7) {
                        ExchangeRate exchangeRate = new ExchangeRate();
                        exchangeRate.setDate(parseDate(cells.get(0).text()));
                        exchangeRate.setOpenPrice(parseDouble(cells.get(1).text()));
                        exchangeRate.setHighPrice(parseDouble(cells.get(2).text()));
                        exchangeRate.setLowPrice(parseDouble(cells.get(3).text()));
                        exchangeRate.setClosePrice(parseDouble(cells.get(4).text()));
                        exchangeRate.setCurrencyPair(currencyPair);

                        exchangeRates.add(exchangeRate);
                        exchangeRateRepository.save(exchangeRate);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scrape forex data: " + e.getMessage(), e);
        }

        return exchangeRates;
    }
}
