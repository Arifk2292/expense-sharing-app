package com.example.expensesharingapp.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensesharingapp.exception.ResourceNotFoundException;
import com.example.expensesharingapp.model.Expense;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.Settlement;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.ExpenseRepository;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.SettlementRepository;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.GroupService;

@Service
public class GroupServiceImpl implements GroupService{
	
	@Autowired
    private GroupRepository groupRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private ExpenseRepository expenseRepository;
	@Autowired
    private SettlementRepository settlementRepository;

	@Override
    public Group createGroup(String name, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Group newGroup = new Group();
        newGroup.setName(name);
        newGroup.setAdmin(admin);
        newGroup.getMembers().add(admin);
        return groupRepository.save(newGroup);
    }

	@Override
    public Group addMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        group.getMembers().add(user);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (group.getAdmin().equals(userToRemove)) {
            throw new IllegalArgumentException("Admin cannot be removed from the group.");
        }

        if (!group.getMembers().contains(userToRemove)) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }

        Map<User, BigDecimal> balances = getBalances(groupId);
        BigDecimal userBalance = balances.get(userToRemove);

        if (userBalance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("User has an unsettled balance and cannot be removed.");
        }

        group.getMembers().remove(userToRemove);
        groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<User, BigDecimal> getBalances(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        
        Map<User, BigDecimal> balances = new HashMap<>();
        for (User member : group.getMembers()) {
            balances.put(member, BigDecimal.ZERO);
        }

        for (Expense expense : expenseRepository.findByGroup(group)) {
            User payer = expense.getCreatedBy();
            balances.put(payer, balances.get(payer).add(expense.getAmount()));
            for (Map.Entry<User, BigDecimal> split : expense.getSplits().entrySet()) {
                User participant = split.getKey();
                balances.put(participant, balances.get(participant).subtract(split.getValue()));
            }
        }

        List<Settlement> settlements = settlementRepository.findByGroup(group);
        for (Settlement settlement : settlements) {
            User payer = settlement.getPayer();
            User receiver = settlement.getReceiver();
            BigDecimal amount = settlement.getAmount();

            // Payer's balance increases (they paid, so their debt decreases)
            balances.put(payer, balances.get(payer).add(amount));
            // Receiver's balance decreases (they received, so they are owed less)
            balances.put(receiver, balances.get(receiver).subtract(amount));
        }

        return balances;
    }

    @Override
    public Optional<Group> findById(Long groupId) {
        return groupRepository.findById(groupId);
    }

    @Override
    public void deleteGroup(Long groupId) {
        groupRepository.deleteById(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMemberOfGroup(Long groupId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        return group.getMembers().contains(user);
    }
}