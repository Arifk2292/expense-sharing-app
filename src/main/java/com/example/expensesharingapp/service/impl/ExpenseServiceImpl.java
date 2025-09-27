package com.example.expensesharingapp.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensesharingapp.exception.ResourceNotFoundException;
import com.example.expensesharingapp.model.Expense;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.ExpenseRepository;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.ExpenseService;

@Service
public class ExpenseServiceImpl implements ExpenseService {
	
	@Autowired
    private ExpenseRepository expenseRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private GroupRepository groupRepository;

    @Transactional
    @Override
    public Expense addExpense(String description, BigDecimal amount, Long payerId, Long groupId, Map<Long, BigDecimal> splits) {
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCreatedBy(payer);
        expense.setGroup(group);
        expense.setDate(new Date());

        Map<User, BigDecimal> userSplits = splits.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> {
                            User user = userRepository.findById(entry.getKey())
                                    .orElseThrow(() -> new ResourceNotFoundException("User not found in split"));
                            if (!group.getMembers().contains(user)) {
                                throw new IllegalArgumentException("User with ID " + user.getId() + " is not a member of the group.");
                            }
                            return user;
                        },
                        Map.Entry::getValue
                ));
        expense.setSplits(userSplits);

        return expenseRepository.save(expense);
    }
}