package com.vogulev.list_randomizing_telegram_bot.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Getter
@Configuration
@EnableScheduling
public class BotConfig {
    private final String botUserName;
    private final String botToken;
    private final List<String> admins;

    public BotConfig() {
        Dotenv dotenv = Dotenv.load();
        this.botUserName = dotenv.get("BOT_USERNAME");
        this.botToken = dotenv.get("BOT_TOKEN");
        this.admins = Arrays.stream(dotenv.get("ADMINS").split(",")).toList();
    }
}
