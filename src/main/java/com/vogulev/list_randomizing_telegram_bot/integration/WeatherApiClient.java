package com.vogulev.list_randomizing_telegram_bot.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vogulev.list_randomizing_telegram_bot.config.BotConfig;
import com.vogulev.list_randomizing_telegram_bot.model.weather.WeatherRsBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Service
public class WeatherApiClient {
    @Value("${weather.url}")
    private String targetUrl;
    private BotConfig botConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherRsBody getCurrentWeather(String city) throws IOException {
        var apiToken = botConfig.getWeatherApiToken();
        var uri = URI.create(targetUrl + "current.json?key=" + apiToken + "&aqi=no&lang=ru&q=" + city);
        var weatherRsBody = objectMapper.readValue(uri.toURL(), WeatherRsBody.class);
        return weatherRsBody;
    }
}
