package com.example.expensesharingapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensesharingapp.dto.ExpenseRequestDto;
import com.example.expensesharingapp.model.Expense;
import com.example.expensesharingapp.service.ExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

	@Autowired
    private ExpenseService expenseService;

    @Operation(summary = "Add an expense to a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group or User not found")
    })
    @PostMapping
    @PreAuthorize("@groupRepository.findById(#request.groupId).get().members.contains(@userRepository.findByEmail(authentication.principal.attributes['email']).get())")
    public ResponseEntity<Expense> addExpense(@RequestBody ExpenseRequestDto request) {
        Expense expense = expenseService.addExpense(
                request.getDescription(),
                request.getAmount(),
                request.getPayerId(),
                request.getGroupId(),
                request.getSplits());
        return ResponseEntity.ok(expense);
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        
        return ResponseEntity.ok("Application is running..................");
    }
}