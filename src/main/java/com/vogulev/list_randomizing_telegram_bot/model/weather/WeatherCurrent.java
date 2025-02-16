package com.vogulev.list_randomizing_telegram_bot.model.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherCurrent implements Serializable {
    private Double temp_c;
    private Double wind_mph;
    private CurrentCondition condition;
}
