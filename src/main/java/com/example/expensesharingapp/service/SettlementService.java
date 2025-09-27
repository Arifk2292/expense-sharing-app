package com.example.expensesharingapp.service;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.expensesharingapp.model.Settlement;

@Service
public interface SettlementService {
    
    public Settlement settle(Long payerId, Long receiverId, BigDecimal amount, Long groupId);

    public Optional<Settlement> findById(Long id);
}