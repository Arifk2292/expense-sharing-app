package com.example.expensesharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.User;
import com.example.expensesharingapp.repository.GroupRepository;
import com.example.expensesharingapp.repository.UserRepository;
import com.example.expensesharingapp.service.impl.GroupServiceImpl;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    public void testCreateGroup() {
        User admin = new User(1L, "Test User", "test@example.com", "oauth-test-id", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        
        Group group = new Group(1L, "Test Group", admin, new HashSet<>(Set.of(admin)));
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        
        Group createdGroup = groupService.createGroup("Test Group", 1L);

        assertEquals("Test Group", createdGroup.getName());
        assertEquals(admin, createdGroup.getAdmin());
        assertEquals(1, createdGroup.getMembers().size());
    }

    @Test
    public void testAddMember() {
        User member = new User(2L, "New Member", "member@example.com", "oauth-member-id", "USER");
        User admin = new User(1L, "Test User", "test@example.com", "oauth-test-id", "USER");
        Group group = new Group(1L, "Test Group", admin, new HashSet<>(Set.of(admin)));

        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArguments()[0]);

        Group updatedGroup = groupService.addMember(1L, 2L);

        assertEquals(2, updatedGroup.getMembers().size());
    }

    @Test
    public void testIsUserMemberOfGroup() {
        User admin = new User(1L, "Test User", "test@example.com", "oauth-test-id", "USER");
        User nonMember = new User(2L, "Non Member", "nonmember@example.com", "oauth-nonmember-id", "USER");
        Group group = new Group(1L, "Test Group", admin, new HashSet<>(Set.of(admin)));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("nonmember@example.com")).thenReturn(Optional.of(nonMember));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertTrue(groupService.isUserMemberOfGroup(1L, "test@example.com"));
        assertFalse(groupService.isUserMemberOfGroup(1L, "nonmember@example.com"));
    }
}