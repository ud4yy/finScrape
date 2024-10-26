package com.vance.backend.models;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "monthly_exchange_rate", indexes = {
    @Index(name = "idx_month_start_currency_pair", columnList = "month_start, currency_pair_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "currency_pair_id", nullable = false)
    private CurrencyPair currencyPair;

    @Column(name = "month_start", nullable = false)
    private LocalDate monthStart;

    @Column(name = "open_price")
    private Double openPrice;

    @Column(name = "high_price")
    private Double highPrice;

    @Column(name = "low_price")
    private Double lowPrice;

    @Column(name = "close_price")
    private Double closePrice;

}
