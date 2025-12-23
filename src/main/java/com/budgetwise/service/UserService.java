package com.budgetwise.service;

import com.budgetwise.entity.User;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> authenticateUser(String email, String password);
    Optional<User> getUserById(Long id);
    boolean emailExists(String email);
    User findByEmail(String email);
}