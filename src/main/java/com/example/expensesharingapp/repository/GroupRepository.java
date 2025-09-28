package com.example.expensesharingapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.expensesharingapp.model.Group;

import jakarta.persistence.LockModeType;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Group g WHERE g.id = :groupId")
    Optional<Group> findByIdForUpdate(@Param("groupId") Long groupId);
}