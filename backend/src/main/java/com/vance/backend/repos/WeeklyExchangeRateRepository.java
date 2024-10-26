package com.vance.backend.repos;

import com.vance.backend.models.WeeklyExchangeRate;
import com.vance.backend.models.CurrencyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WeeklyExchangeRateRepository extends JpaRepository<WeeklyExchangeRate, Long> {
    List<WeeklyExchangeRate> findByCurrencyPairAndWeekStartBetween(CurrencyPair currencyPair, LocalDate start, LocalDate end);
}
