package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.repository.ClientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ClientsRepository clientsRepository;

    public String markAsAdmin() {
        return "назначен админом";
    }

}
