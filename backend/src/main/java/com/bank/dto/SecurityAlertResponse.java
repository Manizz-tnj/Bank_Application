package com.bank.dto;

import com.bank.enums.AlertSeverity;
import com.bank.enums.AlertStatus;
import java.time.LocalDateTime;

public class SecurityAlertResponse {

    private Long id;
    private String alertId;
    private String customerId;
    private String accountId;
    private AlertSeverity severity;
    private AlertStatus status;
    private String ruleTriggered;
    private String description;
    private LocalDateTime timestamp;
    private String resolutionNotes;

    public SecurityAlertResponse() {
    }

    public SecurityAlertResponse(Long id, String alertId, String customerId, String accountId,
                                 AlertSeverity severity, AlertStatus status, String ruleTriggered,
                                 String description, LocalDateTime timestamp, String resolutionNotes) {
        this.id = id;
        this.alertId = alertId;
        this.customerId = customerId;
        this.accountId = accountId;
        this.severity = severity;
        this.status = status;
        this.ruleTriggered = ruleTriggered;
        this.description = description;
        this.timestamp = timestamp;
        this.resolutionNotes = resolutionNotes;
    }

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
