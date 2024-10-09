package com.vogulev.list_randomizing_telegram_bot.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

@Slf4j
@Service
public class HolidaysService {
    public static final String URL = "https://my-calend.ru/holidays";
    public static final String USER_AGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
    public static final String CLASS_ATTR_KEY = "class";
    public static final String HOLIDAYS_ITEMS = "holidays-items";

    @SneakyThrows(IOException.class)
    public String getHolidays() {
        return Jsoup.connect(URL)
                .userAgent(USER_AGENT)
                .timeout(30000)
                .get().body()
                .getElementsByAttributeValue(CLASS_ATTR_KEY, HOLIDAYS_ITEMS)
                .getFirst()
                .select(":is(a, span)").stream()
                .map(Element::text)
                .filter(element -> isNotBlank(element) && !isCreatable(element))
                .map(element -> "-  " + element)
                .collect(Collectors.joining("\n"));
    }
}
