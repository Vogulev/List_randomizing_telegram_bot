package com.vogulev.list_randomizing_telegram_bot.exception;

import lombok.Getter;

@Getter
public class NoUserFoundException extends RuntimeException {
    private final String message;

    public NoUserFoundException(String message) {
        super(message);
        this.message = message;
    }
}
