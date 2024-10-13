package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.entity.PbUser;
import com.vogulev.list_randomizing_telegram_bot.entity.TelegramUser;
import com.vogulev.list_randomizing_telegram_bot.exception.NoUserFoundException;
import com.vogulev.list_randomizing_telegram_bot.repository.NamesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {
    private final NamesRepository namesRepository;
    private final ShuffleService shuffleService;
    private final TelegramUserService telegramUserService;

    private boolean isSaveUserCmd;
    private boolean isDeleteCmd;
    private boolean isAddAdminCmd;
    private boolean isDeleteAdminCmd;

    protected String startCmdReceived(User user, Long chatId) {
        var pbClientOpt = telegramUserService.getByTelegramId(user.getId());
        if (pbClientOpt.isEmpty()) {
            telegramUserService.save(user, chatId);
            return "Привет, " + user.getFirstName() + "!\n" +
                    "Это бот для выбора порядка выступления на Daily, созданный инициативным парнем ;-)\n" +
                    "Он присылает список в 9:45 по МСК каждый день за исключением выходных!\n" +
                    "Кстати, вы уже автоматически подписаны на утреннюю рассылку!\n" +
                    "Желаю хорошего и продуктивного дня! :-*";
        }
        var pbClient = pbClientOpt.get();
        if (pbClient.isActive()) {
            return "Вы и так уже подписаны на ежедневную рассылку в ЛС";
        }
        pbClient.setActive(true);
        telegramUserService.save(pbClient);
        return "Вы снова подписались на ежедневную рассылку в ЛС";
    }

    protected String listCmdReceived() {
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

    protected String shuffleCmdReceived() {
        var users = namesRepository.findAll();
        return shuffleService.shuffle(users);
    }

    protected String delCmdReceived(User telegramUser) {
        var user = telegramUserService.getByTelegramId(telegramUser.getId());
        if (user.isPresent() && isAdmin(user.get())) {
            isDeleteCmd = true;
            return "Введите имя сотрудника для удаления его из списка дейли";
        }
        return "Вы не являетесь администратором бота";
    }

    protected String addCmdReceived(User telegramUser) {
        var user = telegramUserService.getByTelegramId(telegramUser.getId());
        if (user.isPresent() && isAdmin(user.get())) {
            isSaveUserCmd = true;
            return "Введите имя сотрудника для добавления его в список дейли";
        }
        return "Вы не являетесь администратором бота";
    }

    private static Boolean isAdmin(TelegramUser user) {
        return user.isAdmin() || user.isSuperuser();
    }

    protected String adminCmdReceived(User telegramUser, boolean isAddAdminCmd) {
        var user = telegramUserService.getByTelegramId(telegramUser.getId());
        if (user.isPresent() && user.get().isSuperuser()) {
            if (isAddAdminCmd) {
                this.isAddAdminCmd = true;
            } else {
                this.isDeleteAdminCmd = true;
            }
            return "Введите имя сотрудника для повышения его до админа";
        }
        return "Вы не являетесь администратором бота";
    }

    protected String unsubscribeCmdReceived(long userId) {
        try {
            var telegramUser = telegramUserService.getByTelegramId(userId)
                    .orElseThrow(() -> new TelegramApiRequestException("Не удалось получить пользователя"));
            var firstName = telegramUser.getName();
            if (telegramUser.isActive()) {
                telegramUser.setActive(false);
                telegramUserService.save(telegramUser);
                return firstName + " вы успешно отписались от назойливой рассылки по утрам, " +
                        "теперь сообщение можно увидеть только в группе PB!";
            }
            return firstName + " вы и так уже отписаны от получения сообщений в ЛС!";
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException(e);
        }
    }

    public String unknownCmdReceived(Update update, String messageText) {
        String answer;
        if (isExpectedCommand(update, isSaveUserCmd)) {
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
        } else if (isExpectedCommand(update, isDeleteCmd)) {
            isDeleteCmd = false;
            var isDeleted = namesRepository.deletePbUserByName(messageText);
            if (isDeleted == 1) {
                answer = "Вы успешно удалили сотрудника " + messageText + " из дейли списка";
            } else {
                answer = "Сотрудник с именем " + messageText + " не найден!";
            }
        } else if (isExpectedCommand(update, isAddAdminCmd)) {
            isAddAdminCmd = false;
            TelegramUser user = telegramUserService.getByName(messageText)
                    .orElseThrow(() -> new NoUserFoundException("Пользователь " + messageText + " не найден!"));
            telegramUserService.markAdmin(user);
            answer = "Вы успешно возвысили сотрудника " + messageText + " до администратора бота";
        } else if (isExpectedCommand(update, isDeleteAdminCmd)) {
            isDeleteAdminCmd = false;
            TelegramUser user = telegramUserService.getByName(messageText)
                    .orElseThrow(() -> new NoUserFoundException("Пользователь " + messageText + " не найден! " +
                            "Возможно он еще не познакомился с ботом? Попросите его нажать кнопку \"старт\" в ЛС бота"));
            telegramUserService.unmarkAdmin(user);
            answer = "Вы успешно унизили администратора " + messageText + " до обычного смертного";
        } else {
            answer = "Не знаю такой команды, попробуйте выбрать нужное действие";
        }
        return answer;
    }

    private boolean isExpectedCommand(Update update, boolean isCommand) {
        return isCommand && update.hasMessage() && update.getMessage().hasText();
    }
}