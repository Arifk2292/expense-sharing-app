package com.example.expensesharingapp.repository;

import java.util.List;

 

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

 

import com.example.expensesharingapp.model.Expense;

import com.example.expensesharingapp.model.Group;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroup(Group group);

}