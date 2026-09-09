package com.example.converter_service.repository;

import com.example.converter_service.model.InboxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface InboxRepository extends JpaRepository<InboxRecord, String> {

    @Modifying
    @Transactional
    @Query("UPDATE InboxRecord i SET i.status = :status, i.processedAt = :processedAt WHERE i.messageId = :messageId")
    void updateStatus(@Param("messageId") String messageId,
                      @Param("status") String status,
                      @Param("processedAt") LocalDateTime processedAt);
}