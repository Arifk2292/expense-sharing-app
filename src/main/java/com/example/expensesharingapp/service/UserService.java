package com.example.expensesharingapp.service;

import org.springframework.stereotype.Service;

import com.example.expensesharingapp.model.User;

import java.util.Optional;

@Service
public interface UserService {

    public User createOrUpdateUser(String name, String email, String oauthId);

    Optional<User> findByEmail(String email);

    User getAuthenticatedUser();
}