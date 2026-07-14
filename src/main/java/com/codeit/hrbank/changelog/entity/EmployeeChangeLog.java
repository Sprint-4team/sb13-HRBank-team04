package com.codeit.hrbank.changelog.entity;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "employee_change_logs")
public class EmployeeChangeLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeChangeType type;

    @Column(name = "employee_id")
    private Long employeeId;
    /*
    나중에 Employee Entity가 생기면
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    로 대체
     */

    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;

    @Column(length = 500)
    private String memo;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @OneToMany(
            mappedBy = "changeLog",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EmployeeChangeDetail> details = new ArrayList<>();

    @Builder
    private EmployeeChangeLog(
            EmployeeChangeType type,
            Long employeeId,
            String employeeNumber,
            String memo,
            String ipAddress
    ) {
        this.type = type;
        this.employeeId = employeeId;
        this.employeeNumber = employeeNumber;
        this.memo = memo;
        this.ipAddress = ipAddress;
    }

}
