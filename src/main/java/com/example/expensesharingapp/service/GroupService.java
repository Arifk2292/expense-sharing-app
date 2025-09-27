package com.example.expensesharingapp.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.User;

@Service
public interface GroupService {

    public Group createGroup(String name, Long adminId);

    public Group addMember(Long groupId, Long userId);

    public Map<User, BigDecimal> getBalances(Long groupId);

    public Optional<Group> findById(Long groupId);

    public void deleteGroup(Long groupId);

    public boolean isUserMemberOfGroup(Long groupId, String email);
}