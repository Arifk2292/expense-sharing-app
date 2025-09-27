package com.example.expensesharingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.expensesharingapp.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

}