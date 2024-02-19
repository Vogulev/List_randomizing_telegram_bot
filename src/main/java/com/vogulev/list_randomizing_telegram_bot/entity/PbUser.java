package com.vogulev.list_randomizing_telegram_bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pb_user")
public class PbUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
