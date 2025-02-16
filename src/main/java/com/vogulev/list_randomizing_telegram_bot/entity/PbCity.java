package com.vogulev.list_randomizing_telegram_bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pb_cities")
public class PbCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_rus", unique = true)
    private String nameRus;

    @Column(name = "name_eng", unique = true, nullable = false)
    private String nameEng;
}
