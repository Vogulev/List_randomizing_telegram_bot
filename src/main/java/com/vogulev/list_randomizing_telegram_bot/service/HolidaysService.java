package com.vogulev.list_randomizing_telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HolidaysService {
    public static final String URL = "https://kakoysegodnyaprazdnik.ru";
    public static final String CLASS_ATTR_KEY = "class";
    public static final String MAIN_ATTR_VALUE = "main";
    public static final String ITEMPROP_ATTR_KEY = "itemprop";

    public String getHolidays() {
        return getHolidaysList().stream().limit(10)
                .map(Object::toString)
                .collect(Collectors.joining("\n- "));
    }

    private ArrayList<String> getHolidaysList() {
        var list = new ArrayList<String>();
        try {
            var doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-encoding", "gzip, deflate, br, zstd")
                    .header("accept-language", "en-RU,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6")
                    .header("cache-control", "max-age=0")
                    .header("sec-ch-ua", "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .header("priority", "u=0, i")
                    .timeout(30000)
                    .get();
            var body = doc.body();
            var mainEntity = body.getElementsByAttributeValue(ITEMPROP_ATTR_KEY, "mainEntity").getFirst();
            var listing = mainEntity.getElementsByAttributeValue(CLASS_ATTR_KEY, "listing").getFirst();
            var listingWr = listing.getElementsByAttributeValue(CLASS_ATTR_KEY, "listing_wr").getFirst();

            var acceptedTitle =
                    getHolidayTitle(listingWr.getElementsByAttributeValue(ITEMPROP_ATTR_KEY, "acceptedAnswer").getFirst());
            list.add("- " + acceptedTitle);

            listingWr.getElementsByAttributeValue(ITEMPROP_ATTR_KEY, "suggestedAnswer").forEach(element -> {
                var suggestedTitle = getHolidayTitle(element);
                list.add(suggestedTitle);
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return list;
    }

    private String getHolidayTitle(Element element) {
        try {
            var suggestedAnswerMain = element.getElementsByAttributeValue(CLASS_ATTR_KEY, MAIN_ATTR_VALUE).getFirst();
            var suggestedFirst = suggestedAnswerMain.getElementsByAttributeValue(CLASS_ATTR_KEY, MAIN_ATTR_VALUE).getFirst();
            var suggestedSecond = suggestedFirst.getElementsByAttributeValue(ITEMPROP_ATTR_KEY, "text").getFirst();
            return String.valueOf(suggestedSecond.childNode(0));
        } catch (RuntimeException ex) {
            log.warn(element.id() + " was skipped due to parser error");
            log.warn(ex.getMessage());
        }
        return "";
    }
}
