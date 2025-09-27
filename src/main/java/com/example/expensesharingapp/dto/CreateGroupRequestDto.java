package com.example.expensesharingapp.dto;

import lombok.Data;

@Data
public class CreateGroupRequestDto {

	private String name;
	private Long adminId;

}