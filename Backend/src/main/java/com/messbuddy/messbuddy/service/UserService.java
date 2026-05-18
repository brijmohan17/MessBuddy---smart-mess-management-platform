package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.MessRepository;
import com.messbuddy.messbuddy.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final MenuRepository menuRepository;
    private final MessRepository messRepository;

    public User deleteUserAccount(String userId) {
        menuRepository.findAll().stream()
                .filter(menu -> userId.equals(menu.getOwner_ID()))
                .forEach(menuRepository::delete);

        messRepository.findByOwnerId(userId).ifPresent(messRepository::delete);

        User user = authRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        authRepository.delete(user);
        return user;
    }

    public User getUser(String userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
