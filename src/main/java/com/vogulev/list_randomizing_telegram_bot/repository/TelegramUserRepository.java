package com.vogulev.list_randomizing_telegram_bot.repository;

import com.vogulev.list_randomizing_telegram_bot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    List<TelegramUser> findAllByActiveTrue();

    Optional<TelegramUser> getTelegramUserByTelegramId(Long chatId);

    Optional<TelegramUser> getTelegramUserByName(String name);

}
