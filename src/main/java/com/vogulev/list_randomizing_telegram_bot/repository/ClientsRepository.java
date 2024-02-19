package com.vogulev.list_randomizing_telegram_bot.repository;

import com.vogulev.list_randomizing_telegram_bot.entity.PbClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientsRepository extends JpaRepository<PbClient, Long> {
    List<PbClient> findAllByActiveTrue();
    Optional<PbClient> getPbClientsByChatId(Long chatId);
}
