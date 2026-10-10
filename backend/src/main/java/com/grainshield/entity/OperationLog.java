package com.grainshield.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "operation_log")
public class OperationLog extends BaseEntity {
    private Long userId;
    @Column(length = 50)
    private String username;
    @Column(nullable = false, length = 50)
    private String module;
    @Column(nullable = false, length = 50)
    private String action;
    @Column(nullable = false, length = 1000)
    private String content;
    @Column(length = 64)
    private String ipAddress;
    @Column(nullable = false)
    private LocalDateTime operationTime;
}
