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

import com.example.expensesharingapp.model.Expense;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.ExpenseRepository;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.impl.ExpenseServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Test
    public void testAddExpense() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user2 = new User(2L, "User Two", "user2@example.com", "oauth2", "USER");
        Group group = new Group(1L, "Test Group", user1, new HashSet<>(Set.of(user1, user2)));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        Map<Long, BigDecimal> splits = new HashMap<>();
        splits.put(1L, new BigDecimal("50.00"));
        splits.put(2L, new BigDecimal("50.00"));

        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArguments()[0]);
        
        Expense expense = expenseService.addExpense("Dinner", new BigDecimal("100.00"), 1L, 1L, splits);

        assertEquals("Dinner", expense.getDescription());
        assertEquals(new BigDecimal("100.00"), expense.getAmount());
        assertEquals(user1, expense.getCreatedBy());
    }

    @Test
    public void testAddExpense_shouldThrowExceptionIfSplitUserNotInGroup() {
        User user1 = new User(1L, "User One", "user1@example.com", "oauth1", "USER");
        User user3 = new User(3L, "User Three", "user3@example.com", "oauth3", "USER"); // Not in group
        Group group = new Group(1L, "Test Group", user1, new HashSet<>(Set.of(user1)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user3));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        Map<Long, BigDecimal> splits = new HashMap<>();
        splits.put(1L, new BigDecimal("50.00"));
        splits.put(3L, new BigDecimal("50.00"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            expenseService.addExpense("Lunch", new BigDecimal("100.00"), 1L, 1L, splits);
        });
    }
}