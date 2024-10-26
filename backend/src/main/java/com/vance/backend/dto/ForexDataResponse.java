package com.vance.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForexDataResponse {
    private String fromCurrency;
    private String toCurrency;
    private String period;
    private AggregateStatistics aggregates;
    private ForexTimeSeriesData timeSeriesData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForexDataPoint {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Double openPrice;
        private Double highPrice;
        private Double lowPrice;
        private Double closePrice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AggregateStatistics {
        private Double maximumPrice;
        private Double minimumPrice;
        private Double averagePrice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForexTimeSeriesData {
        private List<ForexDataPoint> dailyData;
        private List<ForexDataPoint> weeklyData;
        private List<ForexDataPoint> monthlyData;
    }
}