package com.budgetwise.service;

import com.budgetwise.entity.User;
import com.budgetwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        System.out.println("REGISTERING USER: " + user.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        // ENCRYPT PASSWORD - CRITICAL STEP
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        System.out.println("Password encoded successfully");

        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);
        System.out.println("USER SAVED to database with ID: " + savedUser.getId());

        return savedUser;
    }

    @Override
    public Optional<User> authenticateUser(String email, String password) {
        System.out.println("AUTHENTICATING: " + email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("User found in DB: " + user.getEmail());

            // Verify password matches
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("Password match: " + passwordMatches);

            if (passwordMatches) {
                // Update last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("AUTHENTICATION SUCCESS for: " + email);
                return Optional.of(user);
            }
        }

        System.out.println("AUTHENTICATION FAILED for: " + email);
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean emailExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        System.out.println("Email exists check for " + email + ": " + exists);
        return exists;
    }
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}