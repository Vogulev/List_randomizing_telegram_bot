package com.vogulev.list_randomizing_telegram_bot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vogulev.list_randomizing_telegram_bot.model.Holiday;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class HolidaysService {
    @Value("${holidays.url}")
    private String targetUrl;

    public List<Holiday> getHolidays() {
        var currentYear = String.valueOf(LocalDate.now().getYear());
        var requestUrl = targetUrl + "PublicHolidays/" + currentYear + "/RU";
        try {
            var url = URI.create(requestUrl).toURL();
            var listType = new TypeReference<List<Holiday>>() {
            };
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper.readValue(url, listType);
        } catch (IOException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean todayIsNotPublicHoliday() {
        var requestUrl = targetUrl + "IsTodayPublicHoliday/RU?offset=3";
        int responseCode;
        try {
            var url = URI.create(requestUrl).toURL();
            var con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            responseCode = con.getResponseCode();
            con.disconnect();
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return responseCode == 204;
    }
}
