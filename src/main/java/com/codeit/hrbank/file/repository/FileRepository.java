package com.codeit.hrbank.file.repository;

import com.codeit.hrbank.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> { }
// Spring Data JPA 기본 메서드 우선 사용