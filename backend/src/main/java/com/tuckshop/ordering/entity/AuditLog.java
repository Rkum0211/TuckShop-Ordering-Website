package com.tuckshop.ordering.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String adminUsername;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 50)
    private String entityType;

    @Column(length = 50)
    private String entityId;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
