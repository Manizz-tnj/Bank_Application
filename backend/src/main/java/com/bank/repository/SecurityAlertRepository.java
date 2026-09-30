package com.bank.repository;

import com.bank.entity.SecurityAlert;
import com.bank.enums.AlertSeverity;
import com.bank.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    Optional<SecurityAlert> findByAlertId(String alertId);
    List<SecurityAlert> findBySeverity(AlertSeverity severity);
    List<SecurityAlert> findByStatus(AlertStatus status);
    List<SecurityAlert> findAllByOrderByTimestampDesc();
}
