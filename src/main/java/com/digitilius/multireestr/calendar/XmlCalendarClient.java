package com.digitilius.multireestr.calendar;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class XmlCalendarClient {

    static final String URL_TEMPLATE = "https://xmlcalendar.ru/data/ru/%d/calendar.xml";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public byte[] download(int year) {
        String url = URL_TEMPLATE.formatted(year);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "multi-reestr-helper-calendar")
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("xmlcalendar.ru вернул HTTP " + response.statusCode()
                        + " для " + url);
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Загрузка календаря прервана: " + url, e);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось скачать календарь: " + url, e);
        }
    }
}
