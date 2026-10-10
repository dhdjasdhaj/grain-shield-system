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
@Table(name = "execution_feedback")
public class ExecutionFeedback extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private DispatchPlan plan;

    @Column(nullable = false, length = 30)
    private String feedbackType;

    @Column(nullable = false, length = 600)
    private String content;

    @Column(nullable = false, length = 30)
    private String executionStatus;

    @Column(nullable = false, length = 50)
    private String feedbackBy;

    @Column(nullable = false)
    private LocalDateTime feedbackTime;
}
