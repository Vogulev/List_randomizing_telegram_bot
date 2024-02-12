package com.vogulev.list_randomizing_telegram_bot.repository;

import com.vogulev.list_randomizing_telegram_bot.model.PbClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientsRepository extends JpaRepository<PbClient, Long> {
}
