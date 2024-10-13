package com.vogulev.list_randomizing_telegram_bot.exception;

import com.vogulev.list_randomizing_telegram_bot.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionHandler {
    private final TelegramBot telegramBot;

    @AfterThrowing(
            pointcut = "execution(* com.vogulev.list_randomizing_telegram_bot.service.CommandService.unknownCmdReceived(..)))",
            throwing = "ex"
    )
    protected void handleNoUserFoundException(JoinPoint joinPoint, NoUserFoundException ex) {
        log.warn(ex.getMessage());
        Object[] signatureArgs = joinPoint.getArgs();
        Update update = (Update) signatureArgs[0];
        telegramBot.sendMessage(update.getMessage().getChatId(), ex.getMessage());
    }
}
