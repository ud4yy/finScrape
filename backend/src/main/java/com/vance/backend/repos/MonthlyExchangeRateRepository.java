package com.vance.backend.repos;

import com.vance.backend.models.ExchangeRate;
import com.vance.backend.models.MonthlyExchangeRate;
import com.vance.backend.models.CurrencyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MonthlyExchangeRateRepository extends JpaRepository<MonthlyExchangeRate, Long> {
    List<MonthlyExchangeRate> findByCurrencyPairAndMonthStartBetween(
        CurrencyPair currencyPair, LocalDate startDate, LocalDate endDate
    );
}