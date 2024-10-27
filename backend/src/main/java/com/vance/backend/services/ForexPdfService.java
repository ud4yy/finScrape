package com.vance.backend.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vance.backend.models.CurrencyPair;
import com.vance.backend.models.ExchangeRate;
import com.vance.backend.repos.ExchangeRateRepository;
import com.vance.backend.repos.CurrencyPairRepository; 

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ForexPdfService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyPairRepository currencyPairRepository; 

    public List<ExchangeRate> getLast30DaysExchangeRates(CurrencyPair currencyPair) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return exchangeRateRepository.findByCurrencyPairAndDateBetween(currencyPair, startDate, endDate);
    }

    public byte[] generateForexReport(String fromCurrency, String toCurrency) throws Exception {
        CurrencyPair currencyPair = currencyPairRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);
        
        if (currencyPair == null) {
            currencyPair = new CurrencyPair(fromCurrency, toCurrency);
            currencyPairRepository.save(currencyPair);
        }

        List<ExchangeRate> exchangeRates = getLast30DaysExchangeRates(currencyPair);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            document.open();
            addMetaData(document);
            addTitlePage(document, fromCurrency, toCurrency);
            addContent(document, exchangeRates);
            document.close();
            
            return baos.toByteArray();
        }
    }

    private void addMetaData(Document document) {
        document.addTitle("Forex Data Report");
        document.addSubject("Forex Exchange Rate Data");
        document.addKeywords("Forex, Exchange Rate, Currency");
        document.addCreator("Vance Forex System");
    }

    private void addTitlePage(Document document, String fromCurrency, String toCurrency) throws DocumentException {
        Paragraph title = new Paragraph();
        title.setAlignment(Element.ALIGN_CENTER);

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        title.add(new Chunk("Forex Exchange Rate Report", titleFont));
        title.add(Chunk.NEWLINE);
        title.add(Chunk.NEWLINE);
        
        // Add subtitle
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 16);
        title.add(new Chunk(String.format("%s to %s", fromCurrency, toCurrency), subtitleFont));
        title.add(Chunk.NEWLINE);
        title.add(Chunk.NEWLINE);
        
        // Add date
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12);
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        title.add(new Chunk("Generated on: " + currentDate, dateFont));
        
        document.add(title);
        document.add(Chunk.NEWLINE);
    }

    private void addContent(Document document, List<ExchangeRate> exchangeRates) throws DocumentException {
        if (exchangeRates == null || exchangeRates.isEmpty()) {
            document.add(new Paragraph("No data available for the selected currencies."));
            return;
        }

        // Add table
        PdfPTable table = new PdfPTable(5); // Date, Open, High, Low, Close
        table.setWidthPercentage(100);
        
        // Add table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Stream.of("Date", "Open", "High", "Low", "Close")
            .forEach(columnTitle -> {
                PdfPCell header = new PdfPCell();
                header.setPhrase(new Phrase(columnTitle, headerFont));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setPadding(5);
                table.addCell(header);
            });

        // Add data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (ExchangeRate rate : exchangeRates) {
            table.addCell(new Phrase(rate.getDate().toString(), dataFont));
            table.addCell(new Phrase(String.format("%.4f", rate.getOpenPrice()), dataFont));
            table.addCell(new Phrase(String.format("%.4f", rate.getHighPrice()), dataFont));
            table.addCell(new Phrase(String.format("%.4f", rate.getLowPrice()), dataFont));
            table.addCell(new Phrase(String.format("%.4f", rate.getClosePrice()), dataFont));
        }
        
        document.add(table);
    }
}
