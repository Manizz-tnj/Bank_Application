package com.bank.controller;

import com.bank.dto.SecurityAlertResponse;
import com.bank.enums.AlertSeverity;
import com.bank.service.SecurityAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityAlertController {

    private final SecurityAlertService securityAlertService;

    public SecurityAlertController(SecurityAlertService securityAlertService) {
        this.securityAlertService = securityAlertService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<SecurityAlertResponse>> getAlerts(@RequestParam(required = false) AlertSeverity severity) {
        List<SecurityAlertResponse> response = (severity != null)
                ? securityAlertService.getAlertsBySeverity(severity)
                : securityAlertService.getAllAlerts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<Map<String, String>> resolveAlert(@PathVariable String alertId,
                                                            @RequestBody Map<String, String> payload) {
        String notes = payload.getOrDefault("notes", "Resolved by staff officer");
        securityAlertService.resolveAlert(alertId, notes);
        return ResponseEntity.ok(Map.of("message", "Alert " + alertId + " marked as RESOLVED"));
    }
}
