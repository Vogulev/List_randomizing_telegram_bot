package com.vogulev.list_randomizing_telegram_bot.repository;

import com.vogulev.list_randomizing_telegram_bot.model.PbUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NamesRepository extends JpaRepository<PbUser, Long> {
    @Transactional
    Integer deletePbUserByName(String name);
}
