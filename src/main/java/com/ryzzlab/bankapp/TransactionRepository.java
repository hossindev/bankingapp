package com.ryzzlab.bankapp;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByFromIbanOrToIban(String fromIban, String toIban, Pageable pageable);

    Page<Transaction> findByUser_Id(UUID userId, Pageable pageable);
}
