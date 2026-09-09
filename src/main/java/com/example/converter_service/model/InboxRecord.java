package com.example.converter_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox")
@Getter
@Setter
public class InboxRecord {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}