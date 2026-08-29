package com.example.converter_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String topic;

    private String status = "PENDING"; //

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime publishedAt;
}