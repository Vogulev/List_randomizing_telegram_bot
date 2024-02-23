package com.vogulev.list_randomizing_telegram_bot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardService {

    public ReplyKeyboardMarkup getKeyboard() {
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

        KeyboardRow row2 = new KeyboardRow();
        KeyboardButton keyboardButton3 = new KeyboardButton();
        keyboardButton3.setText("Добавить коллегу ✅");
        KeyboardButton keyboardButton4 = new KeyboardButton();
        keyboardButton4.setText("Удалить коллегу ❌");
        row2.add(keyboardButton3);
        row2.add(keyboardButton4);

        KeyboardRow row3 = new KeyboardRow();
        KeyboardButton keyboardButton5 = new KeyboardButton();
        keyboardButton5.setText("Список \uD83D\uDCDC");
        KeyboardButton keyboardButton6 = new KeyboardButton();
        keyboardButton6.setText("Перемешать \uD83D\uDD00");
        row3.add(keyboardButton5);
        row3.add(keyboardButton6);

        KeyboardRow row4 = new KeyboardRow();
        KeyboardButton keyboardButton7 = new KeyboardButton();
        keyboardButton7.setText("Праздники \uD83C\uDF89");
        KeyboardButton keyboardButton8 = new KeyboardButton();
        keyboardButton8.setText("ДР \uD83C\uDF81");
        row4.add(keyboardButton7);
        row4.add(keyboardButton8);

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }
}
