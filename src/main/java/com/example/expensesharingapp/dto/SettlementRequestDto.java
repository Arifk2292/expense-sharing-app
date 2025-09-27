package com.example.expensesharingapp.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SettlementRequestDto {

	private Long payerId;
	private Long receiverId;
	private BigDecimal amount;
	private Long groupId;

}