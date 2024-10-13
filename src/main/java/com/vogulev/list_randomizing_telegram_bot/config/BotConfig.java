package com.vogulev.list_randomizing_telegram_bot.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Slf4j
@Getter
@Configuration
@EnableScheduling
public class BotConfig {
    private final String botUserName;
    private final String botToken;

    public BotConfig() {
        Map<String, String> config = new Yaml().load(getClass().getClassLoader().getResourceAsStream("botConfig.yaml"));
        this.botUserName = config.get("botUserName");
        this.botToken = config.get("botToken");
    }
}
