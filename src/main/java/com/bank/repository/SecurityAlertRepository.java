package com.bank.repository;

import com.bank.enums.AlertSeverity;
import com.bank.enums.AlertStatus;
import com.bank.model.SecurityAlert;
import com.bank.util.IdGenerator;

import java.util.*;

/**
 * Repository managing Security Alerts.
 * Demonstrates Collections (Queue for pending reviews, HashSet for unique rules)
 * and Serialization.
 */
public class SecurityAlertRepository extends AbstractFileRepository<SecurityAlert, String> {

    public static final String FILE_PATH = "data/security_alerts.dat";

    // Queue for prioritizing pending/unreviewed security alerts
    private final Queue<SecurityAlert> pendingAlertQueue = new LinkedList<>();

    public SecurityAlertRepository() {
        super(FILE_PATH);
        refreshQueue();
        synchronizeSequence();
    }

    public SecurityAlertRepository(String customPath) {
        super(customPath);
        refreshQueue();
        synchronizeSequence();
    }

    @Override
    protected String extractId(SecurityAlert entity) {
        return entity.getAlertId();
    }

    private void synchronizeSequence() {
        findAll().stream()
                .map(SecurityAlert::getAlertId)
                .filter(id -> id != null && id.startsWith("ALT-"))
                .mapToLong(id -> {
                    try {
                        return Long.parseLong(id.substring(4));
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                })
                .max()
                .ifPresent(IdGenerator::synchronizeAlertSeq);
    }

    private void refreshQueue() {
        pendingAlertQueue.clear();
        findAll().stream()
                .filter(alert -> alert.getStatus() == AlertStatus.OPEN)
                .sorted(Comparator.comparing(SecurityAlert::getTimestamp))
                .forEach(pendingAlertQueue::offer);
    }

    @Override
    public SecurityAlert save(SecurityAlert alert) {
        SecurityAlert saved = super.save(alert);
        refreshQueue();
        return saved;
    }

    public List<SecurityAlert> findBySeverity(AlertSeverity severity) {
        if (severity == null) return List.of();
        return findAll().stream()
                .filter(a -> a.getSeverity() == severity)
                .sorted(Comparator.comparing(SecurityAlert::getTimestamp).reversed())
                .toList();
    }

    public List<SecurityAlert> findHighAndCriticalAlerts() {
        return findAll().stream()
                .filter(a -> a.getSeverity() == AlertSeverity.HIGH || a.getSeverity() == AlertSeverity.CRITICAL)
                .sorted(Comparator.comparing(SecurityAlert::getTimestamp).reversed())
                .toList();
    }

    public List<SecurityAlert> findByCustomerId(String customerId) {
        if (customerId == null) return List.of();
        return findAll().stream()
                .filter(a -> customerId.equals(a.getCustomerId()))
                .sorted(Comparator.comparing(SecurityAlert::getTimestamp).reversed())
                .toList();
    }

    /**
     * Demonstrates Queue retrieval: polls the next unreviewed alert in FIFO order.
     */
    public Optional<SecurityAlert> pollNextPendingAlert() {
        SecurityAlert alert = pendingAlertQueue.poll();
        return Optional.ofNullable(alert);
    }

    /**
     * Demonstrates HashSet usage: returns unique set of all triggered security rule names.
     */
    public Set<String> getUniqueTriggeredRules() {
        Set<String> uniqueRules = new HashSet<>();
        for (SecurityAlert alert : findAll()) {
            if (alert.getRuleTriggered() != null) {
                uniqueRules.add(alert.getRuleTriggered());
            }
        }
        return Collections.unmodifiableSet(uniqueRules);
    }
}
