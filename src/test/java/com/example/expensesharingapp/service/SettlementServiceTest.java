package com.example.expensesharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.Settlement;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.SettlementRepository;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.impl.SettlementServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @Test
    public void testSettle() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user2 = new User(2L, "User Two", "user2@example.com", "oauth2", "USER");
        Group group = new Group(1L, "Test Group", user1, new HashSet<>(Set.of(user1, user2)));
        
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(user1, new BigDecimal("-50.00"));
        balances.put(user2, new BigDecimal("50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(groupService.getBalances(1L)).thenReturn(balances);
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(i -> i.getArguments()[0]);
        
        Settlement settlement = settlementService.settle(1L, 2L, new BigDecimal("50.00"), 1L);

        assertEquals(user1, settlement.getPayer());
        assertEquals(user2, settlement.getReceiver());
        assertEquals(new BigDecimal("50.00"), settlement.getAmount());
    }

    @Test
    public void testSettle_shouldThrowExceptionWhenUserNotInGroup() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user3 = new User(3L, "User Three", "user3@example.com", "oauth3", "USER"); // Not in group
        Group group = new Group(1L, "Test Group", user1, new HashSet<>(Set.of(user1)));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user3));
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            settlementService.settle(1L, 3L, new BigDecimal("25.00"), 1L);
        });
    }

    @Test
    public void testSettle_shouldThrowExceptionForPositiveAmount() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user2 = new User(2L, "User Two", "user2@example.com", "oauth2", "USER");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            settlementService.settle(1L, 2L, new BigDecimal("-50.00"), 1L);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            settlementService.settle(1L, 2L, new BigDecimal("0.00"), 1L);
        });
    }

    @Test
    public void testSettle_shouldThrowExceptionWhenPayerHasNoDebt() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user2 = new User(2L, "User Two", "user2@example.com", "oauth2", "USER");
        Group group = new Group(1L, "Test Group", user1, new HashSet<>(Set.of(user1, user2)));
        
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(user1, new BigDecimal("50.00")); // Positive balance
        balances.put(user2, new BigDecimal("-50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(groupService.getBalances(1L)).thenReturn(balances);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            settlementService.settle(1L, 2L, new BigDecimal("50.00"), 1L);
        });
    }

    @Test
    public void testSettle_shouldThrowExceptionWhenAmountIsGreaterThanDebt() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user2 = new User(2L, "User Two", "user2@example.com", "oauth2", "USER");
        Group group = new Group(1L, "Test Group", user1, new HashSet<>(Set.of(user1, user2)));
        
        Map<User, BigDecimal> balances = new HashMap<>();
        balances.put(user1, new BigDecimal("-50.00"));
        balances.put(user2, new BigDecimal("50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(groupService.getBalances(1L)).thenReturn(balances);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            settlementService.settle(1L, 2L, new BigDecimal("100.00"), 1L);
        });
    }
}