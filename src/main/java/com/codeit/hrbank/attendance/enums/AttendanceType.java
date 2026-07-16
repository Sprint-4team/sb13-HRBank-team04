package com.codeit.hrbank.attendance.enums;

public enum AttendanceType {
  LATE("지각"),
  ANNUAL_LEAVE("연차"),
  HALF_DAY("반차");

  private final String tag;

  AttendanceType(String tag) {
    this.tag = tag;
  }
}