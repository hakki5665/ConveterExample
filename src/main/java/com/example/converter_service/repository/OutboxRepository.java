package com.example.converter_service.repository;

import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {
    List<OutboxRecord> findByStatus(OutboxStatus status);

    Optional<OutboxRecord> findByMessageId(String messageId);
}