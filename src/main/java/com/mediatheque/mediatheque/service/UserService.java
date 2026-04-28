package com.mediatheque.mediatheque.service;

import com.mediatheque.mediatheque.dto.UserDTO;
import com.mediatheque.mediatheque.model.User;
import com.mediatheque.mediatheque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean registerUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return false;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return true;
    }
}
