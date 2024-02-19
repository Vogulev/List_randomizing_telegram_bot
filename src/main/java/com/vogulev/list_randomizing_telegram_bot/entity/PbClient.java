package com.vogulev.list_randomizing_telegram_bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pb_client")
public class PbClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "chat_id")
    private Long chatId;

    private Boolean active;
}