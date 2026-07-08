package com.example.demo.repository;

import com.example.demo.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Alert> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndIsReadFalse(Long userId);
}
