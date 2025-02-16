package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.integration.WeatherApiClient;
import com.vogulev.list_randomizing_telegram_bot.repository.CitiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherApiClient weatherApiClient;
    private final CitiesRepository citiesRepository;

    public String getWeather() {
        var result = new StringBuilder();
        citiesRepository.findAll().forEach(city -> {
            try {
                var currentWeather = weatherApiClient.getCurrentWeather(city);
                result.append(currentWeather);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        return result.toString().stripTrailing();
    }
}
