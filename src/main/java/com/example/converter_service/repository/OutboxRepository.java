package com.example.converter_service.repository;

import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {

    Optional<OutboxRecord> findByMessageId(String messageId);

    List<OutboxRecord> findByStatus(OutboxStatus status);

    List<OutboxRecord> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}