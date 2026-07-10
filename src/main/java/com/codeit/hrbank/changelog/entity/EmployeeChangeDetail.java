package com.codeit.hrbank.changelog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "employee_change_details")
public class EmployeeChangeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_log_id", nullable = false)
    private EmployeeChangeLog changeLog;

    @Column(name = "property_name", nullable = false, length = 100)
    private String propertyName;

    @Column(columnDefinition = "TEXT")
    private String before;

    @Column(columnDefinition = "TEXT")
    private String after;

}
