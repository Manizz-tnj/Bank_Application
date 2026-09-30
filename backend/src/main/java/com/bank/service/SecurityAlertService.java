package com.bank.service;

import com.bank.dto.SecurityAlertResponse;
import com.bank.enums.AlertSeverity;
import java.util.List;

public interface SecurityAlertService {
    List<SecurityAlertResponse> getAllAlerts();
    List<SecurityAlertResponse> getAlertsBySeverity(AlertSeverity severity);
    void resolveAlert(String alertId, String resolutionNotes);
}
