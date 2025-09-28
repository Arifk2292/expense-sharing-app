package com.example.expensesharingapp.service.impl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensesharingapp.exception.ResourceNotFoundException;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.Settlement;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.SettlementRepository;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.GroupService;
import com.example.expensesharingapp.service.SettlementService;

import java.math.RoundingMode;

@Service
public class SettlementServiceImpl implements SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupService groupService;
    
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    @Transactional
    public Settlement settle(Long payerId, Long receiverId, BigDecimal amount, Long groupId) {
        amount = amount.setScale(MONEY_SCALE, ROUNDING_MODE);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be positive.");
        }
        
        Group group = groupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (!group.getMembers().contains(payer) || !group.getMembers().contains(receiver)) {
            throw new IllegalArgumentException("Both payer and receiver must be members of the group.");
        }

        if (payer.equals(receiver)) {
            throw new IllegalArgumentException("Payer and receiver cannot be the same user.");
        }

        Map<User, BigDecimal> balances = groupService.getBalances(groupId);
        BigDecimal payerBalance = balances.get(payer);
        if (payerBalance.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalArgumentException("Payer does not owe any money in the group.");
        }

        if (amount.compareTo(payerBalance.negate()) > 0) {
            throw new IllegalArgumentException("Settlement amount is greater than the amount owed.");
        }

        Settlement settlement = new Settlement();
        settlement.setPayer(payer);
        settlement.setReceiver(receiver);
        settlement.setAmount(amount);
        settlement.setGroup(group);

        return settlementRepository.save(settlement);
    }

    public Optional<Settlement> findById(Long id) {
        return settlementRepository.findById(id);
    }
}