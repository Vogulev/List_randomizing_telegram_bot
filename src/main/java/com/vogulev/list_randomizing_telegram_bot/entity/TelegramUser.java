package com.vogulev.list_randomizing_telegram_bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "telegram_user")
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "telegram_id")
    private Long telegramId;

    private boolean active;

    @Column(name = "is_admin")
    private boolean admin;

    @Column(name = "is_superuser")
    private boolean superuser;
}