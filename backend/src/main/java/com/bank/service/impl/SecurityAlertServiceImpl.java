package com.bank.service.impl;

import com.bank.dto.SecurityAlertResponse;
import com.bank.entity.SecurityAlert;
import com.bank.enums.AlertSeverity;
import com.bank.exception.BankingException;
import com.bank.mapper.EntityDtoMapper;
import com.bank.repository.SecurityAlertRepository;
import com.bank.service.SecurityAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SecurityAlertServiceImpl implements SecurityAlertService {

    private final SecurityAlertRepository alertRepository;

    public SecurityAlertServiceImpl(SecurityAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityAlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByTimestampDesc().stream()
                .map(EntityDtoMapper::toSecurityAlertResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityAlertResponse> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverity(severity).stream()
                .map(EntityDtoMapper::toSecurityAlertResponse)
                .toList();
    }

    @Override
    public void resolveAlert(String alertId, String resolutionNotes) {
        SecurityAlert alert = alertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new BankingException("Alert " + alertId + " not found"));
        alert.resolve(resolutionNotes);
        alertRepository.save(alert);
    }
}
