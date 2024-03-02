package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.entity.PbClient;
import com.vogulev.list_randomizing_telegram_bot.repository.ClientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientsRepository clientsRepository;

    public void save(Long chatId, String name) {
        var pbClient = new PbClient();
        pbClient.setName(name);
        pbClient.setChatId(chatId);
        pbClient.setActive(true);
        clientsRepository.save(pbClient);
    }

    public void save(PbClient client) {
        clientsRepository.save(client);
    }

    public Optional<PbClient> get(Long chatId) {
        return clientsRepository.getPbClientsByChatId(chatId);
    }

    public List<PbClient> getAllActive() {
        return clientsRepository.findAllByActiveTrue();
    }
}
