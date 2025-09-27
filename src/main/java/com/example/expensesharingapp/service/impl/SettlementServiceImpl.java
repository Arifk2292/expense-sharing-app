package com.example.expensesharingapp.service.impl;

import java.math.BigDecimal;
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
import com.example.expensesharingapp.service.SettlementService;

@Service
public class SettlementServiceImpl implements SettlementService {

	@Autowired
    private SettlementRepository settlementRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private GroupRepository groupRepository;
    
    @Transactional
    public Settlement settle(Long payerId, Long receiverId, BigDecimal amount, Long groupId) {
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!group.getMembers().contains(payer) || !group.getMembers().contains(receiver)) {
            throw new IllegalArgumentException("Both payer and receiver must be members of the group.");
        }

        if (payer.equals(receiver)) {
            throw new IllegalArgumentException("Payer and receiver cannot be the same user.");
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