package com.example.expensesharingapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.expensesharingapp.model.Group;
import com.example.expensesharingapp.model.Settlement;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

	List<Settlement> findByGroup(Group group);

}