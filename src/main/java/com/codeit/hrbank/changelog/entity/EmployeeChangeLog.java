package com.codeit.hrbank.changelog.entity;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.common.BaseEntity;
import com.codeit.hrbank.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Employee employee;

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
            Employee employee,
            String employeeNumber,
            String memo,
            String ipAddress
    ) {
        this.type = type;
        this.employee = employee;
        this.employeeNumber = employeeNumber;
        this.memo = memo;
        this.ipAddress = ipAddress;
    }

}
