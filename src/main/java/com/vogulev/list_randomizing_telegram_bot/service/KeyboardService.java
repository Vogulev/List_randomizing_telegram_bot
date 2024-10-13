package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.entity.TelegramUser;
import com.vogulev.list_randomizing_telegram_bot.exception.NoUserFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeyboardService {
    private final TelegramUserService telegramUserService;

    public ReplyKeyboardMarkup getKeyboard(Long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText("Старт/подписаться на ЛС \uD83D\uDE80");
        KeyboardButton keyboardButton2 = new KeyboardButton();
        keyboardButton2.setText("Отписаться от ЛС \uD83D\uDD15");
        row1.add(keyboardButton1);
        row1.add(keyboardButton2);
        keyboardRows.add(row1);

        TelegramUser user = telegramUserService.getByTelegramId(userId)
                .orElseThrow(() -> new NoUserFoundException("Пользователь с id " + userId + " не найден!"));
        if (user.isAdmin() || user.isSuperuser()) {
            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton keyboardButton3 = new KeyboardButton();
            keyboardButton3.setText("Добавить коллегу ✅");
            KeyboardButton keyboardButton4 = new KeyboardButton();
            keyboardButton4.setText("Удалить коллегу ❌");
            row2.add(keyboardButton3);
            row2.add(keyboardButton4);
            keyboardRows.add(row2);
        }

        KeyboardRow row3 = new KeyboardRow();
        KeyboardButton keyboardButton5 = new KeyboardButton();
        keyboardButton5.setText("Список \uD83D\uDCDC");
        KeyboardButton keyboardButton6 = new KeyboardButton();
        keyboardButton6.setText("Перемешать \uD83D\uDD00");
        row3.add(keyboardButton5);
        row3.add(keyboardButton6);
        keyboardRows.add(row3);

        KeyboardRow row4 = new KeyboardRow();
        KeyboardButton keyboardButton7 = new KeyboardButton();
        keyboardButton7.setText("Праздники \uD83C\uDF89");
        KeyboardButton keyboardButton8 = new KeyboardButton();
        keyboardButton8.setText("ДР \uD83C\uDF81");
        row4.add(keyboardButton7);
        row4.add(keyboardButton8);
        keyboardRows.add(row4);

        if (user.isSuperuser()) {
            KeyboardRow row5 = new KeyboardRow();
            KeyboardButton keyboardButton9 = new KeyboardButton();
            keyboardButton9.setText("Назначить админа \uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB");
            KeyboardButton keyboardButton10 = new KeyboardButton();
            keyboardButton10.setText("Удалить админа \uD83E\uDDD1\uD83C\uDFFB\u200D\uD83D\uDD27");
            row5.add(keyboardButton9);
            row5.add(keyboardButton10);
            keyboardRows.add(row5);
        }

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
