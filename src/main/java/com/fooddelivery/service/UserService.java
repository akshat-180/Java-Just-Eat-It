package com.fooddelivery.service;

import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User u) {
        // encode the raw password before saving
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return repo.save(u);
    }

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public boolean authenticate(String username, String rawPassword) {
        return repo.findByUsername(username)
                .map(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }
}
