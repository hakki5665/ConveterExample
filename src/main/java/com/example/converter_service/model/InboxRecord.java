package com.example.converter_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InboxRecord {

    @Id
    @Column(unique = true, nullable = false)
    private String messageId;

    private LocalDateTime processedAt;

    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED, FAILED
}