package com.example.expensesharingapp.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    @Transactional
    public Expense addExpense(String description, BigDecimal amount, Long payerId, Long groupId, Map<Long, BigDecimal> splits) {

        amount = amount.setScale(MONEY_SCALE, ROUNDING_MODE);
        Map<Long, BigDecimal> scaledSplits = splits.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().setScale(MONEY_SCALE, ROUNDING_MODE)));
        
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

        if (!group.getMembers().contains(payer)) {
            throw new IllegalArgumentException("Payer must be a member of the group.");
        }

        BigDecimal totalSplitAmount = scaledSplits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplitAmount.compareTo(amount) != 0) {
            throw new IllegalArgumentException("Total of splits must equal the expense amount.");
        }
        
        Map<User, BigDecimal> userSplits = new HashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : scaledSplits.entrySet()) {
            User participant = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Participant user not found"));
            if (!group.getMembers().contains(participant)) {
                throw new IllegalArgumentException("User with ID " + participant.getId() + " is not a member of the group.");
            }
            userSplits.put(participant, entry.getValue());
        }
        expense.setSplits(userSplits);

        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> findByGroupId(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        return expenseRepository.findByGroup(group);
    }
}