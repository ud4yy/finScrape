package com.vance.backend.repos;


import org.springframework.data.jpa.repository.JpaRepository;

import com.vance.backend.models.CurrencyPair;

public interface CurrencyPairRepository extends JpaRepository<CurrencyPair, Long> {
    CurrencyPair findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
}
