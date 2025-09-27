package com.example.expensesharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.expensesharingapp.exception.ResourceNotFoundException;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.impl.UserServiceImple;
import static org.mockito.Mockito.mock;
import java.util.Map;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImple userService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void testCreateOrUpdateUser_shouldCreateNewUser() {
        when(userRepository.findByOauthId("new-oauth-id")).thenReturn(Optional.empty());
        
        User userToSave = new User(null, "New User", "new@example.com", "new-oauth-id", "USER");
        when(userRepository.save(any(User.class))).thenReturn(userToSave);
        
        User result = userService.createOrUpdateUser("New User", "new@example.com", "new-oauth-id");

        assertEquals("New User", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testCreateOrUpdateUser_shouldUpdateExistingUser() {
        User existingUser = new User(1L, "Old Name", "old@example.com", "existing-oauth-id", "USER");
        when(userRepository.findByOauthId("existing-oauth-id")).thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.createOrUpdateUser("New Name", "new@example.com", "existing-oauth-id");
        
        assertEquals(1L, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(existingUser);
    }

    @Test
    public void testFindByEmail_shouldReturnUserWhenFound() {
        User user = new User(1L, "Test User", "test@example.com", "oauth-id", "USER");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByEmail("test@example.com");

        assertEquals(user, foundUser.get());
    }

    @Test
    public void testGetAuthenticatedUser_shouldReturnUserWhenAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Map<String, Object> attributes = Collections.singletonMap("email", "test@example.com");
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "email");
        User user = new User(1L, "Test User", "test@example.com", "oauth-id", "USER");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User authUser = userService.getAuthenticatedUser();

        assertEquals(user, authUser);
    }

    @Test
    public void testGetAuthenticatedUser_shouldThrowExceptionWhenUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Map<String, Object> attributes = Collections.singletonMap("email", "notfound@example.com");
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "email");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getAuthenticatedUser();
        });
    }
}