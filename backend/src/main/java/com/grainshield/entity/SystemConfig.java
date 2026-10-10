package com.grainshield.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sys_config")
public class SystemConfig extends BaseEntity {
    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String configKey;
    @Column(nullable = false, length = 500)
    private String configValue;
    @Column(length = 200)
    private String description;
    @Column(nullable = false)
    private boolean enabled = true;
}
