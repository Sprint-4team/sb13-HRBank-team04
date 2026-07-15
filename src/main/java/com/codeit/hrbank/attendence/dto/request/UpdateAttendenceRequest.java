package com.codeit.hrbank.attendence.dto.request;

import com.codeit.hrbank.attendence.enums.AttendanceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public record UpdateAttendenceRequest(
    @NotNull Long employeeId,
    @NotNull LocalDate date,
    @NotNull AttendanceType type,
    @Size(max = 500) String memo
) {

}
