package com.vogulev.list_randomizing_telegram_bot.service;

import com.vogulev.list_randomizing_telegram_bot.entity.PbUser;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShuffleService {
    public String shuffleNames(List<PbUser> users) {
        Collections.shuffle(users);
        return users.stream()
                .map(PbUser::getName)
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
