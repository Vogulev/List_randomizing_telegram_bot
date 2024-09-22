package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.config.BotConfig;
import com.vogulev.list_randomizing_telegram_bot.entity.PbClient;
import com.vogulev.list_randomizing_telegram_bot.entity.PbUser;
import com.vogulev.list_randomizing_telegram_bot.repository.NamesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {
    private final NamesRepository namesRepository;
    private final ShuffleService shuffleService;
    private final ClientService clientService;
    private final BotConfig botConfig;

    private boolean isSaveUserCmd = false;
    private boolean isDeleteCmd = false;

    protected String startCmdReceived(Long chatId, String name) {
        var pbClientOpt = clientService.get(chatId);
        if (pbClientOpt.isEmpty()) {
            clientService.save(chatId, name);
            return "Привет, " + name + "!\n" +
                    "Это бот для выбора порядка выступления на Daily, созданный инициативным парнем ;-)\n" +
                    "Он присылает список в 9:45 по МСК каждый день за исключением выходных!\n" +
                    "Кстати, вы уже автоматически подписаны на утреннюю рассылку!\n" +
                    "Желаю хорошего и продуктивного дня! :-*";
        }
        var pbClient = pbClientOpt.get();
        if (pbClient.getActive()) {
            return "Вы и так уже подписаны на ежедневную рассылку в ЛС";
        }
        pbClient.setActive(true);
        clientService.save(pbClient);
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

    protected String delCmdReceived(Update update) {
        if (botConfig.getAdmins().contains(update.getMessage().getFrom().getUserName())) {
            isDeleteCmd = true;
            return "Введите имя сотрудника для удаления его из списка";
        }
        return "Вы не являетесь администратором бота";
    }

    protected String addCmdReceived(Update update) {
        if (botConfig.getAdmins().contains(update.getMessage().getFrom().getUserName())) {
            isSaveUserCmd = true;
            return "Введите имя сотрудника для добавления его в список";
        }
        return "Вы не являетесь администратором бота";
    }

    protected String unsubscribeCmdReceived(long chatId, String firstName) {
        PbClient pbClient;
        try {
            pbClient = clientService.get(chatId)
                    .orElseThrow(() -> new TelegramApiRequestException("Не удалось получить пользователя"));
        } catch (TelegramApiRequestException e) {
            throw new RuntimeException(e);
        }

        if (pbClient.getActive()) {
            pbClient.setActive(false);
            clientService.save(pbClient);
            return firstName + " вы успешно отписались от назойливой рассылки по утрам, " +
                    "теперь сообщение можно увидеть только в группе PB!";
        }
        return firstName + " вы и так уже отписаны от получения сообщений в ЛС!";
    }


    protected String unknownCmdReceived(Update update, String messageText) {
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
}