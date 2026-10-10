package com.grainshield.entity;

import com.grainshield.enums.NodeStatus;
import com.grainshield.enums.NodeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "supply_node", indexes = {@Index(name = "idx_node_region", columnList = "region")})
public class SupplyNode extends BaseEntity {
    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NodeType type;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String region;

    @Column(length = 200)
    private String address;

    @DecimalMin("0")
    @Column(precision = 12, scale = 2)
    private BigDecimal capacity;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(length = 40)
    private String managerName;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NodeStatus status = NodeStatus.NORMAL;
}
