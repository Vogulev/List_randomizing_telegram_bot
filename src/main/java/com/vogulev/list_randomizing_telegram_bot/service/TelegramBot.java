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
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

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
    private final KeyboardService keyboardService;
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

            String answer = switch (messageText) {
                case "/start", "Старт/подписаться на ЛС \uD83D\uDE80" -> startCmdReceived(chatId, firstName);
                case "/unsubscribe", "Отписаться от ЛС \uD83D\uDD15" -> unsubscribeCmdReceived(chatId, firstName);
                case "/add", "Добавить коллегу ✅" -> addCmdReceived(update);
                case "/delete", "Удалить коллегу ❌" -> delCmdReceived(update);
                case "/shuffle", "Перемешать \uD83D\uDD00" -> shuffleCmdReceived();
                case "/list", "Список \uD83D\uDCDC" -> listCmdReceived();
                case "/holidays", "Праздники \uD83C\uDF89" -> "t.me/p_r_a_z_d_n_i_k";
                case "/birthdays", "ДР \uD83C\uDF81" -> "Раздел \"Дни рождения\"" +
                        " находится в процессе разработки: дайте разработчику немного больше времени :-)";
                default -> unknownCmdReceived(update, messageText);
            };
            sendMessage(chatId, answer);
        }
    }

    private String unknownCmdReceived(Update update, String messageText) {
        String answer;
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
            answer = "Не знаю такой команды, попробуйте выбрать нужное действие";
        }
        return answer;
    }

    private String listCmdReceived() {
        var allUsers = namesRepository.findAll();
        if (allUsers.isEmpty()) {
            return "Нет добавленных сотрудников, если вы администратор - воспользуйся командой \"/add\"\n\n";
        }
        var allUsersStr = allUsers.stream()
                .map(PbUser::getName)
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        return "Список всех сотрудников:\n\n" + allUsersStr;
    }

    private String shuffleCmdReceived() {
        var users = namesRepository.findAll();
        return shuffleService.shuffleNames(users);
    }

    private String delCmdReceived(Update update) {
        if (admins.contains(update.getMessage().getFrom().getUserName())) {
            isDeleteCmd = true;
            return "Введите имя сотрудника для удаления его из списка";
        }
        return "Вы не являетесь администратором бота";
    }

    private String addCmdReceived(Update update) {
        if (admins.contains(update.getMessage().getFrom().getUserName())) {
            isSaveUserCmd = true;
            return "Введите имя сотрудника для добавления его в список";
        }
        return "Вы не являетесь администратором бота";
    }

    private String unsubscribeCmdReceived(long chatId, String firstName) {
        PbClient pbClient;
        try {
            pbClient = clientsRepository.getPbClientsByChatId(chatId)
                    .orElseThrow(() -> new TelegramApiRequestException("Не удалось получить пользователя"));
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException(e);
        }

        if (pbClient.getActive()) {
            pbClient.setActive(false);
            clientsRepository.save(pbClient);
            return firstName + " вы успешно отписались от назойливой рассылки по утрам, " +
                    "теперь сообщение можно увидеть только в группе PB!";
        }
        return firstName + " вы и так уже отписаны от получения сообщений в ЛС!";
    }

    @Schedules({
            @Scheduled(cron = "0 45 10 * * 3", zone = "Europe/Moscow"),
            @Scheduled(cron = "0 45 9 * * 1,2,4,5", zone = "Europe/Moscow")
    })
    protected void scheduledShuffle() {
        if (holidaysService.todayIsNotPublicHoliday()) {
            var users = namesRepository.findAll();
            var namesStr = shuffleService.shuffleNames(users);
            var activePbClients = clientsRepository.findAllByActiveTrue();
            activePbClients.forEach(pbClient -> sendMessage(pbClient.getChatId(), namesStr));
        }
    }

    private String startCmdReceived(Long chatId, String name) {
        var pbClientOpt = clientsRepository.getPbClientsByChatId(chatId);
        if (pbClientOpt.isPresent()) {
            var pbClient = pbClientOpt.get();
            if (pbClient.getActive()) {
                return "Вы и так уже подписаны на ежедневную рассылку в ЛС";
            } else {
                pbClient.setActive(true);
                clientsRepository.save(pbClient);
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
        sendMessage.setReplyMarkup(keyboardService.getKeyboard());
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
