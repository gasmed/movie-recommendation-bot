package com.example.moviebot.service;

import com.example.moviebot.model.User;
import com.example.moviebot.model.Preference;
import com.example.moviebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Mono<User> findOrCreateUser(Long chatId, String username) {
        return userRepository.findByChatId(chatId)
                .switchIfEmpty(createUser(chatId, username));
    }

    private Mono<User> createUser(Long chatId, String username) {
        User newUser = new User();
        newUser.setChatId(chatId);
        newUser.setUsername(username);
        newUser.setPreferences(new ArrayList<>());
        return userRepository.save(newUser);
    }

    public Mono<User> addPreference(Long chatId, Preference preference) {
        return userRepository.findByChatId(chatId)
                .flatMap(user -> {
                    user.getPreferences().add(preference);
                    return userRepository.save(user);
                });
    }

    public Mono<User> deletePreference(Long chatId, int index) {
        return userRepository.findByChatId(chatId)
                .flatMap(user -> {
                    if (index >= 0 && index < user.getPreferences().size()) {
                        user.getPreferences().remove(index);
                        return userRepository.save(user);
                    }
                    return Mono.just(user);
                });
    }

    public Mono<Void> setUserState(long chatId, String state) {
        return userRepository.findByChatId(chatId)
                .flatMap(user -> {
                    user.setState(state);
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setChatId(chatId);
                    newUser.setState(state);
                    return userRepository.save(newUser);
                }))
                .then();
    }

    public Mono<String> getUserState(long chatId) {
        return userRepository.findByChatId(chatId)
                .map(User::getState)
                .defaultIfEmpty("NORMAL");
    }
}