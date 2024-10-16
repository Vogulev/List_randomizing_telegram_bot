package com.vogulev.list_randomizing_telegram_bot.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class IsDayOffClient {
    public static final String WORKING_DAY = "0";
    private final RestClient restClient = RestClient.create();
    @Value("${holidays.url}")
    private String targetUrl;

    public boolean isWorkingDate(LocalDate date) {
        var dateIso = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        var response = restClient
                .get()
                .uri(targetUrl, dateIso)
                .exchange((rq, rs) -> {
                    if (rs.getStatusCode().isError()) {
                        return WORKING_DAY;
                    } else {
                        return requireNonNull(rs.bodyTo(String.class));
                    }
                });
        return Objects.equals(response, WORKING_DAY);
    }
}
