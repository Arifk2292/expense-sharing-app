package com.example.expensesharingapp.dto;

import java.math.BigDecimal;

import java.util.Map;

import lombok.Data;

@Data
public class ExpenseRequestDto {

	private String description;
	private BigDecimal amount;
	private Long payerId;
	private Long groupId;
	private Map<Long, BigDecimal> splits;

}