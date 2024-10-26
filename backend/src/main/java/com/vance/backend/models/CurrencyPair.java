package com.vance.backend.models;


import lombok.*;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "currency_pair")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class CurrencyPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @OneToMany(mappedBy = "currencyPair", cascade = CascadeType.ALL)
    private Set<ExchangeRate> exchangeRates = new HashSet<>();
    
    public CurrencyPair(String fromCurrency, String toCurrency) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }
}
