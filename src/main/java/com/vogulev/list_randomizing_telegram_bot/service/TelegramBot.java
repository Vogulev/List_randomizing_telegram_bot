package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.config.BotConfig;
import com.vogulev.list_randomizing_telegram_bot.model.PbClient;
import com.vogulev.list_randomizing_telegram_bot.model.PbUser;
import com.vogulev.list_randomizing_telegram_bot.repository.ClientsRepository;
import com.vogulev.list_randomizing_telegram_bot.repository.NamesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final ShuffleService shuffleService;
    private final NamesRepository namesRepository;
    private final ClientsRepository clientsRepository;
    private boolean isSaveUserCmd = false;
    private boolean isDeleteCmd = false;
    @Value("#{'${bot.admins}'.split(',')}")
    private List<String> admins;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String answer;

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/add":
                    if (admins.contains(update.getMessage().getFrom().getUserName())) {
                        answer = "Введите имя сотрудника для добавления его в список";
                        sendMessage(chatId, answer);
                        isSaveUserCmd = true;
                    } else {
                        answer = "Вы не являетесь администратором бота";
                        sendMessage(chatId, answer);
                    }
                    break;
                case "/delete":
                    if (admins.contains(update.getMessage().getFrom().getUserName())) {
                        answer = "Введите имя сотрудника для удаления его из списка";
                        sendMessage(chatId, answer);
                        isDeleteCmd = true;
                    } else {
                        answer = "Вы не являетесь администратором бота";
                        sendMessage(chatId, answer);
                    }
                    break;
                case "/shuffle":
                    List<PbUser> users = namesRepository.findAll();
                    String namesStr = shuffleService.shuffleNames(users);
                    sendMessage(chatId, namesStr);
                    break;
                case "/list":
                    List<PbUser> allUsers = namesRepository.findAll();
                    if (allUsers.isEmpty()) {
                        sendMessage(chatId,
                                "Нет добавленных сотрудников, если вы администратор - воспользуйся командой \"/add\"\n\n");
                    } else {
                        sendMessage(chatId, "Список всех сотрудников:\n\n");
                        String allUsersStr = allUsers.stream()
                                .map(PbUser::getName)
                                .map(Object::toString)
                                .collect(Collectors.joining("\n"));
                        sendMessage(chatId, allUsersStr);
                    }
                    break;
                case "/holidays":
                    sendMessage(chatId,
                            "Раздел \"Праздники\" находится в процессе разработки: дайте разработчику немного больше времени :-)");
                    break;
                case "/birthdays":
                    sendMessage(chatId,
                            "Раздел \"Дни рождения\" находится в процессе разработки: дайте разработчику немного больше времени :-)");
                    break;
                default:
                    if (isSaveUserCmd && update.hasMessage() && update.getMessage().hasText()) {
                        try {
                            PbUser pbUser = new PbUser();
                            pbUser.setName(messageText);
                            namesRepository.save(pbUser);
                            answer = "Вы добавили сотрудника " + messageText;
                            sendMessage(chatId, answer);
                        } catch (DataIntegrityViolationException ex) {
                            log.error(ex.getMessage());
                            sendMessage(chatId, "Ошибка сохранения сотрудника: возможно такое имя уже присутствует в списке");
                        } finally {
                            isSaveUserCmd = false;
                        }
                    } else if (isDeleteCmd && update.hasMessage() && update.getMessage().hasText()) {
                        Integer isDeleted = namesRepository.deletePbUserByName(messageText);
                        if (isDeleted == 1) {
                            answer = "Вы успешно удалили сотрудника " + messageText;
                        } else {
                            answer = "Сотрудник с именем " + messageText + " не найден!";
                        }
                        sendMessage(chatId, answer);
                        isDeleteCmd = false;
                    } else {
                        answer = "Не знаю такой команды, попробуйте еще раз";
                        sendMessage(chatId, answer);
                    }
            }
        }

    }

    @Scheduled(cron = "45 9 * * 1-5")
    private void scheduledShuffle() {
        List<PbUser> users = namesRepository.findAll();
        String namesStr = shuffleService.shuffleNames(users);
        clientsRepository.findAll().forEach(pbClient -> sendMessage(pbClient.getChatId(), namesStr));
    }

    private void startCommandReceived(Long chatId, String name) {
        PbClient pbClient = new PbClient();
        pbClient.setName(name);
        pbClient.setChatId(chatId);
        clientsRepository.save(pbClient);
        String answer = "Привет, " + name + "!\n" +
                "Это бот для выбора порядка выступления на Daily, созданный инициативным парнем ;-)\n" +
                "Он присылает список в 9:45 по МСК каждый день за исключением выходных!\n" +
                "Кстати, вы уже автоматически подписаны на утреннюю рассылку!\n" +
                "Желаю хорошего и продуктивного дня! :-*";
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
