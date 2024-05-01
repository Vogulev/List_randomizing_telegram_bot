package com.vogulev.list_randomizing_telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Service
public class WorkingDaysInfoService {
    @Value("${holidays.url}")
    private String targetUrl;

    public boolean isWorkingDate(LocalDate date) {
        DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE;
        var requestUrl = targetUrl + dtf.format(date);
        RestTemplate request = new RestTemplate();
        String response = request.getForObject(requestUrl, String.class);
        return Objects.equals(response, "0");
    }
}
