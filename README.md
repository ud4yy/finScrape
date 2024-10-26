

# finScrape

A robust Spring Boot application that scrapes historical exchange rate data from Yahoo Finance and provides REST APIs to query the data.

- **Deployment:** The project is deployed on Render's free tier. APIs can also be interacted with through Swagger UI; see the **DEMO** section for details.

## Tech Stack
- Spring Boot
- PostgreSQL (via Supabase)
- Render (Deployment)
- GitHub
- Postman

## Database Design
<img src="https://drive.google.com/uc?id=1hx9nyB9AfAWkUPcBw3upmYLifn9kGIc1" alt="ER Diagram" style="max-width: 100%; height: auto;">

The database is designed with scalability and performance in mind:
- **CurrencyPair**: Central entity storing currency pairs (e.g., USD-INR)
- **DailyExchangeRate**: Stores day-wise exchange rates
- **WeeklyExchangeRate**: Aggregated weekly data with optimized indexes
- **MonthlyExchangeRate**: Aggregated monthly data with optimized indexes

This multi-table approach allows for:
- Efficient querying based on different time periods
- Reduced data redundancy
- Optimized storage of aggregated data
- Fast retrieval through **indexed** time-based queries

## Key Features

### 1. Data Scraping
The application scrapes forex data from Yahoo Finance using their historical data API. The scraping is implemented with robust error handling and rate limiting to ensure reliable data collection.

### 2. Scheduled Synchronization
The application maintains data synchronization through three scheduled scraping jobs for AED-INR and GBP-INR pairs:
- **Daily**: Updates daily exchange rates at 00:05
- **Weekly**: Aggregates and updates weekly data every Monday at 00:15  
- **Monthly**: Aggregates and updates monthly data on the 1st of every month at 00:30 

Each scheduled job includes a retry mechanism with exponential backoff (3 attempts, starting with a 5-second delay) to ensure reliable data collection. You can check the implementation details [here](https://github.com/ud4yy/finScrape/blob/main/backend/src/main/java/com/vance/backend/config/ForexSchedulerConfig.java).

### 3. REST API Response
The API returns comprehensive forex data, including:
- **Aggregate Statistics**:
  - Maximum Price
  - Minimum Price
  - Average Price

  - Daily exchange rates
  - Weekly aggregated data
  - Monthly aggregated data

### 4. Monthly Report Generation
Generates detailed PDF reports containing:
- Daily exchange rate data for the previous month
- Tabulated format for easy readability
- Specific to requested currency pairs

## Demo

### Swagger Documentation
The project includes Swagger UI for API documentation and testing:
```
https://finscrape-supa.onrender.com/swagger-ui/index.html#/scrape-controller/getForexData
```

### Steps to Use

1. **Initialize Database (Optional)**
   ```
   https://finscrape-supa.onrender.com/api/populate?fromCurrency=AED&toCurrency=INR&startDate=2023-01-01&endDate=2024-10-26
   ```
   The `populate` endpoint allows for initial data population if needed. Since this deployment uses Supabase as a persistent PostgreSQL database, data remains intact across server restarts, eliminating the need to reinitialize each time the server restarts. Scheduled scraping will automatically keep the data updated.

2. **Query Historical Data**
   ```
   https://finscrape-supa.onrender.com/api/forex-data?from=USD&to=INR&period=3M
   ```
   Returns exchange rate data with aggregate statistics and time series data for the specified period.

3. **Generate Monthly PDF Reports**
   ```
   https://finscrape-supa.onrender.com/api/forex-pdf?fromCurrency=AED&toCurrency=INR
   ```
   Generates a PDF report containing monthly exchange rate analysis.

## Sample Data
Sample PDFs and other resources are available in this [Google Drive folder](https://drive.google.com/drive/folders/1ATk01J0cNIAE8fjzwS5ms4TmENvhWlEQ?usp=sharing)
