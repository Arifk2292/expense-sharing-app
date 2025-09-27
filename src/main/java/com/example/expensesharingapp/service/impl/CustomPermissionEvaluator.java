package com.example.expensesharingapp.service.impl;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (!"Group".equalsIgnoreCase(targetType)) {
            return false;
        }

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return false;
        }

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null) {
            return false;
        }

        Long groupId = (Long) targetId;
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with ID: " + groupId));

        return userRepository.findByEmail(email).map(user -> {
            if ("isMember".equalsIgnoreCase(permission.toString())) {
                return group.getMembers().contains(user);
            }
            if ("isAdmin".equalsIgnoreCase(permission.toString())) {
                return group.getAdmin().equals(user);
            }
            return false;
        }).orElse(false);
    }
}