package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.config.BotConfig;
import com.vogulev.list_randomizing_telegram_bot.entity.PbClient;
import com.vogulev.list_randomizing_telegram_bot.entity.PbUser;
import com.vogulev.list_randomizing_telegram_bot.repository.ClientsRepository;
import com.vogulev.list_randomizing_telegram_bot.repository.NamesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
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
    private final HolidaysService holidaysService;
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
            var messageText = formatMessage(update.getMessage().getText());
            long chatId = update.getMessage().getChatId();
            var firstName = update.getMessage().getChat().getFirstName();
            String answer;

            switch (messageText) {
                case "/start":
                    answer = startCommandReceived(chatId, firstName);
                    break;
                case "/unsubscribe":
                    clientsRepository.getPbClientsByChatId(chatId)
                            .ifPresent(client -> client.setActive(false));
                    answer = firstName + " вы успешно отписались от назойливой рассылки по утрам, хорошего отдыха!";
                    break;
                case "/add":
                    if (admins.contains(update.getMessage().getFrom().getUserName())) {
                        answer = "Введите имя сотрудника для добавления его в список";
                        isSaveUserCmd = true;
                    } else {
                        answer = "Вы не являетесь администратором бота";
                    }
                    break;
                case "/delete":
                    if (admins.contains(update.getMessage().getFrom().getUserName())) {
                        answer = "Введите имя сотрудника для удаления его из списка";
                        isDeleteCmd = true;
                    } else {
                        answer = "Вы не являетесь администратором бота";
                    }
                    break;
                case "/shuffle":
                    var users = namesRepository.findAll();
                    answer = shuffleService.shuffleNames(users);
                    break;
                case "/list":
                    var allUsers = namesRepository.findAll();
                    if (allUsers.isEmpty()) {
                        answer = "Нет добавленных сотрудников, если вы администратор - воспользуйся командой \"/add\"\n\n";
                    } else {
                        var allUsersStr = allUsers.stream()
                                .map(PbUser::getName)
                                .map(Object::toString)
                                .collect(Collectors.joining("\n"));
                        answer = "Список всех сотрудников:\n\n" + allUsersStr;
                    }
                    break;
                case "/holidays":
                    answer = "Какой сегодня праздник можно узнать в группе t.me/p_r_a_z_d_n_i_k";
                    break;
                case "/birthdays":
                    answer = "Раздел \"Дни рождения\" находится в процессе разработки: дайте разработчику немного больше времени :-)";
                    break;
                default:
                    if (isSaveUserCmd && update.hasMessage() && update.getMessage().hasText()) {
                        try {
                            var pbUser = new PbUser();
                            pbUser.setName(messageText);
                            namesRepository.save(pbUser);
                            answer = "Вы добавили сотрудника " + messageText;
                        } catch (DataIntegrityViolationException ex) {
                            log.error(ex.getMessage());
                            answer = "Ошибка сохранения сотрудника: возможно такое имя уже присутствует в списке";
                        } finally {
                            isSaveUserCmd = false;
                        }
                    } else if (isDeleteCmd && update.hasMessage() && update.getMessage().hasText()) {
                        var isDeleted = namesRepository.deletePbUserByName(messageText);
                        if (isDeleted == 1) {
                            answer = "Вы успешно удалили сотрудника " + messageText;
                        } else {
                            answer = "Сотрудник с именем " + messageText + " не найден!";
                        }
                        isDeleteCmd = false;
                    } else {
                        answer = "Не знаю такой команды, попробуйте еще раз";
                    }
            }
            sendMessage(chatId, answer);
        }

    }

    @Schedules({
            @Scheduled(cron = "0 45 10 * * 3", zone = "Europe/Moscow"),
            @Scheduled(cron = "0 45 9 * * 1,2,4,5", zone = "Europe/Moscow")
    })
    protected void scheduledShuffle() {
        if (holidaysService.todayIsNotPublicHoliday()) {
            var users = namesRepository.findAll();
            var namesStr = shuffleService.shuffleNames(users);
            List<PbClient> activePbClients = clientsRepository.findAllByActiveTrue();
            activePbClients.forEach(pbClient -> sendMessage(pbClient.getChatId(), namesStr));
        }
    }

    private String startCommandReceived(Long chatId, String name) {
        var pbClientOpt = clientsRepository.getPbClientsByChatId(chatId);
        if (pbClientOpt.isPresent()) {
            PbClient pbClient = pbClientOpt.get();
            if (pbClient.getActive()) {
                return "Вы и так уже подписаны на ежедневную рассылку в ЛС";
            } else {
                pbClient.setActive(true);
                return "Вы снова подписались на ежедневную рассылку в ЛС";
            }
        }
        savePbClient(chatId, name);
        return "Привет, " + name + "!\n" +
                "Это бот для выбора порядка выступления на Daily, созданный инициативным парнем ;-)\n" +
                "Он присылает список в 9:45 по МСК каждый день за исключением выходных!\n" +
                "Кстати, вы уже автоматически подписаны на утреннюю рассылку!\n" +
                "Желаю хорошего и продуктивного дня! :-*";
    }

    private void savePbClient(Long chatId, String name) {
        var pbClient = new PbClient();
        pbClient.setName(name);
        pbClient.setChatId(chatId);
        pbClient.setActive(true);
        clientsRepository.save(pbClient);
    }

    private void sendMessage(Long chatId, String textToSend) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
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
