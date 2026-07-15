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
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // private 접근 제어자 추가

  @Column(name = "original_file_name", nullable = false, length = 255)
  private String originalFileName;

  @Column(name = "stored_file_name", nullable = false, length = 255)
  private String storedFileName;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "path", nullable = false, length = 500)
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
