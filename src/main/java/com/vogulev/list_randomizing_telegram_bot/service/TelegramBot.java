package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.config.BotConfig;
import com.vogulev.list_randomizing_telegram_bot.repository.NamesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final CommandService commandService;
    private final KeyboardService keyboardService;
    private final HolidaysService holidaysService;
    private final ClientService clientService;
    private final ShuffleService shuffleService;
    private final NamesRepository namesRepository;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    @Transactional
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() &&
                update.getMessage().getChat().getType().equals("private")) {
            var message = update.getMessage();
            var messageText = formatMessage(message.getText());
            long chatId = message.getChatId();
            var firstName = message.getChat().getFirstName();

            String answer = switch (messageText) {
                case "/start", "Старт/подписаться на ЛС \uD83D\uDE80" ->
                        commandService.startCmdReceived(chatId, firstName);
                case "/unsubscribe", "Отписаться от ЛС \uD83D\uDD15" ->
                        commandService.unsubscribeCmdReceived(chatId, firstName);
                case "/add", "Добавить коллегу ✅" -> commandService.addCmdReceived(update);
                case "/delete", "Удалить коллегу ❌" -> commandService.delCmdReceived(update);
                case "/shuffle", "Перемешать \uD83D\uDD00" -> commandService.shuffleCmdReceived();
                case "/list", "Список \uD83D\uDCDC" -> commandService.listCmdReceived();
                case "/holidays", "Праздники \uD83C\uDF89" -> "t.me/p_r_a_z_d_n_i_k";
                case "/birthdays", "ДР \uD83C\uDF81" -> "Раздел \"Дни рождения\"" +
                        " находится в процессе разработки: дайте разработчику немного больше времени :-)";
                default -> commandService.unknownCmdReceived(update, messageText);
            };
            sendMessage(chatId, answer, true);
        }
    }

    @Schedules({
            @Scheduled(cron = "0 45 10 * * 3", zone = "Europe/Moscow"),
            @Scheduled(cron = "0 45 9 * * 1,2,4,5", zone = "Europe/Moscow")
    })
    protected void scheduledShuffle() {
        if (holidaysService.todayIsNotPublicHoliday()) {
            var users = namesRepository.findAll();
            var namesStr = shuffleService.shuffle(users);
            var activePbClients = clientService.getAllActive();
            activePbClients.forEach(pbClient -> sendMessage(pbClient.getChatId(), namesStr, false));
        }
    }

    private void sendMessage(Long chatId, String text, boolean withReplyMarkup) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        if (withReplyMarkup) {
            sendMessage.setReplyMarkup(keyboardService.getKeyboard());
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private String formatMessage(String message) {
        if (message.startsWith("/") && message.contains("@")) {
            int subStringIndex = message.indexOf("@");
            return message.substring(0, subStringIndex);
        }
        return message;
    }
}
