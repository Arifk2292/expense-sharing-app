package com.example.expensesharingapp.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.expensesharingapp.model.Expense;

@Service
public interface ExpenseService {
    
	public Expense addExpense(String description, BigDecimal amount, Long payerId, Long groupId, Map<Long, BigDecimal> splits);
}