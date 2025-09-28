package com.example.expensesharingapp.service.impl;

import com.example.expensesharingapp.exception.ResourceNotFoundException;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // This method is not used in this implementation
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || targetType == null) {
            return false;
        }

        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return false;
        }

        if ("Group".equalsIgnoreCase(targetType)) {
            Long groupId = (Long) targetId;
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

            switch (permission.toString()) {
                case "isMember":
                    return group.getMembers().contains(user);
                case "isAdmin":
                    return group.getAdmin().equals(user);
                default:
                    return false;
            }
        }
        return false;
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userDetails.getUsername()));
        }
        return null;
    }
}