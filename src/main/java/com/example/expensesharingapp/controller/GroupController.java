package com.example.expensesharingapp.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensesharingapp.dto.AddMemberRequestDto;
import com.example.expensesharingapp.dto.CreateGroupRequestDto;
import com.example.expensesharingapp.model.Expense;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.service.ExpenseService;
import com.example.expensesharingapp.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/groups")
public class GroupController {

	@Autowired
    private GroupService groupService;

    @Autowired
    private ExpenseService expenseService;

    @Operation(summary = "Create a new group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequestDto request) {
        Group group = groupService.createGroup(request.getName(), request.getAdminId());
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Add a member to a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Group or User not found")
    })
    @PostMapping("/{groupId}/members")
    @PreAuthorize("hasPermission(#groupId, 'Group', 'isAdmin')")
    public ResponseEntity<Group> addMember(@PathVariable Long groupId, @RequestBody AddMemberRequestDto request) {
        Group group = groupService.addMember(groupId, request.getUserId());
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Remove a member from a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Group or User not found")
    })
    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasPermission(#groupId, 'Group', 'isAdmin')")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all expenses for a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/expenses")
    @PreAuthorize("hasPermission(#groupId, 'Group', 'isMember')")
    public ResponseEntity<List<Expense>> getExpenses(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.findByGroupId(groupId);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get balances for a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balances retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/balances")
    @PreAuthorize("hasPermission(#groupId, 'Group', 'isMember')")
    public ResponseEntity<Map<String, BigDecimal>> getBalances(@PathVariable Long groupId) {
        Map<User, BigDecimal> balances = groupService.getBalances(groupId);
        Map<String, BigDecimal> response = balances.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getName(), Map.Entry::getValue));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}")
    @PreAuthorize("hasPermission(#groupId, 'Group', 'isMember')")
    public ResponseEntity<Group> getGroupById(@PathVariable Long groupId) {
        return groupService.findById(groupId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasPermission(#groupId, 'Group', 'isAdmin')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}