package com.codeit.hrbank.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 시각 자동 기록
public class File {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 숫자 자동 증가
  private Long id;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  Instant createdAt;

  @Column(nullable = false, length = 255)
  private String originalFileName;

  @Column(nullable = false, length = 255)
  private String storedFileName;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false, length = 500)
  private String path;

  public File(String originalFileName, String storedFileName,
      String contentType, Long size, String path) {
    this.originalFileName = originalFileName;
    this.storedFileName = storedFileName;
    this.contentType = contentType;
    this.size = size;
    this.path = path;
  }
}
