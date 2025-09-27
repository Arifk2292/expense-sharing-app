package com.example.expensesharingapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensesharingapp.dto.SettlementRequestDto;
import com.example.expensesharingapp.model.Settlement;
import com.example.expensesharingapp.service.SettlementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

	@Autowired
    private SettlementService settlementService;

    @Operation(summary = "Settle a payment within a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlement successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group or User not found")
    })
    @PostMapping
    @PreAuthorize("@groupRepository.findById(#request.groupId).get().members.contains(@userRepository.findByEmail(authentication.principal.attributes['email']).get())")
    public ResponseEntity<Settlement> settle(@RequestBody SettlementRequestDto request) {
        Settlement settlement = settlementService.settle(
                request.getPayerId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getGroupId());
        return ResponseEntity.ok(settlement);
    }

    @Operation(summary = "Get settlement details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlement details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Settlement not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@settlementRepository.findById(#id).get().group.members.contains(@userRepository.findByEmail(authentication.principal.attributes['email']).get())")
    public ResponseEntity<Settlement> getSettlementById(@PathVariable Long id) {
        return settlementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}