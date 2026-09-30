package com.bank.entity;

import com.bank.enums.AlertSeverity;
import com.bank.enums.AlertStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", unique = true, nullable = false, length = 30)
    private String alertId;

    @Column(name = "customer_id", length = 30)
    private String customerId;

    @Column(name = "account_id", length = 30)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.OPEN;

    @Column(name = "rule_triggered", nullable = false, length = 50)
    private String ruleTriggered;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "resolution_notes", length = 255)
    private String resolutionNotes;

    public SecurityAlert() {
    }

    public SecurityAlert(String alertId, String customerId, String accountId,
                         AlertSeverity severity, String ruleTriggered, String description) {
        this.alertId = alertId;
        this.customerId = customerId;
        this.accountId = accountId;
        this.severity = severity;
        this.ruleTriggered = ruleTriggered;
        this.description = description;
        this.status = AlertStatus.OPEN;
        this.timestamp = LocalDateTime.now();
    }

    public void resolve(String resolutionNotes) {
        this.status = AlertStatus.RESOLVED;
        this.resolutionNotes = resolutionNotes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getRuleTriggered() {
        return ruleTriggered;
    }

    public void setRuleTriggered(String ruleTriggered) {
        this.ruleTriggered = ruleTriggered;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
