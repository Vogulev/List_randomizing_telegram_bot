package com.vogulev.list_randomizing_telegram_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class ListRandomizingTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListRandomizingTelegramBotApplication.class, args);
    }

}
