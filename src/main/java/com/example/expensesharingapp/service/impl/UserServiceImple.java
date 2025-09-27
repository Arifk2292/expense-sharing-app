package com.example.expensesharingapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.example.expensesharingapp.exception.ResourceNotFoundException;

import java.util.Optional;

@Service
public class UserServiceImple implements UserService{
	
	@Autowired
    private UserRepository userRepository;

    public User createOrUpdateUser(String name, String email, String oauthId) {
        User user = userRepository.findByOauthId(oauthId)
                .orElse(new User());
        user.setName(name);
        user.setEmail(email);
        user.setOauthId(oauthId);
        user.setRole("USER");
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        return findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}