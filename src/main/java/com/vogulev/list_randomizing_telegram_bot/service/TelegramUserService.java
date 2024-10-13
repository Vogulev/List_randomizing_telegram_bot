package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.entity.TelegramUser;
import com.vogulev.list_randomizing_telegram_bot.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramUserService {
    private final TelegramUserRepository telegramUserRepository;

    public void save(User user, Long chatId) {
        var pbClient = new TelegramUser();
        pbClient.setName(user.getFirstName());
        pbClient.setSurname(user.getLastName());
        pbClient.setTelegramId(user.getId());
        pbClient.setChatId(chatId);
        pbClient.setActive(true);
        pbClient.setAdmin(false);
        pbClient.setSuperuser(false);
        telegramUserRepository.save(pbClient);
    }

    public void markAdmin(TelegramUser user) {
        user.setAdmin(true);
        telegramUserRepository.save(user);
    }

    public void unmarkAdmin(TelegramUser user) {
        user.setAdmin(false);
        telegramUserRepository.save(user);
    }

    public Optional<TelegramUser> getByName(String name) {
        return telegramUserRepository.getTelegramUserByName(name);
    }

    public void save(TelegramUser client) {
        telegramUserRepository.save(client);
    }

    public Optional<TelegramUser> getByTelegramId(Long id) {
        return telegramUserRepository.getTelegramUserByTelegramId(id);
    }

    public List<TelegramUser> getAllActive() {
        return telegramUserRepository.findAllByActiveTrue();
    }
}
