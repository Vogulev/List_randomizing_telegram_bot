package com.vogulev.list_randomizing_telegram_bot.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Configuration
@EnableScheduling
public class BotConfig {
    private final String botUserName;
    private final String botToken;
    private final List<String> admins;

    public BotConfig() {
        try {
            Map<String, String> config = new Yaml().load(new FileReader("botConfig.yaml"));
            this.botUserName = config.get("botUserName");
            this.botToken = config.get("botToken");
            this.admins = Arrays.stream(config.get("admins").split(",")).toList();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
