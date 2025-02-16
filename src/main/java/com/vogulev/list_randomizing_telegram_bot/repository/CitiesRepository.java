package com.vogulev.list_randomizing_telegram_bot.repository;

import com.vogulev.list_randomizing_telegram_bot.entity.PbCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitiesRepository extends JpaRepository<PbCity, Long> {
}
