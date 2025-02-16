package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.entity.PbUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShuffleService {

    public String shuffle(List<PbUser> users) {
        var number = new AtomicInteger(1);
        Collections.shuffle(users);
        return users.stream()
                .map(pbUser -> number.getAndIncrement() + ". " + pbUser.getName())
                .collect(Collectors.joining("\n"));
    }
}
